package com.prestige.dao.mappers;

import com.prestige.models.*;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductMarginMapper extends ProductMapper {

    @Override
    public ProductWithMargin mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductWithMargin(super.mapRow(rs, rowNum), rs.getBigDecimal(14));
    }
}
