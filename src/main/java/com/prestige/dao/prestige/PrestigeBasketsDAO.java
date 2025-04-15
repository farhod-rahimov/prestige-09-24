package com.prestige.dao.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.BasketMapper;
import com.prestige.models.Basket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component
public class PrestigeBasketsDAO {

    private final JdbcTemplate jdbcTemplate;

    private Utils utils;

    @Autowired
    public PrestigeBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Basket> findAllBaskets() {
        String sql =    "SELECT " +
                            "pb.id, pb.created_date_time, pb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "pb.total_sum, pb.notes " +
                        "FROM prestige_baskets pb " +
                        "JOIN prestige_clients pc on pb.client_id = pc.id " +
                        "ORDER BY (pb.id)";

        return jdbcTemplate.query(sql, new BasketMapper());
    }

    public Basket findBasketById(Integer basketId) {
        String sql =    "SELECT " +
                            "pb.id, pb.created_date_time, pb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "pb.total_sum, pb.notes " +
                        "FROM prestige_baskets pb " +
                        "JOIN prestige_clients pc on pb.client_id = pc.id " +
                        "WHERE pb.id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[] {basketId}, new BasketMapper());
    }

    public void saveNewPrestigeBasket(Basket basket) {
        String sql =    "INSERT INTO prestige_baskets (" +

                            "created_date_time, " +
                            "last_changed_date_time, " +
                            "client_id, " +
                            "total_sum" +
                        ") VALUES (?, ?, ?, ?) RETURNING id";

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

            return ps;
        }, keyHolder);

        basket.setId(keyHolder.getKey().intValue());
    }
}
