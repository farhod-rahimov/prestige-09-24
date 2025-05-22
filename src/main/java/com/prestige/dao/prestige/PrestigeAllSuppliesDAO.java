package com.prestige.dao.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PrestigeAllSuppliesDAO {

    private final JdbcTemplate jdbcTemplate;

    private Utils utils;

    @Autowired
    public PrestigeAllSuppliesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Supply> findAllSuppliesByBasketId(Integer basketId) {

        String sql =    "SELECT " +

                            "pas.id, " +
                            "pas.quantity, " +
                            "pas.price_dollar, " +

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

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE pas.basket_id = ? " +
                        "ORDER BY (pas.id)";

        return jdbcTemplate.query(sql, new Object[] {basketId}, new SupplyMapper());
    }

    public void updateBasketTotalSum(SupplyBasket basket) {
        String sql =    "UPDATE prestige_supply_baskets SET total_sum = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getTotalSum(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void saveNewPrestigeSupply(SupplyBasket basket, Supply supply) {
        String sql =    "INSERT INTO prestige_all_supplies (" +

                            "common_product_id, " +
                            "quantity, " +
                            "price_dollar, " +
                            "basket_id" +
                        ") VALUES (?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, supply.getProduct().getId());
            ps.setInt(2, supply.getQuantity());
            ps.setBigDecimal(3, supply.getPriceDollar());
            ps.setInt(4, basket.getId());

            return ps;
        }, keyHolder);

        supply.setId(keyHolder.getKey().intValue());
    }

    public Map<Integer, Integer> findStockBeforeSale(Set<Integer> productIds, BaseBasket basket) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "SELECT cp.id AS product_id, " +
                            "(" +
                                "(COALESCE( " +
                                    "(SELECT SUM(pas1.quantity) " +
                                    "FROM prestige_all_supplies pas1 " +
                                    "JOIN prestige_supply_baskets psb1 ON pas1.basket_id = psb1.id " +
                                    "WHERE " +
                                        "pas1.common_product_id = cp.id AND " +
                                        "psb1.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " AND " +
                                        "psb1.created_date_time < ?), 0))" +

                                    " - " +

                                "(COALESCE( " +
                                    "(SELECT SUM(pas2.quantity) " +
                                    "FROM prestige_all_sales pas2 " +
                                    "JOIN prestige_sales_baskets psb2 on pas2.basket_id = psb2.id " +
                                    "WHERE " +
                                        "pas2.common_product_id = cp.id AND " +
                                        "psb2.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " AND " +
                                        "psb2.created_date_time < ?), 0))" +
                            ") AS stock_before_sale " +
                        "FROM common_products cp " +
                        "WHERE cp.id IN (" + inClause + ")";

        List<Object> bindings = new ArrayList();

        bindings.add(basket.getCreatedDateTime());
        bindings.add(basket.getCreatedDateTime());
        bindings.addAll(productIds);

        return jdbcTemplate.query(sql, bindings.toArray(), rs -> {
            Map<Integer, Integer> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getInt(1), rs.getInt(2));
            }
            return result;
        });
    }

    public void saveNewPrestigeSuppliesList(SupplyBasket basket, List<Supply> suppliesList) {
        saveBatch(suppliesList, basket.getId());
        setSuppliesIds(suppliesList, basket.getId());
    }

    public void updateQuantityAndPrices(List<Supply> suppliesList) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Supply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "UPDATE prestige_all_supplies SET quantity = ?, price_dollar = ? WHERE id = ?";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Supply supply = batch.get(index);
                    ps.setInt(1, supply.getQuantity());
                    ps.setBigDecimal(2, supply.getPriceDollar());
                    ps.setInt(3, supply.getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private void saveBatch(List<Supply> suppliesList, Integer basketId) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Supply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "INSERT INTO prestige_all_supplies (" +

                                "common_product_id, " +
                                "quantity, " +
                                "price_dollar, " +
                                "basket_id" +
                            ") VALUES (?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Supply supply = batch.get(index);

                    ps.setInt(1, supply.getProduct().getId());
                    ps.setInt(2, supply.getQuantity());
                    ps.setBigDecimal(3, supply.getPriceDollar());
                    ps.setInt(4, basketId);
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private void setSuppliesIds(List<Supply> suppliesList, Integer basketId) {
        List<Supply> allBasketSavedSupplies = findAllSuppliesByBasketId(basketId);
        Map<Integer, Integer> productIdSupplyIdMap = allBasketSavedSupplies.stream()
                .collect(Collectors.toMap(s -> s.getProduct().getId(), s -> s.getId())); // map <productId, salesId>

        for(Supply supply : suppliesList) {
            supply.setId(productIdSupplyIdMap.get(supply.getProduct().getId()));
        }
    }

    public void deleteSuppliesIds(List<Integer> suppliesIds) {
        String sql =    "DELETE FROM prestige_all_supplies WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, suppliesIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return suppliesIds.size();
            }
        });
    }

    public List<RetailProductView> findAllPrestigeProductsInStock() {
        String sql =    "WITH sales_info AS (" +
                            "SELECT " +
                                "common_product_id, " +
                                "SUM(quantity) AS total_quantity " +
                            "FROM prestige_all_sales pas " +
                            "JOIN prestige_sales_baskets psb ON pas.basket_id = psb.id " +
                            "WHERE psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                            "GROUP BY (common_product_id)), " +

                        "supply_info AS (" +
                            "SELECT " +
                                "pas.common_product_id AS cp_id, " +
                                "ROUND(SUM(pas.price_dollar * pas.quantity) / NULLIF(SUM(pas.quantity), 0), 2) AS avg_price, " +
                                "SUM(pas.quantity) - COALESCE(MAX(sales_info.total_quantity), 0) AS stock " + // обязательно использовать агрегатную ф-ию для sales_info.total_quantity

                            "FROM prestige_all_supplies pas " +
                            "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                            "LEFT JOIN sales_info ON pas.common_product_id = sales_info.common_product_id " +
                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY (cp_id) " +
                        ")" +

                        "SELECT " +
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
                            "COALESCE(supply_info.avg_price, 0) AS avg_price, " +
                            "COALESCE(supply_info.stock, 0) AS stock " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN supply_info ON cp.id = supply_info.cp_id " +

                        "ORDER BY   (cbn.brand_name, cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number) ";

        return jdbcTemplate.query(sql, new RetailProductViewMapper());
    }

    public List<Sales> findAllPrestigeProductsWrappedInSales(List<Integer> excludeProductIds) {
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

                        "FROM prestige_all_supplies pas " +
                        "JOIN common_products cp ON cp.id = pas.common_product_id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id ";

        if (!excludeProductIds.isEmpty()) {
            String inClause = excludeProductIds.stream().map(id -> "?").collect(Collectors.joining(", "));
            sql +=      "WHERE cp.id NOT IN ("+ inClause +") ";
        }

        sql +=          "GROUP BY   (cp.id, cpn.id, cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.id, cbn.brand_name) " +

                        "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name)";

        if (!excludeProductIds.isEmpty()) {
            return jdbcTemplate.query(sql, excludeProductIds.toArray(), new ProductMapperWrappedInSales());
        }
        return jdbcTemplate.query(sql, new ProductMapperWrappedInSales());
    }

    private List<ProductName> findAllProductNamesInternal(BasketFilter filter) {
        String sql =    "SELECT cpn.id, cpn.product_name " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
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

    public List<String> findAvailableCarModels(Product product) {
        String sql =    "SELECT cp.car_model " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +

                        "WHERE cpn.id = ? " +
                        "GROUP BY (cp.car_model) " +
                        "ORDER by (cp.car_model)";

        return jdbcTemplate.queryForList(sql, new Object[]{product.getProductName().getId()}, String.class);
    }

    public List<String> findAvailableEngineModels(Product product) {
        String sql =    "SELECT cp.engine_model " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
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

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
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

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
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

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
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

    public Map<Integer, BigDecimal> findWholesalePrice(Set<Integer> productIds) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =   "WITH    latest_prices AS ( " +
                                    "SELECT DISTINCT ON (common_product_id) " +
                                        "pas.common_product_id, " +
                                        "pas.price_dollar, " +
                                        "pas.date_time " +
                                    "FROM prestige_all_supplies pas " +
                                    "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                                    "WHERE " +
                                        "common_product_id IN (" + inClause + ") AND " +
                                        "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                                    "ORDER BY common_product_id, date_time DESC), " +

                                    "stock_data AS ( " +
                                        "SELECT " +
                                            "pas.common_product_id as pp_id, " +
                                            "SUM(pas.quantity) AS total_stock, " +
                                            "SUM(pas.quantity * pas.price_dollar) AS total_stock_value, " +
                                            "lp.price_dollar AS latest_price " +
                                    "FROM prestige_all_supplies pas " +
                                    "JOIN latest_prices lp ON pas.common_product_id = lp.common_product_id " +
                                    "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                                    "WHERE " +
                                        "pas.common_product_id IN ("+ inClause +") " +
                                        "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                                    "GROUP BY pp_id, lp.price_dollar), " +

                                    "sales_data AS ( " +
                                        "SELECT " +
                                            "pas.common_product_id as pas_id, " +
                                            "COALESCE(SUM(pas.quantity), 0) AS total_sold, " +
                                            "COALESCE(SUM(pas.quantity * pas.wholesale_price), 0) AS total_sold_value " +
                                    "FROM prestige_all_sales pas " +
                                    "JOIN prestige_sales_baskets psb ON psb.id = pas.basket_id " +
                                    "WHERE pas.common_product_id IN (" + inClause + ") AND " +
                                            "psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                                    "GROUP BY pas_id) " +

                                "SELECT " +
                                    "sd.pp_id, " +
                                    "CASE " +
                                        "WHEN (sd.total_stock - sd2.total_sold) = 0 " +
                                            "THEN sd.latest_price " +
                                        "WHEN sd2.total_sold IS NULL " +
                                            "THEN sd.total_stock_value / sd.total_stock " + // ср. взвешенная цена поступления
                                        "ELSE (sd.total_stock_value - sd2.total_sold_value) / (sd.total_stock - sd2.total_sold) " +
                                    "END AS calculated_price " +
                                "FROM stock_data sd " +
                                "LEFT JOIN sales_data sd2 ON sd.pp_id = sd2.pas_id";

        List<Object> bindings = new ArrayList();
        bindings.addAll(productIds);
        bindings.addAll(productIds);
        bindings.addAll(productIds);

        return jdbcTemplate.query(sql, bindings.toArray(), rs -> {
            Map<Integer, BigDecimal> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getInt(1), rs.getBigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            }
            return result;
        });
    }

    public List<RetailProduct> findPrestigeProductSupplyListByProductId(Integer productId) {
        String sql =    "SELECT " +
                            "pas.id as pas_id, " +
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
                            "pas.price_dollar, " +
                            "pas.quantity, " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "psb.created_date_time " + // TODO: заменить на received_date_time

                        "FROM prestige_all_supplies pas " +

                        "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN prestige_suppliers ps ON psb.supplier_id = ps.id " +

                        "WHERE " +
                            "cp.id = ? AND " +
                            "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +

                        "ORDER BY(psb.created_date_time)";

        return jdbcTemplate.query(sql, new Object[]{productId}, new RetailProductMapper());
    }
}
