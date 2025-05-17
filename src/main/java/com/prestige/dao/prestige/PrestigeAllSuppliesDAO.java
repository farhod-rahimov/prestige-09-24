package com.prestige.dao.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.SupplyMapper;
import com.prestige.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

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
}
