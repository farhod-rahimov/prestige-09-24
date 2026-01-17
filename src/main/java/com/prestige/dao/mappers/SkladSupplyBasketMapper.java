package com.prestige.dao.mappers;

import com.prestige.models.SkladSupplyBasket;
import com.prestige.models.Supplier;
import com.prestige.models.SupplyBasket;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SkladSupplyBasketMapper implements RowMapper<SkladSupplyBasket> {

    @Override
    public SkladSupplyBasket mapRow(ResultSet rs, int rowNum) throws SQLException {

        Supplier supplier = new Supplier(rs.getInt(5), rs.getString(6),
                                         rs.getString(7), rs.getString(8));

        SupplyBasket.EStatus status = SupplyBasket.EStatus.fromIntValue(rs.getInt(14));

        return new SkladSupplyBasket(rs.getInt(1), rs.getTimestamp(3), rs.getTimestamp(4),
                                     supplier, rs.getBigDecimal(9).setScale(2, RoundingMode.HALF_UP),
                                     rs.getString(13), null, status, null, rs.getString(2),
                                     rs.getBigDecimal(10).setScale(2, RoundingMode.HALF_UP),
                                     rs.getBigDecimal(11).setScale(2, RoundingMode.HALF_UP),
                                     rs.getString(12));
    }
}
