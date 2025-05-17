package com.prestige.dao.common;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommonProductsDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils;

    @Autowired
    public CommonProductsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Product> findAllProducts() {
        String sql =    "SELECT " +

                            "cp.id, " +
                            "cpn.id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name)";

        return jdbcTemplate.query(sql, new ProductMapper());
    }

    public List<Supply> findAllProductsWrappedInSupply(List<Integer> excludeProductIds) {
        String sql =    "SELECT " +

                            "cp.id, " +
                            "cpn.id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes " +

                    "FROM common_products cp " +
                    "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                    "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id ";

        if (!excludeProductIds.isEmpty()) {
            String inClause = excludeProductIds.stream().map(id -> "?").collect(Collectors.joining(", "));
            sql +=      "WHERE cp.id NOT IN ("+ inClause +") ";
        }

        sql +=          "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name)";

        if (!excludeProductIds.isEmpty()) {
            return jdbcTemplate.query(sql, excludeProductIds.toArray(), new ProductMapperWrappedInSupply());
        }
        return jdbcTemplate.query(sql, new ProductMapperWrappedInSupply());
    }

    public boolean checkIfProductExists(Product product) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_products WHERE " +
                                    "product_name_id = ? AND " +
                                    "car_model = ? AND " +
                                    "engine_model = ? AND " +
                                    "oem_article_number = ? AND " +
                                    "cross_article_number = ? AND " +
                                    "brand_name_id = ?";

        List<Object> bindings = new ArrayList();

        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel(),
                product.getEngineModel(), product.getOemArticleNumber(),
                product.getCrossArticleNumber(), product.getBrand().getId()));

        if (product.getId() != null) {
            sql += " AND id != ?";
            bindings.add(product.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public Integer findProductId(Product product) {
        String sql =    "SELECT id " +
                        "FROM common_products cp " +
                        "WHERE " +
                            "cp.brand_name_id = ? AND " +
                            "cp.product_name_id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();

        bindings.addAll(Arrays.asList(product.getBrand().getId(),
                                      product.getProductName().getId(),
                                      product.getCarModel()));

        if (product.getEngineModel() != null) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        if (product.getCrossArticleNumber() != null) {
            sql +=          "AND cp.cross_article_number = ? ";
            bindings.add(product.getCrossArticleNumber());
        }
        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Integer.class);
    }

    public boolean saveNewProduct(Product product) {
        utils.formatProductAttributes(product);

        if (!checkIfProductExists(product)) {
            String sql =    "INSERT INTO common_products (" +

                                "product_name_id, " +
                                "car_model, " +
                                "engine_model, " +
                                "oem_article_number, " +
                                "cross_article_number, " +
                                "brand_name_id, " +
                                "notes" +

                            ") VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setInt(1, product.getProductName().getId());
                ps.setString(2, product.getCarModel());
                ps.setString(3, product.getEngineModel());
                ps.setString(4, product.getOemArticleNumber());
                ps.setString(5, product.getCrossArticleNumber());
                ps.setInt(6, product.getBrand().getId());
                ps.setString(7, product.getNotes());

                return ps;
            }, keyHolder);

            product.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public Product findProductById(Integer productId) {
        String sql =    "SELECT " +

                            "cp.id, " +
                            "cpn.id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE cp.id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {productId}, new ProductMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateProduct(Product product) {
        utils.formatProductAttributes(product);

        String sql =    "UPDATE common_products SET " +

                            "product_name_id = ?, " +
                            "car_model = ?, " +
                            "engine_model = ?, " +
                            "oem_article_number = ?, " +
                            "cross_article_number = ?, " +
                            "brand_name_id = ?, " +
                            "notes = ? " +

                        "WHERE id = ?";

        Object[] bindings = new Object[]{
                product.getProductName().getId(),
                product.getCarModel(),
                product.getEngineModel(),
                product.getOemArticleNumber(),
                product.getCrossArticleNumber(),
                product.getBrand().getId(),
                product.getNotes(),
                product.getId()};

        jdbcTemplate.update(sql, bindings);
    }

    public void deleteProductById(Integer productId) {
        String sql = "DELETE FROM common_products WHERE id = ?";

        jdbcTemplate.update(sql, productId);
    }

    private List<ProductName> findAllProductNamesInternal(BasketFilter filter) {
        String sql =    "SELECT cpn.id, cpn.product_name " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE 1 = 1 ";

        List<Object> bindings = new ArrayList();

        if (filter != null) {
            if (!filter.getProductName().isEmpty()) {
                sql += "AND cpn.product_name ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getProductName());
            }

            if (!filter.getCarModel().isEmpty()) {
                sql += "AND cp.car_model ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getCarModel());
            }

            if (!filter.getEngineModel().isEmpty()) {
                sql += "AND cp.engine_model ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getEngineModel());
            }

            if (!filter.getOemArticleNumber().isEmpty()) {
                sql += "AND cp.oem_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getOemArticleNumber());
            }

            if (!filter.getCrossArticleNumber().isEmpty()) {
                sql += "AND cp.cross_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getCrossArticleNumber());
            }

            if (!filter.getBrand().isEmpty()) {
                sql += "AND cbn.brand_name ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getBrand());
            }
        }

        sql +=          "GROUP BY (cpn.id) " +
                        "ORDER BY (cpn.product_name)";

        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new ProductNameMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new ProductNameMapper());
    }

    public List<ProductName> findAllProductNames() {
        return findAllProductNamesInternal(null);
    }

    public List<ProductName> findAllProductNamesWithFilter(BasketFilter filter) {
        return findAllProductNamesInternal(filter);
    }

    public List<String> findCarModelsByProductNameId(Integer productNameId) {
        String sql =    "SELECT cp.car_model " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "WHERE cpn.id = ? " +
                        "GROUP BY (cp.car_model) " +
                        "ORDER BY (cp.car_model)";

        return jdbcTemplate.queryForList(sql, new Object[] {productNameId}, String.class);
    }

    public List<String> findAvailableEngineModels(Product product) {
        String sql =    "SELECT cp.engine_model " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model IS NOT NULL AND cp.engine_model != '' " +

                        "GROUP BY (cp.engine_model) " +
                        "ORDER BY (cp.engine_model)";

        return jdbcTemplate.queryForList(sql, new Object[] {product.getProductName().getId(), product.getCarModel()}, String.class);
    }

    public List<String> findAvailableOemArticleNumbers(Product product) {
        String sql =    "SELECT cp.oem_article_number " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? AND " +
                            "cp.oem_article_number IS NOT NULL AND cp.oem_article_number != '' " +

                        "GROUP BY (cp.oem_article_number) " +
                        "ORDER BY (cp.oem_article_number)";

        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableCrossArticleNumbers(Product product) {
        String sql =    "SELECT cp.cross_article_number " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? AND " +
                            "cp.oem_article_number = ? AND " +
                            "cp.cross_article_number IS NOT NULL AND cp.cross_article_number != '' " +

                        "GROUP BY (cp.cross_article_number) " +
                        "ORDER BY (cp.cross_article_number)";

        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());
        bindings.add(product.getOemArticleNumber());

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<Brand> findAvailableBrands(Product product) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? AND " +
                            "cp.oem_article_number = ? AND " +
                            "cp.cross_article_number = ? " +

                        "GROUP BY (cbn.id, cbn.brand_name) " +
                        "ORDER BY (cbn.brand_name)";

        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());
        bindings.add(product.getOemArticleNumber());
        bindings.add(product.getCrossArticleNumber());

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }
}
