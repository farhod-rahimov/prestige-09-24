package com.prestige.dao;

import com.prestige.dao.mappers.ShowAllBasketsMapper;
import com.prestige.models.Basket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PrestigeSalesDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public PrestigeSalesDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Basket> showAllBaskets() {
        return jdbcTemplate.query(

            "SELECT " +

                    "prestige_baskets.id, " +
                    "prestige_baskets.created_date_time, " +
                    "prestige_baskets.last_changed_date_time, " +
                    "prestige_clients.id, " +
                    "prestige_clients.name, " +
                    "prestige_clients.phone_number, " +
                    "sum(prestige_all_sales.quantity * prestige_all_sales.sold_price_smn) " +

                "FROM prestige_baskets " +

                "JOIN prestige_all_sales ON prestige_baskets.id = prestige_all_sales.basket_id " +
                "JOIN prestige_clients ON prestige_baskets.client_id = prestige_clients.id " +

                "GROUP BY " +
                    "prestige_baskets.id, " +
                    "prestige_clients.id"

                , new ShowAllBasketsMapper()
        );
    }
}
