package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractSuppliersDAO;
import com.prestige.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladSuppliersDAO extends AbstractSuppliersDAO {

    @Autowired
    public SkladSuppliersDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "sklad_suppliers", utils);
    }
}
