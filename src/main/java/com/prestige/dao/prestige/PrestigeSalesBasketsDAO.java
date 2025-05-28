package com.prestige.dao.prestige;

import com.prestige.service.Utils;
import com.prestige.dao.mappers.SalesBasketMapper;
import com.prestige.models.SalesBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component
public class PrestigeSalesBasketsDAO {

    private final JdbcTemplate jdbcTemplate;

    private Utils utils;

    @Autowired
    public PrestigeSalesBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<SalesBasket> findAllBaskets() {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM prestige_sales_baskets psb " +
                        "JOIN prestige_clients pc on psb.client_id = pc.id " +
                        "ORDER BY (psb.id)";

        return jdbcTemplate.query(sql, new SalesBasketMapper());
    }

    public SalesBasket findBasketById(Integer basketId) {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM prestige_sales_baskets psb " +
                        "JOIN prestige_clients pc on psb.client_id = pc.id " +
                        "WHERE psb.id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[] {basketId}, new SalesBasketMapper());
    }

    public void saveNewPrestigeBasket(SalesBasket basket) {
        String sql =    "INSERT INTO prestige_sales_baskets (" +

                            "created_date_time, " +
                            "last_changed_date_time, " +
                            "client_id, " +
                            "total_sum, " +
                            "notes, " +
                            "status" +
                        ") VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        basket.setCreatedDateTime(utils.getCurrentDateAndTime());
        basket.setLastChangedDateTime(utils.getCurrentDateAndTime());
        basket.setTotalSum(new BigDecimal(0));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTimestamp(1, basket.getCreatedDateTime());
            ps.setTimestamp(2, basket.getLastChangedDateTime());
            ps.setInt(3, basket.getClient().getId());
            ps.setBigDecimal(4, basket.getTotalSum());
            ps.setString(5, basket.getNotes());
            ps.setInt(6, basket.getStatus().getIntValue());

            return ps;
        }, keyHolder);

        basket.setId(keyHolder.getKey().intValue());
    }

    public void updateBasketTotalSum(SalesBasket basket) {
        String sql =    "UPDATE prestige_sales_baskets SET total_sum = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getTotalSum(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketStatus(SalesBasket basket) {
        String sql =    "UPDATE prestige_sales_baskets SET status = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getStatus().getIntValue(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketNotes(SalesBasket basket) {
        String sql =    "UPDATE prestige_sales_baskets SET notes = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getNotes(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
