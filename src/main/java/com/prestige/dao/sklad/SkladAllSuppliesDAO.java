package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.mappers.SkladSupplyMapper;
import com.prestige.models.SkladSupply;
import com.prestige.models.SkladSupplyBasket;
import com.prestige.service.Utils;
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
public class SkladAllSuppliesDAO extends AbstractAllSuppliesDAO<SkladSupply, SkladSupplyBasket> {

    @Autowired
    public SkladAllSuppliesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "sklad_all_supplies", "sklad_supply_baskets",
                "sklad_all_sales", "sklad_sales_baskets", utils);
    }

    @Override
    public List<SkladSupply> findAllSuppliesByBasketId(Integer basketId) {

        String sql =    "SELECT " +

                            "sas.id, " +
                            "sas.ordered_quantity, " +
                            "sas.received_quantity, " +
                            "sas.price_dollar, " +
                            "sas.price_supplier_currency, " +
                            "sas.import_cost_percentage, " +
                            "sas.notes, " +

                            "cp.id, " +
                            "cpn.id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM sklad_all_supplies sas " +

                        "JOIN common_products cp ON sas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE sas.basket_id = ? " +
                        "ORDER BY (sas.id)";

        return jdbcTemplate.query(sql, new Object[] {basketId}, new SkladSupplyMapper());
    }

    @Override
    public void saveNewSupply(SkladSupplyBasket basket, SkladSupply supply) {
        String sql =    "INSERT INTO sklad_all_supplies (" +

                            "common_product_id, " +
                            "ordered_quantity, " +
                            "received_quantity, " +
                            "price_dollar, " +
                            "price_supplier_currency, " +
                            "import_cost_percentage, " +
                            "basket_id, " +
                            "notes " +
                        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, supply.getProduct().getId());
            ps.setInt(2, supply.getOrderedQuantity());
            ps.setInt(3, supply.getReceivedQuantity());
            ps.setBigDecimal(4, supply.getPriceDollar());
            ps.setBigDecimal(5, supply.getPriceSupplierCurrency());
            ps.setBigDecimal(6, supply.getImportCostPercentage());
            ps.setInt(7, basket.getId());
            ps.setString(8, supply.getNotes());

            return ps;
        }, keyHolder);

        supply.setId(keyHolder.getKey().intValue());
    }

    @Override
    protected void saveBatch(List<SkladSupply> suppliesList, Integer basketId) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<SkladSupply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "INSERT INTO sklad_all_supplies (" +

                                "common_product_id, " +
                                "ordered_quantity, " +
                                "received_quantity, " +
                                "price_dollar, " +
                                "price_supplier_currency, " +
                                "import_cost_percentage, " +
                                "basket_id, " +
                                "notes " +
                            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    SkladSupply supply = batch.get(index);

                    ps.setInt(1, supply.getProduct().getId());
                    ps.setInt(2, supply.getOrderedQuantity());
                    ps.setInt(3, supply.getReceivedQuantity());
                    ps.setBigDecimal(4, supply.getPriceDollar());
                    ps.setBigDecimal(5, supply.getPriceSupplierCurrency());
                    ps.setBigDecimal(6, supply.getImportCostPercentage());
                    ps.setInt(7, basketId);
                    ps.setString(8, supply.getNotes());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    @Override
    public void updateQuantityAndPrices(List<SkladSupply> suppliesList) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<SkladSupply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "UPDATE sklad_all_supplies " +
                            "SET " +
                                "ordered_quantity = ?, received_quantity = ?, " +
                                "price_dollar = ?, price_supplier_currency = ?, " +
                                "import_cost_percentage = ? " +

                            "WHERE id = ?";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    SkladSupply supply = batch.get(index);
                    ps.setInt(1, supply.getOrderedQuantity());
                    ps.setInt(2, supply.getReceivedQuantity());
                    ps.setBigDecimal(3, supply.getPriceDollar());
                    ps.setBigDecimal(4, supply.getPriceSupplierCurrency());
                    ps.setBigDecimal(5, supply.getImportCostPercentage());
                    ps.setInt(6, supply.getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    public void updateBasketTotalSumUsd(SkladSupplyBasket basket) {
        String sql =    "UPDATE sklad_supply_baskets SET total_sum_usd = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getTotalSumUsd(), utils.getCurrentDateAndTime(), basket.getId()});
    }

    public void updateBasketTotalSumSupplierCurrency(SkladSupplyBasket basket) {
        String sql =    "UPDATE sklad_supply_baskets SET total_sum_supplier_currency = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getTotalSumSupplierCurrency(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
