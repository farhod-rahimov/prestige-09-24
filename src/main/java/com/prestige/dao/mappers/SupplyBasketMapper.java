package com.prestige.dao.mappers;

import com.prestige.models.Supplier;
import com.prestige.models.SupplyBasket;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplyBasketMapper implements RowMapper<SupplyBasket> {

    @Override
    public SupplyBasket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Supplier supplier = new Supplier(rs.getInt(4), rs.getString(5),
                                        rs.getString(6), rs.getString(7));

        SupplyBasket.EStatus status = SupplyBasket.EStatus.fromIntValue(rs.getInt(10));

        return new SupplyBasket(rs.getInt(1), rs.getTimestamp(2), rs.getTimestamp(3),
                                supplier, rs.getBigDecimal(8).setScale(2, RoundingMode.HALF_UP),
                                rs.getString(9), null, status, null);
    }
}
