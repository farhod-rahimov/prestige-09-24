package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractClientsDAO;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladClientsDAO extends AbstractClientsDAO {

    @Autowired
    public SkladClientsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "sklad_clients", utils);
    }
}
