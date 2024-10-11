package com.prestige.dao.mappers;

import com.prestige.models.Basket;
import com.prestige.models.Client;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowAllBasketsMapper implements RowMapper<Basket> {

    @Override
    public Basket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Client client = new Client(rs.getInt(4), rs.getString(5),
                rs.getString(6));

        Basket basket = new Basket();
        basket.setId(rs.getInt(1));
        basket.setCreatedDateTime(rs.getTimestamp(2));
        basket.setLastChangedDateTime(rs.getTimestamp(3));
        basket.setClient(client);
        basket.setTotalSum(rs.getBigDecimal(7));

        return basket;
    }
}
