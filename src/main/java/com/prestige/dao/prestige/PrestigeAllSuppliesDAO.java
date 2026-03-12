package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.mappers.SupplyMapper;
import com.prestige.models.Supply;
import com.prestige.models.SupplyBasket;
import com.prestige.utils.Utils;
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
public class PrestigeAllSuppliesDAO extends AbstractAllSuppliesDAO<Supply, SupplyBasket> {

    @Autowired
    public PrestigeAllSuppliesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "prestige_all_supplies", "prestige_supply_baskets",
                "prestige_suppliers", "prestige_all_sales", "prestige_sales_baskets", utils);
    }

    @Override
    public List<Supply> findAllSuppliesByBasketId(Integer basketId) {

        String sql =    "SELECT " +

                            "pas.id, " +
                            "pas.quantity, " +
                            "pas.price_dollar, " +

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

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE pas.basket_id = ? " +
                        "ORDER BY (pas.id)";

        return jdbcTemplate.query(sql, new Object[] {basketId}, new SupplyMapper());
    }

    @Override
    public void saveNewSupply(SupplyBasket basket, Supply supply) {
        String sql =    "INSERT INTO prestige_all_supplies (" +

                            "common_product_id, " +
                            "quantity, " +
                            "price_dollar, " +
                            "basket_id" +
                        ") VALUES (?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, supply.getProduct().getId());
            ps.setInt(2, supply.getQuantity());
            ps.setBigDecimal(3, supply.getPriceDollar());
            ps.setInt(4, basket.getId());

            return ps;
        }, keyHolder);

        supply.setId(keyHolder.getKey().intValue());
    }

    @Override
    public void updateQuantityAndPrices(List<Supply> suppliesList) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Supply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "UPDATE prestige_all_supplies SET quantity = ?, price_dollar = ? WHERE id = ?";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Supply supply = batch.get(index);
                    ps.setInt(1, supply.getQuantity());
                    ps.setBigDecimal(2, supply.getPriceDollar());
                    ps.setInt(3, supply.getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    @Override
    protected void saveBatch(List<Supply> suppliesList, Integer basketId) {
        int batchSize = 500;
        int totalSize = suppliesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Supply> batch = suppliesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "INSERT INTO prestige_all_supplies (" +

                                "common_product_id, " +
                                "quantity, " +
                                "price_dollar, " +
                                "basket_id" +
                            ") VALUES (?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Supply supply = batch.get(index);

                    ps.setInt(1, supply.getProduct().getId());
                    ps.setInt(2, supply.getQuantity());
                    ps.setBigDecimal(3, supply.getPriceDollar());
                    ps.setInt(4, basketId);
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    public void updateBasketTotalSum(SupplyBasket basket) {
        String sql =    "UPDATE prestige_supply_baskets SET total_sum = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{basket.getTotalSum(), utils.getCurrentDateAndTime(), basket.getId()});
    }
}
