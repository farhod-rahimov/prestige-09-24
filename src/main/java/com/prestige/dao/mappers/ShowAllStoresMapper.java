package com.prestige.dao.mappers;

import com.prestige.models.Client;
import com.prestige.models.Store;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowAllStoresMapper implements RowMapper<Store> {

    @Override
    public Store mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Store(rs.getInt(1), rs.getString(2));
    }
}
