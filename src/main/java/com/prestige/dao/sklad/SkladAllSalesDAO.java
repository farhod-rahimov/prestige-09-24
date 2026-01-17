package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladAllSalesDAO extends AbstractAllSalesDAO {

    @Autowired
    public SkladAllSalesDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "sklad_all_sales");
    }
}
