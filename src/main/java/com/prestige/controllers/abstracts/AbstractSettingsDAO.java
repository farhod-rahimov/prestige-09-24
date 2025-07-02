package com.prestige.controllers.abstracts;

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
public abstract class AbstractSettingsDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    @Autowired
    public AbstractSettingsDAO(JdbcTemplate jdbcTemplate, String tableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
    }

    public List<Settings> findAllSettings() {
        String sql = String.format(
                        "SELECT ps.id, ps.key, ps.value " +
                        "FROM %s ps " +
                        "ORDER BY (ps.key)", tableName);

        return jdbcTemplate.query(sql, new SettingsMap());
    }

    public boolean checkIfSettingsExists(Settings settings) {
        // check only key
        String sql = String.format(
                        "SELECT EXISTS (" +
                                "SELECT 1 FROM %s WHERE key = ?", tableName);

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
            String sql = String.format(
                            "INSERT INTO %s (key, value) " +
                            "VALUES (?, ?) RETURNING id", tableName);

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
        String sql = String.format(
                        "SELECT ps.id, ps.key, ps.value " +
                        "FROM %s ps " +
                        "WHERE ps.id = ?", tableName);

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {settingsId}, new SettingsMap());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateSettings(Settings settings) {
        String sql = String.format(
                        "UPDATE %s SET key = ?, value = ? " +
                        "WHERE id = ?", tableName);

        Object[] bindings = new Object[]{settings.getKey(), settings.getValue(), settings.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteSettingsById(Integer settingsId) {
        String sql = String.format(
                        "DELETE FROM %s " +
                        "WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{settingsId});
    }

    public Settings findSettingsByName(String settingsName) {
        String sql = String.format(
                        "SELECT ps.id, ps.key, ps.value " +
                        "FROM %s ps " +
                        "WHERE ps.key = ?");

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {settingsName}, new SettingsMap());
        }
        catch (DataAccessException e) {
            return null;
        }
    }
}
