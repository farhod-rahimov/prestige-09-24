package com.prestige.dao.mappers;

import com.prestige.models.Brand;
import com.prestige.models.Product;
import com.prestige.models.ProductName;
import com.prestige.models.Sales;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SalesMapper implements RowMapper<Sales> {

    @Override
    public Sales mapRow(ResultSet rs, int rowNum) throws SQLException {
        Sales sales = new Sales();

        sales.setId(rs.getInt(1));
        sales.setQuantity(rs.getInt(2));
        sales.setSoldPrice(rs.getBigDecimal(3).setScale(2, RoundingMode.HALF_UP));
        sales.setWholesalePrice(rs.getBigDecimal(4).setScale(2, RoundingMode.HALF_UP));

        ProductName productName = new ProductName(rs.getInt(6), rs.getString(7));
        Brand brand = new Brand(rs.getInt(12), rs.getString(13));

        Product product = new Product(rs.getInt(5), productName, rs.getString(8),
                                    rs.getString(9), rs.getString(10),
                                    rs.getString(11), brand, rs.getString(14));

        sales.setProduct(product);

        return sales;
    }
}
