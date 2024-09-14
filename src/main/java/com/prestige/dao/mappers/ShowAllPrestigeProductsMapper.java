package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowAllPrestigeProductsMapper implements RowMapper<RetailProduct> {

    @Override
    public RetailProduct mapRow(ResultSet rs, int rowNum) throws SQLException {

        ProductName productName = new ProductName(null, rs.getString(1));
        Brand brand = new Brand(null, rs.getString(4));

        Product product = new Product(null, productName, rs.getString(2),
                rs.getString(3), brand, rs.getString(4));

        return new RetailProduct(null, null, product, rs.getBigDecimal(6),
                rs.getInt(7), null);
    }
}
