package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RetailProductViewMapper implements RowMapper<RetailProductView> {

    @Override
    public RetailProductView mapRow(ResultSet rs, int rowNum) throws SQLException {

        ProductName productName = new ProductName(rs.getInt(2), rs.getString(3));
        Brand brand = new Brand(rs.getInt(8), rs.getString(9));
        EquivalentProductInfo equivalentProductInfo = new EquivalentProductInfo(rs.getInt(13), rs.getInt(14), rs.getInt(15));

        Product product = new Product(rs.getInt(1), productName, rs.getString(4),
                rs.getString(5), rs.getString(6), rs.getString(7),
                brand, rs.getString(10), equivalentProductInfo);



        return new RetailProductView(product, rs.getBigDecimal(11).setScale(2, RoundingMode.HALF_UP), rs.getInt(12));
    }
}
