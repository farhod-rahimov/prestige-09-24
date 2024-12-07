package com.prestige.dao.mappers;

import com.prestige.models.ProductName;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductNameMapper implements RowMapper<ProductName> {

    @Override
    public ProductName mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductName(rs.getInt(1), rs.getString(2));
    }
}
