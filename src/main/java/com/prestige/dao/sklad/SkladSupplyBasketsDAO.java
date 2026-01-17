package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractSupplyBasketsDAO;
import com.prestige.dao.mappers.SkladSupplyBasketMapper;
import com.prestige.models.SkladSupplyBasket;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.util.List;

@Component
public class SkladSupplyBasketsDAO extends AbstractSupplyBasketsDAO<SkladSupplyBasket> {

    @Autowired
    public SkladSupplyBasketsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "sklad_supply_baskets", "sklad_all_supplies", utils);
    }

    @Override
    public List<SkladSupplyBasket> findAllBaskets() {
        String sql =    "SELECT " +
                            "ssb.id, ssb.basket_name, ssb.created_date_time, ssb.last_changed_date_time, " +
                            "ss.id, ss.name, ss.phone_number, ss.store_name, " +
                            "ssb.total_sum_usd, ssb.total_sum_supplier_currency, ssb.usd_supplier_currency_rate, " +
                            "ssb.currency_name, ssb.notes, ssb.status " +
                        "FROM sklad_supply_baskets ssb " +
                        "JOIN sklad_suppliers ss ON ss.id = ssb.supplier_id " +
                        "ORDER BY (ssb.id)";

        return jdbcTemplate.query(sql, new SkladSupplyBasketMapper());
    }

    @Override
    public SkladSupplyBasket findBasketById(Integer basketId) {
        String sql =    "SELECT " +
                            "ssb.id, ssb.basket_name, ssb.created_date_time, ssb.last_changed_date_time, " +
                            "ss.id, ss.name, ss.phone_number, ss.store_name, " +
                            "ssb.total_sum_usd, ssb.total_sum_supplier_currency, ssb.usd_supplier_currency_rate, " +
                            "ssb.currency_name, ssb.notes, ssb.status " +
                        "FROM sklad_supply_baskets ssb " +
                        "JOIN sklad_suppliers ss ON ss.id = ssb.supplier_id " +
                        "WHERE ssb.id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{basketId}, new SkladSupplyBasketMapper());
    }

    @Override
    public void saveNewBasket(SkladSupplyBasket basket) {
        String sql =    "INSERT INTO sklad_supply_baskets (" +

                            "basket_name, " +
                            "created_date_time, " +
                            "last_changed_date_time, " +
                            "supplier_id, " +
                            "total_sum_usd, " +
                            "total_sum_supplier_currency, " +
                            "usd_supplier_currency_rate, " +
                            "currency_name, " +
                            "notes, " +
                            "status" +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        basket.setBasketName("Новая корзина: " + basket.getSupplier().getStoreName());
        basket.setCreatedDateTime(utils.getCurrentDateAndTime());
        basket.setLastChangedDateTime(utils.getCurrentDateAndTime());
        basket.setTotalSum(new BigDecimal(0));
        basket.setTotalSumSupplierCurrency(new BigDecimal(0));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, basket.getBasketName());
            ps.setTimestamp(2, basket.getCreatedDateTime());
            ps.setTimestamp(3, basket.getLastChangedDateTime());
            ps.setInt(4, basket.getSupplier().getId());
            ps.setBigDecimal(5, basket.getTotalSum());
            ps.setBigDecimal(6, basket.getTotalSumSupplierCurrency());
            ps.setBigDecimal(7, basket.getUsdSupplierCurrencyRate());
            ps.setString(8, basket.getCurrencyName());
            ps.setString(9, basket.getNotes());
            ps.setInt(10, basket.getStatus().getIntValue());

            return ps;
        }, keyHolder);

        basket.setId(keyHolder.getKey().intValue());
    }

    public void updateBasketName(SkladSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET basket_name = ?, last_changed_date_time = ? WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{basket.getBasketName(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateUsdSupplierCurrencyRate(SkladSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET usd_supplier_currency_rate = ?, last_changed_date_time = ? WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{basket.getUsdSupplierCurrencyRate(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
