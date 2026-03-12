package com.prestige.dao.abstracts;

import com.prestige.models.BaseSupplyBasket;
import com.prestige.utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

@AllArgsConstructor
public abstract class AbstractSupplyBasketsDAO<B extends BaseSupplyBasket> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String supplyBasketsTableName;

    protected final String allSuppliesTableName;

    protected final Utils utils;

    public abstract List<B> findAllBaskets();

    public abstract B findBasketById(Integer basketId);

    public abstract void saveNewBasket(B basket);

    public Boolean checkIfSupplierIdExists(Integer supplierId) {

        String sql = String.format(
                        "SELECT EXISTS (" +
                            "SELECT 1 FROM %s WHERE supplier_id = ?)", supplyBasketsTableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {supplierId}, Boolean.class);
    }

    public void deleteAllEmptyBaskets() {
        String sql = String.format(
                        "DELETE FROM %s psb " +
                        "WHERE NOT EXISTS ( " +
                            "SELECT 1 " +
                            "FROM %s pas " +
                            "WHERE pas.basket_id  = psb.id)", supplyBasketsTableName, allSuppliesTableName);

        jdbcTemplate.update(sql);
    }

    public void updateBasketNotes(BaseSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET notes = ?, last_changed_date_time = ? WHERE id = ?", supplyBasketsTableName);

        jdbcTemplate.update(sql, new Object[]{basket.getNotes(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketStatus(BaseSupplyBasket basket) {
        String sql = String.format("UPDATE %s SET status = ?, last_changed_date_time = ? WHERE id = ?", supplyBasketsTableName);

        jdbcTemplate.update(sql, new Object[]{basket.getStatus().getIntValue(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
