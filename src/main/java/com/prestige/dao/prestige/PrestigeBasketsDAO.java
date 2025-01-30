package com.prestige.dao.prestige;

import com.prestige.dao.mappers.BasketMapper;
import com.prestige.models.Basket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrestigeBasketsDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeBasketsDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
}
