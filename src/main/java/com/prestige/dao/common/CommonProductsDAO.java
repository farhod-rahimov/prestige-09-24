package com.prestige.dao.common;

import com.prestige.dao.mappers.BrandMapper;
import com.prestige.dao.mappers.ProductMapper;
import com.prestige.dao.mappers.ProductMapperWrappedInSupply;
import com.prestige.dao.mappers.ProductNameMapper;
import com.prestige.models.*;
import com.prestige.models.filters.BasketFilter;
import com.prestige.models.filters.ProductFilter;
import com.prestige.service.Utils;
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
import java.util.function.Function;
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
        return findAllProductsInternal(null);
    }

    public List<Product> findAllProductsWIthFilter(ProductFilter productFilter) {
        return findAllProductsInternal(productFilter);
    }

    private List<Product> findAllProductsInternal(ProductFilter productFilter) {

        List<Object> bindings = new ArrayList();

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
                            "cp.notes, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE 1 = 1 ";

        if (productFilter != null && productFilter.getProduct() != null) {
            Product product = productFilter.getProduct();

            Integer groupId = product.getEquivalentProductInfo().getGroupId();
            if (groupId != null && groupId != 0) {
                sql +=  "AND cpe.group_id = ? ";
                bindings.add(groupId);
            }

            Integer productId = product.getId();
            if (productId != null && productId != 0) {
                sql +=  "AND cp.id  = ? ";
                bindings.add(productId);
            }

            ProductName productName = product.getProductName();
            if (productName != null && productName.getProductName() != null &&
                    !productName.getProductName().isEmpty()) {

                sql +=  "AND cpn.product_name ILIKE ? ESCAPE '\\' ";
                bindings.add(productName.getProductName());
            }

            if (product.getCarModel() != null && !product.getCarModel().isEmpty()) {
                sql +=  "AND cp.car_model ILIKE ? ESCAPE '\\' ";
                bindings.add(product.getCarModel());
            }

            if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
                sql +=  "AND cp.engine_model ILIKE ? ESCAPE '\\' ";
                bindings.add(product.getEngineModel());
            }

            if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
                sql +=  "AND cp.oem_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(product.getOemArticleNumber());
            }

            if (product.getCrossArticleNumber() != null && !product.getCrossArticleNumber().isEmpty()) {
                sql +=  "AND cp.cross_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(product.getCrossArticleNumber());
            }

            Brand brand = product.getBrand();
            if (brand != null && brand.getBrandName() != null && !brand.getBrandName().isEmpty()) {
                sql +=  "AND cbn.brand_name ILIKE ? ESCAPE '\\' ";
                bindings.add(brand.getBrandName());
            }

            if (product.getNotes() != null && !product.getNotes().isEmpty()) {
                sql +=  "AND cp.notes ILIKE ? ESCAPE '\\' ";
                bindings.add(product.getNotes());
            }
        }

        sql +=          "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name, cpe.group_id)";

        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new ProductMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new ProductMapper());
    }

    private <S extends BaseSupply> List<S> findAllProductsWrappedInSupplyInternal(List<Integer> excludeProductIds,
                                                                                  Function<Product, S> creator) {
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
                            "cp.notes, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                    "FROM common_products cp " +

                    "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                    "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                    "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id ";

        if (!excludeProductIds.isEmpty()) {
            String inClause = excludeProductIds.stream().map(id -> "?").collect(Collectors.joining(", "));
            sql +=      "WHERE cp.id NOT IN ("+ inClause +") ";
        }

        sql +=          "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name)";

        if (!excludeProductIds.isEmpty()) {
            return jdbcTemplate.query(sql, excludeProductIds.toArray(), new ProductMapperWrappedInSupply(creator));
        }
        return jdbcTemplate.query(sql, new ProductMapperWrappedInSupply(creator));
    }

    public <S> List<S> findAllProductsWrappedInSupply(Class<S> supplyClass, List<Integer> excludeProductIds) {

        if (Supply.class.equals(supplyClass)) {
            return (List<S>)findAllProductsWrappedInSupplyInternal(excludeProductIds, Supply::new);
        }
        else {
            return (List<S>) findAllProductsWrappedInSupplyInternal(excludeProductIds, SkladSupply::new);
        }
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
                            "cp.notes, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

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

    private String getFilterClause(BasketFilter filter, List<Object> bindings) {
        String sql = "";

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
        return sql;
    }

    public List<ProductName> findAllProductNames(BasketFilter filter) {
        String sql =    "SELECT cpn.id, cpn.product_name " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE 1 = 1 ";

        List<Object> bindings = new ArrayList();

        if (filter != null) {
            sql += getFilterClause(filter, bindings);
        }

        sql +=          "GROUP BY (cpn.id) " +
                        "ORDER BY (cpn.product_name)";

        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new ProductNameMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new ProductNameMapper());
    }

    public List<String> findCarModelsByProductNameId(Integer productNameId, BasketFilter filter) {
        String sql =    "SELECT cp.car_model " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE cpn.id = ? ";

        List<Object> bindings = new ArrayList();
        bindings.add(productNameId);

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeCarModel();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.car_model) " +
                        "ORDER BY (cp.car_model)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableEngineModels(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.engine_model " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeEngineModel();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.engine_model) " +
                        "ORDER BY (cp.engine_model)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableOemArticleNumbers(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.oem_article_number " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? ";

        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeOemArticleNumber();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.oem_article_number) " +
                        "ORDER BY (cp.oem_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableCrossArticleNumbers(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.cross_article_number " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? AND " +
                            "cp.oem_article_number = ? ";

        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());
        bindings.add(product.getOemArticleNumber());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeCrossArticleNumber();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.cross_article_number) " +
                        "ORDER BY (cp.cross_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<Brand> findAvailableBrands(Product product, BasketFilter filter) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model = ? AND " +
                            "cp.oem_article_number = ? AND " +
                            "cp.cross_article_number = ? ";



        List<Object> bindings = new ArrayList();

        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());
        bindings.add(product.getEngineModel());
        bindings.add(product.getOemArticleNumber());
        bindings.add(product.getCrossArticleNumber());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeBrand();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cbn.id, cbn.brand_name) " +
                        "ORDER BY (cbn.brand_name)";

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }

    public boolean checkIfProductNameExists(Integer productNameId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_products WHERE product_name_id = ?)";

        return jdbcTemplate.queryForObject(sql, new Object[]{productNameId}, Boolean.class);
    }

    public boolean checkIfBrandExists(Integer brandId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_products WHERE brand_name_id = ?)";

        return jdbcTemplate.queryForObject(sql, new Object[]{brandId}, Boolean.class);
    }
}
