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

        Basket basket = new Basket(rs.getInt(1), rs.getTimestamp(2),
                                    rs.getTimestamp(3), client, rs.getBigDecimal(7), null);

        return basket;
    }
}
