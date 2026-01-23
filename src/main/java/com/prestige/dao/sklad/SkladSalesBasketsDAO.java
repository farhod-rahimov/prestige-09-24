package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractSalesBasketsDAO;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladSalesBasketsDAO extends AbstractSalesBasketsDAO {

    @Autowired
    public SkladSalesBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "sklad_sales_baskets", "sklad_all_sales",
                "sklad_clients", utils);
    }
}
