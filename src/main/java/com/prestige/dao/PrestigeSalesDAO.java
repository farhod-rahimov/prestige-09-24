package com.prestige.dao;

import com.prestige.models.Basket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrestigeSalesDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeSalesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
}
