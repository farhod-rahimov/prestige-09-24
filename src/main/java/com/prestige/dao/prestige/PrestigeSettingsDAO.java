package com.prestige.dao.prestige;

import com.prestige.dao.mappers.SettingsMap;
import com.prestige.models.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrestigeSettingsDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeSettingsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Settings> findAllSettings() {
        String sql =    "SELECT ps.id, ps.key, ps.value " +
                        "FROM prestige_settings ps " +
                        "ORDER BY (ps.key)";

        return jdbcTemplate.query(sql, new SettingsMap());
    }

    public boolean checkIfSettingsExists(Settings settings) {
        // check only key
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_settings WHERE key = ?";

        List<Object> bindings = new ArrayList();
        bindings.add(settings.getKey());

        if (settings.getId() != null) {
            sql += " AND id != ?";
            bindings.add(settings.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public boolean saveNewSettings(Settings newSettings) {

        if (!checkIfSettingsExists(newSettings)) {
            String sql =    "INSERT INTO prestige_settings (key, value) " +
                            "VALUES (?, ?) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, newSettings.getKey());
                ps.setString(2, newSettings.getValue());

                return ps;
            }, keyHolder);

            newSettings.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public Settings findSettingsById(Integer settingsId) {
        String sql =    "SELECT ps.id, ps.key, ps.value " +
                        "FROM prestige_settings ps " +
                        "WHERE ps.id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {settingsId}, new SettingsMap());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateSettings(Settings settings) {
        String sql =    "UPDATE prestige_settings SET key = ?, value = ? " +
                        "WHERE id = ?";

        Object[] bindings = new Object[]{settings.getKey(), settings.getValue(), settings.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteSettingsById(Integer settingsId) {
        String sql = "DELETE from prestige_settings WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{settingsId});
    }
}
