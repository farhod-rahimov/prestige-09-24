package com.prestige.dao.prestige;

import com.prestige.dao.mappers.BrandMapper;
import com.prestige.dao.mappers.ProductNameMapper;
import com.prestige.dao.mappers.RetailProductMapper;
import com.prestige.dao.mappers.RetailProductViewMapper;
import com.prestige.models.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.util.*;

@Component
@Getter
public class PrestigeProductsDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeProductsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RetailProductView> findAllProductsView() {
        String sql =    "SELECT " +

                            "cp.id as cp_id, " +
                            "cpn.id as cpn_id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes, " +
                            "ROUND(SUM(pp.price_dollar * pp.stock_balance) / SUM(pp.stock_balance), 2), " +
                            "SUM(pp.stock_balance) " +

                        "FROM prestige_products pp " +

                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "GROUP BY   (cp.id, cpn.id, cbn.id) " +

                        "ORDER BY   (cbn.brand_name, cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number) ";

        return jdbcTemplate.query(sql, new RetailProductViewMapper());
    }

    public void saveNewPrestigeProduct(RetailProduct retailProduct) {
        String sql =    "INSERT INTO prestige_products (" +

                            "common_product_id, " +
                            "price_dollar, " +
                            "stock_balance, " +
                            "supplier_id, " +
                            "date_time" +
                        ") VALUES (?, ?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, retailProduct.getProduct().getId());
            ps.setBigDecimal(2, retailProduct.getPriceDollar());
            ps.setInt(3, retailProduct.getStockBalance());
            ps.setInt(4, retailProduct.getSupplier().getId());
            ps.setTimestamp(5, retailProduct.getDeliveryDateTime());

            return ps;
        }, keyHolder);

        retailProduct.setId(keyHolder.getKey().intValue());
    }

    public void updatePrestigeProduct(RetailProduct retailProduct) {
        String sql =    "UPDATE prestige_products SET " +

                            "price_dollar = ?, " +
                            "stock_balance = ? " +

                        "WHERE id = ?";

        Object[] bindings = new Object[]{
                retailProduct.getPriceDollar(),
                retailProduct.getStockBalance(),
                retailProduct.getId()
        };

        jdbcTemplate.update(sql, bindings);
    }

    public void deletePrestigeProductById(Integer prestigeProductId) {
        String sql = "DELETE from prestige_products WHERE id = ?";

        jdbcTemplate.update(sql, prestigeProductId);
    }

    public List<RetailProduct> findPrestigeProductSupplyListByProductId(Integer productId) {
        String sql =    "SELECT " +
                            "pp.id as pp_id, " +
                            "cp.id as cp_id, " +
                            "cbn.id as cbn_id, " +
                            "cbn.brand_name, " +
                            "cpn.id as cpn_id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cp.notes, " +
                            "pp.price_dollar, " +
                            "pp.stock_balance, " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "pp.date_time " +

                        "FROM prestige_products pp " +

                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN prestige_suppliers ps ON pp.supplier_id = ps.id " +

                        "WHERE cp.id = ? " +

                        "ORDER BY(pp.date_time)";

        return jdbcTemplate.query(sql, new Object[]{productId}, new RetailProductMapper());
    }

    public RetailProduct findPrestigeProductSupplyByProductIdAndSupplyId(Integer productId, Integer supplyId) {
        String sql =    "SELECT " +
                            "pp.id as pp_id, " +
                            "cp.id as cp_id, " +
                            "cbn.id as cbn_id, " +
                            "cbn.brand_name, " +
                            "cpn.id as cpn_id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cp.notes, " +
                            "pp.price_dollar, " +
                            "pp.stock_balance, " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "pp.date_time " +

                        "FROM prestige_products pp " +

                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN prestige_suppliers ps ON pp.supplier_id = ps.id " +

                        "WHERE " +
                            "cp.id = ? AND " +
                            "pp.id = ?";


        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{productId, supplyId}, new RetailProductMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public RetailProduct findPrestigeProductSupplyBySupplyId(Integer supplyId) {
        String sql = "SELECT " +
                        "pp.id as pp_id, " +
                        "cp.id as cp_id, " +
                        "cbn.id as cbn_id, " +
                        "cbn.brand_name, " +
                        "cpn.id as cpn_id, " +
                        "cpn.product_name, " +
                        "cp.car_model, " +
                        "cp.engine_model, " +
                        "cp.oem_article_number, " +
                        "cp.cross_article_number, " +
                        "cp.notes, " +
                        "pp.price_dollar, " +
                        "pp.stock_balance, " +
                        "ps.id as psn_id, " +
                        "ps.name, " +
                        "ps.phone_number, " +
                        "ps.store_name, " +
                        "pp.date_time " +

                    "FROM prestige_products pp " +

                    "JOIN common_products cp ON pp.common_product_id = cp.id " +
                    "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                    "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                    "JOIN prestige_suppliers ps ON pp.supplier_id = ps.id " +

                    "WHERE " +
                        "pp.id = ?";


        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{supplyId}, new RetailProductMapper());
        } catch (DataAccessException e) {
            return null;
        }
    }

    public List<ProductName> findAllProductNames() {
        String sql =    "SELECT cpn.id, cpn.product_name " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "GROUP BY (cpn.id) " +
                        "ORDER BY (cpn.product_name)";

        return jdbcTemplate.query(sql, new ProductNameMapper());
    }

    public List<String> findAvailableCarModels(Product product) {
        String sql =    "SELECT cp.car_model " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "WHERE cpn.id = ? " +
                        "GROUP BY (cp.car_model) " +
                        "ORDER by (cp.car_model)";

        return jdbcTemplate.queryForList(sql, new Object[]{product.getProductName().getId()}, String.class);
    }

    public List<String> findAvailableEngineModels(Product product) {
        String sql =    "SELECT cp.engine_model " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model IS NOT NULL AND " +
                            "cp.engine_model != '' " +
                        "GROUP BY (cp.engine_model) " +
                        "ORDER by (cp.engine_model)";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableOemArticleNumbers(Product product) {
        String sql =    "SELECT cp.oem_article_number " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "cp.engine_model = ? AND ";
            bindings.add(product.getEngineModel());
        }

        sql +=              "cp.oem_article_number IS NOT NULL AND " +
                            "cp.oem_article_number != '' " +
                        "GROUP BY (cp.oem_article_number) " +
                        "ORDER by (cp.oem_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableCrossArticleNumbers(Product product) {
        String sql =    "SELECT cp.cross_article_number " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        sql +=              "AND cp.cross_article_number IS NOT NULL " +
                            "AND cp.cross_article_number != '' " +
                            "GROUP BY (cp.cross_article_number) " +
                            "ORDER by (cp.cross_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<Brand> findAvailableBrands(Product product) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM prestige_products pp " +
                        "JOIN common_products cp ON pp.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        if (product.getCrossArticleNumber() != null && !product.getCrossArticleNumber().isEmpty()) {
            sql +=          "AND cp.cross_article_number = ? ";
            bindings.add(product.getCrossArticleNumber());
        }

        sql +=              "GROUP BY (cbn.id) " +
                            "ORDER by (cbn.brand_name)";

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }

    public Integer findStockBeforeSale(Product product, Basket basket) {
        String sql =    "SELECT " +
                            "(SELECT SUM(pp.stock_balance) " +
                             "FROM prestige_products pp " +
                             "WHERE pp.common_product_id = ? AND " +
                                   "pp.date_time < ?) " +

                            " - " +

                            "(COALESCE(" +
                                "(SELECT SUM(pas.quantity) " +
                                 "FROM prestige_all_sales pas " +
                                 "JOIN prestige_baskets pb on pas.basket_id = pb.id " +
                                 "WHERE pas.common_product_id = ? AND " +
                                       "pb.created_date_time < ?), 0))";

        List<Object> bindings = new ArrayList();

        bindings.addAll(Arrays.asList(product.getId(), basket.getCreatedDateTime()));
        bindings.addAll(Arrays.asList(product.getId(), basket.getCreatedDateTime()));

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Integer.class);
    }

    public BigDecimal findWholesalePrice(Product product) {
        String sql =   "WITH    stock_data AS ( " +
                                    "SELECT " +
                                        "SUM(pp.stock_balance) AS total_stock, " +
                                        "SUM(pp.stock_balance * pp.price_dollar) AS total_stock_value, " +
                                        "( " +
                                            "SELECT pp.price_dollar " +
                                            "FROM prestige_products pp " +
                                            "WHERE pp.common_product_id = ? " +
                                            "ORDER BY pp.date_time DESC " +
                                            "LIMIT 1 " +
                                        ") AS latest_price " +
                                    "FROM prestige_products pp " +
                                    "WHERE pp.common_product_id = ?), " +

                                "sales_data AS ( " +
                                    "SELECT " +
                                        "COALESCE(SUM(pas.quantity), 0) AS total_sold, " +
                                        "COALESCE(SUM(pas.quantity * pas.wholesale_price), 0) AS total_sold_value " +
                                    "FROM prestige_all_sales pas " +
                                    "WHERE pas.common_product_id = ?) " +

                                "SELECT " +
                                    "CASE " +
                                        "WHEN (sd.total_stock - sd2.total_sold) = 0 " +
                                        "THEN sd.latest_price " +
                                        "ELSE (sd.total_stock_value - sd2.total_sold_value) / (sd.total_stock - sd2.total_sold) " +
                                    "END AS calculated_price " +
                                "FROM stock_data sd, sales_data sd2;";

        List<Object> bindings = new ArrayList();

        bindings.addAll(Arrays.asList(product.getId(), product.getId(), product.getId()));

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), BigDecimal.class)
                           .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal findRecommendedPrice(Product product, BigDecimal calculatedPrice) {
        String sql =    "SELECT SUM(COALESCE(pas.quantity, 0) * COALESCE(pas.sold_price_smn, 0)) / " +
                               "SUM(COALESCE(pas.quantity, 1)) "; // 1 чтобы не было деления на 0

        sql +=          "FROM prestige_all_sales pas " +
                        "WHERE pas.common_product_id = ? " +
                        "GROUP BY (pas.common_product_id)";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{product.getId()} , BigDecimal.class)
                               .setScale(2, RoundingMode.HALF_UP);
        }
        catch (DataAccessException e) {
            return calculatedPrice;
        }
    }
}
