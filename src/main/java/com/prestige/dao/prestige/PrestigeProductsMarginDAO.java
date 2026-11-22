package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractProductsMarginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrestigeProductsMarginDAO extends AbstractProductsMarginDAO {

    @Autowired
    public PrestigeProductsMarginDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "prestige_products_margin");
    }
}
