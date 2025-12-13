package com.prestige.dao.abstracts;

import com.prestige.service.Utils;
import org.springframework.jdbc.core.JdbcTemplate;

public class AbstractSupplyBasketsDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    protected final Utils utils;

    public AbstractSupplyBasketsDAO(JdbcTemplate jdbcTemplate, String tableName, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.utils = utils;
    }

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
}
