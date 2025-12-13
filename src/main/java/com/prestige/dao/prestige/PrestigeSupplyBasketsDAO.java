package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractSupplyBasketsDAO;
import com.prestige.service.Utils;
import com.prestige.dao.mappers.SupplyBasketMapper;
import com.prestige.models.SupplyBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component
public class PrestigeSupplyBasketsDAO extends AbstractSupplyBasketsDAO {

    //TODO: общие методы с SkladSupplyBasketsDAO нужно вынести в AbstractSupplyBasketsDAO

    @Autowired
    public PrestigeSupplyBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "prestige_supply_baskets", utils);
    }

    public List<SupplyBasket> findAllBaskets() {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "ps.id, ps.name, ps.phone_number, ps.store_name, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM prestige_supply_baskets psb " +
                        "JOIN prestige_suppliers ps ON ps.id = psb.supplier_id " +
                        "ORDER BY (psb.id)";

        return jdbcTemplate.query(sql, new SupplyBasketMapper());
    }

    public SupplyBasket findBasketById(Integer basketId) {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "ps.id, ps.name, ps.phone_number, ps.store_name, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM prestige_supply_baskets psb " +
                        "JOIN prestige_suppliers ps on ps.id = psb.supplier_id " +
                        "WHERE psb.id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{basketId}, new SupplyBasketMapper());
    }

    public void saveNewBasket(SupplyBasket basket) {
        String sql =    "INSERT INTO prestige_supply_baskets (" +

                            "created_date_time, " +
                            "last_changed_date_time, " +
                            "supplier_id, " +
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
            ps.setInt(3, basket.getSupplier().getId());
            ps.setBigDecimal(4, basket.getTotalSum());
            ps.setString(5, basket.getNotes());
            ps.setInt(6, basket.getStatus().getIntValue());

            return ps;
        }, keyHolder);

        basket.setId(keyHolder.getKey().intValue());
    }

    public void updateBasketStatus(SupplyBasket basket) {
        String sql =    "UPDATE prestige_supply_baskets SET status = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getStatus().getIntValue(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketNotes(SupplyBasket basket) {
        String sql =    "UPDATE prestige_supply_baskets SET notes = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getNotes(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void deleteAllEmptyBaskets() {
        String sql = "DELETE FROM prestige_supply_baskets WHERE total_sum = 0";

        jdbcTemplate.update(sql);
    }
}
