package com.prestige.dao.abstracts;

import com.prestige.service.Utils;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractSalesBasketsDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    protected final Utils utils;

    public AbstractSalesBasketsDAO(JdbcTemplate jdbcTemplate, String tableName, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.utils = utils;
    }

    public Boolean checkIfClientIdExists(Integer clientId) {
        String sql = String.format(
                        "SELECT EXISTS (" +
                            "SELECT 1 FROM %s WHERE client_id = ?)", tableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {clientId}, Boolean.class);
    }
}
