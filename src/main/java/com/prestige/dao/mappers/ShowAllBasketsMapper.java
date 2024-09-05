package com.prestige.dao.mappers;

import com.prestige.models.Basket;
import com.prestige.models.Client;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ShowAllBasketsMapper implements RowMapper<Basket> {

    @Override
    public Basket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Client client = new Client(rs.getInt(3), rs.getString(4),
                rs.getString(5));

        Basket basket = new Basket(rs.getInt(1), rs.getTimestamp(2),
                client, rs.getBigDecimal(6));

        return basket;
    }
}
