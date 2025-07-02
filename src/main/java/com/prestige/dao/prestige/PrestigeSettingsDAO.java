package com.prestige.dao.prestige;

import com.prestige.controllers.abstracts.AbstractSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PrestigeSettingsDAO extends AbstractSettingsDAO {

    @Autowired
    public PrestigeSettingsDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "prestige_settings");
    }
}
