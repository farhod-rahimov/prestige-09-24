package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplyMapper implements RowMapper<Supply> {

    @Override
    public Supply mapRow(ResultSet rs, int rowNum) throws SQLException {

        Supply supply = new Supply();

        supply.setId(rs.getInt(1));
        supply.setQuantity(rs.getInt(2));
        supply.setPriceDollar(rs.getBigDecimal(3));

        ProductName productName = new ProductName(rs.getInt(5), rs.getString(6));
        Brand brand = new Brand(rs.getInt(11), rs.getString(12));
        EquivalentProductInfo equivalentProductInfo = new EquivalentProductInfo(rs.getInt(14), rs.getInt(15), rs.getInt(16));

        Product product = new Product(rs.getInt(4), productName, rs.getString(7),
                                    rs.getString(8), rs.getString(9),
                                    rs.getString(10), brand, rs.getString(13), equivalentProductInfo);

        supply.setProduct(product);

        return supply;
    }
}
