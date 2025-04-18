package com.prestige.dao.mappers;

import com.prestige.models.Basket;
import com.prestige.models.Client;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BasketMapper implements RowMapper<Basket> {

    @Override
    public Basket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Client client = new Client(rs.getInt(4), rs.getString(5), rs.getString(6));
        Basket.EStatus status = Basket.EStatus.fromIntValue(rs.getInt(9));

        return new Basket(rs.getInt(1), rs.getTimestamp(2), rs.getTimestamp(3),
                            client, rs.getBigDecimal(7).setScale(2, RoundingMode.HALF_UP), rs.getString(8), null, status);
    }
}
