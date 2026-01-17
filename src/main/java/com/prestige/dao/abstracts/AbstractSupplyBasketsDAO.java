package com.prestige.dao.abstracts;

import com.prestige.models.BaseSupplyBasket;
import com.prestige.service.Utils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public abstract class AbstractSupplyBasketsDAO<B extends BaseSupplyBasket> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    protected final String suppliesTableName;

    protected final Utils utils;

    public AbstractSupplyBasketsDAO(JdbcTemplate jdbcTemplate, String tableName, String suppliesTableName, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.suppliesTableName = suppliesTableName;
        this.utils = utils;
    }

    public abstract List<B> findAllBaskets();

    public abstract B findBasketById(Integer basketId);

    public abstract void saveNewBasket(B basket);

    public Boolean checkIfSupplierIdExists(Integer supplierId) {

        if (tableName.equals("sklad_supply_baskets")) {
            // TODO: sklad_supply_baskets should be implemented; and remove this IF condition
            return false;
        }

        String sql = String.format(
                        "SELECT EXISTS (" +
                            "SELECT 1 FROM %s WHERE supplier_id = ?)", tableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {supplierId}, Boolean.class);
    }

    public void deleteAllEmptyBaskets() {
        String sql = String.format(
                        "DELETE FROM %s psb " +
                        "WHERE NOT EXISTS ( " +
                            "SELECT 1 " +
                            "FROM %s pas " +
                            "WHERE pas.basket_id  = psb.id)", tableName, suppliesTableName);

        jdbcTemplate.update(sql);
    }

    public void updateBasketNotes(BaseSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET notes = ?, last_changed_date_time = ? WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{basket.getNotes(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketStatus(BaseSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET status = ?, last_changed_date_time = ? WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{basket.getStatus().getIntValue(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
