package com.prestige.dao.mappers;

import com.prestige.models.Client;
import com.prestige.models.Settings;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SettingsMap implements RowMapper<Settings> {

    @Override
    public Settings mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Settings(rs.getInt(1), rs.getString(2), rs.getString(3));
    }
}
