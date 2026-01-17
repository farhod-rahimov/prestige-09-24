package com.prestige.dao.abstracts;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractAllSalesDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    public AbstractAllSalesDAO(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    public boolean checkIfProductExists(Integer productId) {
        String sql = String.format("SELECT EXISTS (SELECT 1 FROM %s WHERE common_product_id = ?)", tableName);

        return jdbcTemplate.queryForObject(sql, new Object[]{productId}, Boolean.class);
    }
}
