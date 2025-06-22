package com.prestige.dao.prestige;


import com.prestige.dao.mappers.SalesMapper;
import com.prestige.models.SalesBasket;
import com.prestige.models.Sales;
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
public class PrestigeAllSalesDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeAllSalesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveNewPrestigeSale(SalesBasket basket, Sales sales) {
        String sql =    "INSERT INTO prestige_all_sales (" +

                            "common_product_id, " +
                            "quantity, " +
                            "sold_price_smn, " +
                            "wholesale_price, " +
                            "basket_id" +
                        ") VALUES (?, ?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, sales.getProduct().getId());
            ps.setInt(2, sales.getQuantity());
            ps.setBigDecimal(3, sales.getSoldPrice());
            ps.setBigDecimal(4, sales.getWholesalePrice());
            ps.setInt(5, basket.getId());

            return ps;
        }, keyHolder);

        sales.setId(keyHolder.getKey().intValue());
    }

    private void saveBatch(List<Sales> salesList, Integer basketId) {
        int batchSize = 500;
        int totalSize = salesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Sales> batch = salesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "INSERT INTO prestige_all_sales (" +

                                "common_product_id, " +
                                "quantity, " +
                                "sold_price_smn, " +
                                "wholesale_price, " +
                                "basket_id" +
                    ") VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Sales sales = batch.get(index);

                    ps.setInt(1, sales.getProduct().getId());
                    ps.setInt(2, sales.getQuantity());
                    ps.setBigDecimal(3, sales.getSoldPrice());
                    ps.setBigDecimal(4, sales.getWholesalePrice());
                    ps.setInt(5, basketId);
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private void setSalesIds(List<Sales> salesList, Integer basketId) {
        List<Sales> allBasketSavedSales = findAllSalesByBasketId(basketId);
        Map<Integer, Integer> productIdSalesIdMap = allBasketSavedSales.stream()
                .collect(Collectors.toMap(s -> s.getProduct().getId(), s -> s.getId())); // map <productId, salesId>

        for(Sales sales : salesList) {
            sales.setId(productIdSalesIdMap.get(sales.getProduct().getId()));
        }
    }

    public void saveNewPrestigeSalesList(SalesBasket basket, List<Sales> salesList) {
        saveBatch(salesList, basket.getId());
        setSalesIds(salesList, basket.getId());
    }

    public void updateQuantityAndPrices(List<Sales> salesList) {
        int batchSize = 500;
        int totalSize = salesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Sales> batch = salesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "UPDATE prestige_all_sales SET quantity = ?, sold_price_smn = ? WHERE id = ?";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Sales sales = batch.get(index);
                    ps.setInt(1, sales.getQuantity());
                    ps.setBigDecimal(2, sales.getSoldPrice());
                    ps.setInt(3, sales.getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    public List<Sales> findAllSalesByBasketId(Integer basketId) {
        String sql =    "SELECT " +

                            "pas.id, " +
                            "pas.quantity, " +
                            "pas.sold_price_smn, " +
                            "pas.wholesale_price, " +

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


                        "FROM prestige_all_sales pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE pas.basket_id = ? " +
                        "ORDER BY (pas.id)";

        return jdbcTemplate.query(sql, new Object[] {basketId}, new SalesMapper());
    }

    public void deleteSalesIds(List<Integer> salesIds) {
        String sql =    "DELETE FROM prestige_all_sales WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, salesIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return salesIds.size();
            }
        });
    }

    public Map<Integer, BigDecimal> findRecommendedPrice(Set<Integer> productIds, SalesBasket basket) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "SELECT " +
                            "pas.common_product_id, " +
                            "SUM(COALESCE(pas.quantity, 0) * COALESCE(pas.sold_price_smn, 0)) / " +
                                "NULLIF(SUM(pas.quantity), 0) " + // в случае если pas.quantity = 0, результат будет null
                        "FROM prestige_all_sales pas " +
                        "JOIN prestige_sales_baskets psb ON psb.id = pas.basket_id " +
                        "WHERE " +
                            "pas.common_product_id IN (" + inClause + ") AND " +
                            "psb.created_date_time >= ( " +
                                "SELECT created_date_time - INTERVAL '2 years' " +
                                "FROM prestige_sales_baskets " +
                                "WHERE id = ? " +
                            ") AND " +
                            "psb.created_date_time < (" +
                                "SELECT created_date_time " +
                                "FROM prestige_sales_baskets " +
                                "WHERE id = ? " +
                            ") AND " +
                            "psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                        "GROUP BY (pas.common_product_id)";

        List<Object> bindings = new ArrayList();
        bindings.addAll(productIds);
        bindings.add(basket.getId());
        bindings.add(basket.getId());

        return jdbcTemplate.query(sql, bindings.toArray(), rs -> {
            Map<Integer, BigDecimal> resultMap = new HashMap<>();

            while (rs.next()) {
                int productId = rs.getInt(1);
                BigDecimal recommendedPrice = rs.getBigDecimal(2);

                resultMap.put(productId, recommendedPrice != null ? recommendedPrice.setScale(2, RoundingMode.HALF_UP) : null);
            }

            for (Integer id : productIds) {
                resultMap.putIfAbsent(id, null);
            }
            return resultMap;
        });
    }
}
