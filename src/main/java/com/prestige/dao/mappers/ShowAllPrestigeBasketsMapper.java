package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowAllPrestigeBasketsMapper implements RowMapper<Basket> {

    @Override
    public Basket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Basket basket = new Basket();

        basket.setId(rs.getInt(1));
        basket.setClient(new Client(rs.getInt(2), rs.getString(3), rs.getString(4)));
        basket.setCreatedDateTime(rs.getTimestamp(5));
        basket.setLastChangedDateTime(rs.getTimestamp(6));
        basket.setTotalSum(rs.getBigDecimal(7));

        return basket;
    }
}
