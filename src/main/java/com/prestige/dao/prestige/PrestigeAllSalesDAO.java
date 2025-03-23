package com.prestige.dao.prestige;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrestigeAllSalesDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeAllSalesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
