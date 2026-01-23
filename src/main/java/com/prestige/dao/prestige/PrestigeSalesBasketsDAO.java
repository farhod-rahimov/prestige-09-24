package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractSalesBasketsDAO;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrestigeSalesBasketsDAO extends AbstractSalesBasketsDAO {

    @Autowired
    public PrestigeSalesBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "prestige_sales_baskets", "prestige_all_sales",
                "prestige_clients", utils);
    }
}
