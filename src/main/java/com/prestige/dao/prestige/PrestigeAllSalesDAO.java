package com.prestige.dao.prestige;


import com.prestige.models.Basket;
import com.prestige.models.Sales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Component
public class PrestigeAllSalesDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeAllSalesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveNewPrestigeSale(Basket basket, Sales sales) {
        String sql =    "INSERT INTO prestige_all_sales (" +

                            "common_product_id, " +
                            "quantity, " +
                            "sold_price_smn, " +
                            "wholesale_price, " +
                            "basket_id" +
                ") VALUES (?, ?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, sales.getProduct().getId());
            ps.setInt(2, sales.getQuantity());
            ps.setBigDecimal(3, sales.getSoldPrice());
            ps.setBigDecimal(4, sales.getWholesalePrice());
            ps.setInt(5, basket.getId());

            return ps;
        }, keyHolder);

        sales.setId(keyHolder.getKey().intValue());
    }

    public void updateQuantityAndPrices(List<Sales> salesList) {
        String sql = "UPDATE prestige_all_sales SET quantity = ?, sold_price_smn = ? WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Sales sales = salesList.get(i);
                ps.setInt(1, sales.getQuantity());
                ps.setBigDecimal(2, sales.getSoldPrice());
                ps.setInt(3, sales.getId());
            }

            @Override
            public int getBatchSize() {
                return salesList.size();
            }
        });
    }
}
