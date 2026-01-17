package com.prestige.dao.abstracts;

import com.prestige.models.*;
import com.prestige.service.Utils;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractAllSuppliesDAO<S extends BaseSupply, B extends BaseSupplyBasket> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String allSuppliesTableName;

    protected final String supplyBasketTableName;

    protected final String allSalesTableName;

    protected final String salesBasketTableName;

    protected Utils utils;

    public AbstractAllSuppliesDAO(JdbcTemplate jdbcTemplate, String allSuppliesTableName, String supplyBasketTableName,
                                  String allSalesTableName, String salesBasketTableName, Utils utils) {

        this.jdbcTemplate = jdbcTemplate;
        this.allSuppliesTableName = allSuppliesTableName;
        this.supplyBasketTableName = supplyBasketTableName;
        this.allSalesTableName = allSalesTableName;
        this.salesBasketTableName = salesBasketTableName;
        this.utils = utils;
    }

    public abstract List<S> findAllSuppliesByBasketId(Integer basketId);

    protected abstract void saveBatch(List<S> suppliesList, Integer basketId);

    public abstract void saveNewSupply(B basket, S supply);

    public abstract void updateQuantityAndPrices(List<S> suppliesList);

    protected void setSuppliesIds(List<S> suppliesList, Integer basketId) {
        List<S> allBasketSavedSupplies = findAllSuppliesByBasketId(basketId);
        Map<Integer, Integer> productIdSupplyIdMap = allBasketSavedSupplies.stream()
                .collect(Collectors.toMap(s -> s.getProduct().getId(), s -> s.getId())); // map <productId, salesId>

        for(S supply : suppliesList) {
            supply.setId(productIdSupplyIdMap.get(supply.getProduct().getId()));
        }
    }

    public void deleteSuppliesIds(List<Integer> suppliesIds) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", allSuppliesTableName);

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

    public void saveNewSuppliesList(Integer basketId, List<S> suppliesList) {
        saveBatch(suppliesList, basketId);
        setSuppliesIds(suppliesList, basketId);
    }

    public boolean checkIfProductExists(Integer productId) {
        String sql = String.format("SELECT EXISTS (SELECT 1 FROM %s WHERE common_product_id = ?)", allSuppliesTableName);

        return jdbcTemplate.queryForObject(sql, new Object[]{productId}, Boolean.class);
    }

    public Map<Integer, Integer> findStockBeforeSale(Set<Integer> productIds, BaseBasket basket) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "SELECT cp.id AS product_id, " +
                            "(" +
                                "(COALESCE( ";

                                    if (allSuppliesTableName.contains("sklad")) {
        sql +=                          "(SELECT SUM(pas1.received_quantity) ";
                                    }
                                    else {
        sql +=                          "(SELECT SUM(pas1.quantity) ";
                                    }

        sql +=                      "FROM " + allSuppliesTableName + " pas1 " +
                                    "JOIN " + supplyBasketTableName + " psb1 ON pas1.basket_id = psb1.id " +
                                    "WHERE " +
                                        "pas1.common_product_id = cp.id AND " +
                                        "psb1.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " AND " +
                                        "psb1.created_date_time < ?), 0))" +

                                " - " +

                                "(COALESCE( " +
                                    "(SELECT SUM(pas2.quantity) " +
                                    "FROM " + allSalesTableName + " pas2 " +
                                    "JOIN " + salesBasketTableName + " psb2 on pas2.basket_id = psb2.id " +
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
}
