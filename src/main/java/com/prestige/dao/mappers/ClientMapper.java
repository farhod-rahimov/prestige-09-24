package com.prestige.dao.mappers;

import com.prestige.models.Client;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ClientMapper implements RowMapper<Client> {

    @Override
    public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Client(rs.getInt(1), rs.getString(2), rs.getString(3));
    }
}
