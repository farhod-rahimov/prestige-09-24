package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractClientsDAO;
import com.prestige.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrestigeClientsDAO extends AbstractClientsDAO {

    @Autowired
    public PrestigeClientsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "prestige_clients", utils);
    }
}
