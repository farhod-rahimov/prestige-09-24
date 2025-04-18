package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RetailProductMapper implements RowMapper<RetailProduct> {

    @Override
    public RetailProduct mapRow(ResultSet rs, int rowNum) throws SQLException {

        Brand brand = new Brand(rs.getInt(3), rs.getString(4));
        ProductName productName = new ProductName(rs.getInt(5), rs.getString(6));
        Supplier supplier = new Supplier(rs.getInt(14), rs.getString(15),
                rs.getString(16), rs.getString(17));

        Product product = new Product(rs.getInt(2), productName, rs.getString(7), rs.getString(8),
                rs.getString(9), rs.getString(10), brand, rs.getString(11));

        return new RetailProduct(rs.getInt(1), rs.getTimestamp(18), supplier, product,
                rs.getBigDecimal(12).setScale(2, RoundingMode.HALF_UP), rs.getInt(13));
    }
}
