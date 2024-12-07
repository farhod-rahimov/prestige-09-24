package com.prestige.dao.mappers;

import com.prestige.models.Supplier;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplierMapper implements RowMapper<Supplier> {

    @Override
    public Supplier mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Supplier(rs.getInt(1), rs.getString(2),
                rs.getString(3), rs.getString(4));
    }
}
