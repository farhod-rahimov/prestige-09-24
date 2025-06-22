package com.prestige.dao.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CommonProductEquivalentsDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public CommonProductEquivalentsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private Integer getGroupIdByProductIds(List<Integer> equivalentProductIds) {
        String inClause = equivalentProductIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "SELECT group_id " +
                        "FROM common_product_equivalents " +
                            "WHERE product_id IN ("+ inClause +") " +
                        "GROUP BY group_id";

        try {
            return jdbcTemplate.queryForObject(sql, equivalentProductIds.toArray(), Integer.class);
        }
        catch (EmptyResultDataAccessException e) {
            return null; // no group created yet
        }
    }

    private Integer saveNewGroupId(Integer productId) {
        String sql =    "INSERT INTO common_product_equivalents (product_id) VALUES (?) " +
                        "RETURNING group_id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"group_id"});
            ps.setInt(1, productId);

            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    private void saveEquivalentProductIds(List<Integer> equivalentProductIds, Integer groupId) {
        String sql =    "INSERT INTO common_product_equivalents (group_id, product_id) VALUES (?, ?) " +
                        "ON CONFLICT (group_id, product_id) DO NOTHING";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, groupId);
                ps.setInt(2, equivalentProductIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return equivalentProductIds.size();
            }
        });
    }

    private void deleteEquivalentProductIds(List<Integer> unmarkProductIds) {
        String sql =    "DELETE FROM common_product_equivalents WHERE product_id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, unmarkProductIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return unmarkProductIds.size();
            }
        });
    }

    public void markAsSame(List<Integer> equivalentProductIds) {

        Integer groupId = getGroupIdByProductIds(equivalentProductIds);

        if (groupId == null) {
            groupId = saveNewGroupId(equivalentProductIds.get(0));
            equivalentProductIds = equivalentProductIds.subList(1, equivalentProductIds.size());
        }
        saveEquivalentProductIds(equivalentProductIds, groupId);
    }

    private void deleteIfSingleLeft() {
        String sql =    "DELETE FROM common_product_equivalents " +
                        "WHERE group_id IN ( " +
                            "SELECT group_id " +
                            "FROM common_product_equivalents " +
                            "GROUP BY group_id " +
                            "HAVING COUNT(*) = 1)";

        jdbcTemplate.update(sql);
    }

    public void unmarkAsSame(List<Integer> unmarkProductIds) {

        deleteEquivalentProductIds(unmarkProductIds);
        deleteIfSingleLeft();
    }
}
