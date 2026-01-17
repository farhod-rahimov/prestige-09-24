package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.function.Function;

public class ProductMapperWrappedInSupply<S extends BaseSupply> implements RowMapper<S> {

    private final Function<Product, S> creator;

    public ProductMapperWrappedInSupply(Function<Product, S> creator) {
        this.creator = creator;
    }

    @Override
    public S mapRow(ResultSet rs, int rowNum) throws SQLException {

        ProductName productName = new ProductName(rs.getInt(2), rs.getString(3));
        Brand brand = new Brand(rs.getInt(8), rs.getString(9));
        EquivalentProductInfo equivalentProductInfo = new EquivalentProductInfo(rs.getInt(11), rs.getInt(12), rs.getInt(13));

        Product product = new Product(rs.getInt(1), productName, rs.getString(4),
                                      rs.getString(5), rs.getString(6),
                                      rs.getString(7), brand, rs.getString(10), equivalentProductInfo);

        return creator.apply(product);
    }
}
