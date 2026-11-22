package com.prestige.dao.abstracts;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractProductsMarginDAO {

    private final JdbcTemplate jdbcTemplate;

    private final String tableName;

    public void upsertBatch(Map<Integer, BigDecimal> marginsMap) {
        int batchSize = 500;
        int totalSize = marginsMap.size();

        List<Map.Entry<Integer, BigDecimal>> entries = new ArrayList<>(marginsMap.entrySet());

        String sql =    "INSERT INTO " + tableName + " (common_product_id, margin_percentage) " +
                        "VALUES (?, ?) " +
                        "ON CONFLICT (common_product_id) " +
                        "DO UPDATE SET margin_percentage = EXCLUDED.margin_percentage";

        jdbcTemplate.batchUpdate(
                sql,
                entries,
                batchSize,
                (ps, entry) -> {
                    Integer id = entry.getKey();
                    BigDecimal margin = entry.getValue();

                    ps.setInt(1, id);
                    ps.setBigDecimal(2, margin);
                });

    }

    public Map<Integer, BigDecimal> findAllMarginsByProductIds(Set<Integer> productIds) {
        String jointProductIds = productIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = "WITH " +
                        "base_table AS (" +
                        "SELECT id FROM unnest(ARRAY[" + jointProductIds + "]::int4[]) AS base_table(id)) " +

                        "SELECT " +
                            "bt.id, " +
                            "mt.margin_percentage " +
                        "FROM base_table bt " +
                        "LEFT JOIN " + tableName + " mt ON bt.id = mt.common_product_id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, BigDecimal> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getInt(1), rs.getBigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            }
            return result;
        });
    }
}
