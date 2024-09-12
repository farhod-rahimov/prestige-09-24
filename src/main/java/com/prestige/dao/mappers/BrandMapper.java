package com.prestige.dao.mappers;

import com.prestige.models.Basket;
import com.prestige.models.Brand;
import com.prestige.models.Client;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BrandMapper implements RowMapper<Brand> {

    @Override
    public Brand mapRow(ResultSet rs, int rowNum) throws SQLException {

        return new Brand(rs.getInt(1), rs.getString(2));
    }
}
