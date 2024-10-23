package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class GetAllPrestigeSaleUnitsMapper implements RowMapper<RetailSaleUnit> {

    @Override
    public RetailSaleUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProductName productName = new ProductName(rs.getInt(1), rs.getString(2));
        Product product = new Product(rs.getInt(3), productName, rs.getString(4),
                rs.getString(5), new Brand(rs.getInt(6), rs.getString(7)),
                rs.getString(8));


        RetailProduct retailProduct = new RetailProduct();
        retailProduct.setProduct(product);

        return new RetailSaleUnit(retailProduct, rs.getInt(9),
                rs.getBigDecimal(10), true);
    }
}
