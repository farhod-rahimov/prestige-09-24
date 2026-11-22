package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractProductsMarginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladProductsMarginDAO extends AbstractProductsMarginDAO {

    @Autowired
    public SkladProductsMarginDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "sklad_products_margin");
    }
}
