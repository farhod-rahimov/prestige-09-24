package com.prestige.dao.mappers;

import com.prestige.models.Brand;
import com.prestige.models.Product;
import com.prestige.models.ProductName;
import com.prestige.models.Sales;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMapperWrappedInSales implements RowMapper<Sales> {

    @Override
    public Sales mapRow(ResultSet rs, int rowNum) throws SQLException {

        ProductName productName = new ProductName(rs.getInt(2), rs.getString(3));
        Brand brand = new Brand(rs.getInt(8), rs.getString(9));

        return new Sales(new Product(rs.getInt(1), productName, rs.getString(4),
                           rs.getString(5), rs.getString(6),
                           rs.getString(7), brand, rs.getString(10)));
    }
}
