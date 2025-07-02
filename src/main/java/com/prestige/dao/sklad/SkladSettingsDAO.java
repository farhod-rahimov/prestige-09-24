package com.prestige.dao.sklad;

import com.prestige.controllers.abstracts.AbstractSettingsDAO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SkladSettingsDAO extends AbstractSettingsDAO {

    public SkladSettingsDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "sklad_settings");
    }
}
