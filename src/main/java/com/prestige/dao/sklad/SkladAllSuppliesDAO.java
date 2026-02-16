package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.mappers.RetailProductMapper;
import com.prestige.dao.mappers.RetailProductViewMapper;
import com.prestige.dao.mappers.SkladSupplyMapper;
import com.prestige.models.*;
import com.prestige.models.filters.ProductFilter;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public List<RetailProductView> findAllSkladProductsInStock() {
        return findAllSkladProductsInStockInternal(null);
    }

    public List<RetailProductView> findAllSkladProductsInStockWIthFilter(ProductFilter productFilter) {
        return findAllSkladProductsInStockInternal(productFilter);
    }

    private List<RetailProductView> findAllSkladProductsInStockInternal(ProductFilter productFilter) {
        List<Object> bindings = new ArrayList();

        String sql =    "WITH sales_info AS (" +
                            "SELECT " +
                                "common_product_id AS id, " +
                                "SUM(quantity) AS total_quantity " +
                            "FROM sklad_all_sales sas " +
                            "JOIN sklad_sales_baskets ssb ON sas.basket_id = ssb.id " +
                            "WHERE ssb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                            "GROUP BY sas.common_product_id), " +

                        "supply_info AS (" +
                            "SELECT " +
                                "common_product_id AS id, " +
                                "SUM(received_quantity) as total_quantity, " +
                                "CASE " +
                                    "WHEN SUM(sas.received_quantity) = 0 " +
                                    "THEN 0 " +
                                    "ELSE ROUND(SUM(sas.price_dollar * sas.received_quantity) / SUM(sas.received_quantity), 2) " +
                                "END AS avg_price " +
                            "FROM sklad_all_supplies sas " +
                            "JOIN sklad_supply_baskets ssb ON sas.basket_id = ssb.id " +
                            "WHERE ssb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY sas.common_product_id " +
                        "), " +

                        "stock_table AS (" +
                            "SELECT " +
                                "cp.id, " +
                                "COALESCE(supply_info.total_quantity, 0) - COALESCE(sales_info.total_quantity, 0) AS stock " +
                            "FROM common_products cp " +
                            "LEFT JOIN sales_info ON sales_info.id = cp.id " +
                            "LEFT JOIN supply_info ON supply_info.id = cp.id" +
                        ") " +

                        "SELECT " +
                            "cp.id as cp_id, " +
                            "cpn.id as cpn_id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cbn.id, " +
                            "cbn.brand_name, " +
                            "cp.notes, " +
                            "COALESCE(supply_info.avg_price, 0) AS avg_price, " +
                            "stock_table.stock AS stock, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM common_products cp " +

                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN supply_info ON cp.id = supply_info.id " +
                        "LEFT JOIN stock_table ON cp.id = stock_table.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE 1 = 1 ";

        if (productFilter != null) {

            Integer groupId = productFilter.getEquivalentProductInfo().getGroupId();
            if (groupId != null && groupId != 0) {
                sql +=  "AND cpe.group_id = ? ";
                bindings.add(groupId);
            }

            Integer productId = productFilter.getEquivalentProductInfo().getProductId();
            if (productId != null && productId != 0) {
                sql +=  "AND cp.id  = ? ";
                bindings.add(productId);
            }

            Product product = productFilter.getProduct();
            if (product != null) {

                ProductName productName = product.getProductName();
                if (productName != null && productName.getProductName() != null &&
                        !productName.getProductName().isEmpty()) {

                    sql +=  "AND cpn.product_name ILIKE ? ESCAPE '\\' ";
                    bindings.add(productName.getProductName());
                }

                if (product.getCarModel() != null && !product.getCarModel().isEmpty()) {
                    sql +=  "AND cp.car_model ILIKE ? ESCAPE '\\' ";
                    bindings.add(product.getCarModel());
                }

                if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
                    sql +=  "AND cp.engine_model ILIKE ? ESCAPE '\\' ";
                    bindings.add(product.getEngineModel());
                }

                if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
                    sql +=  "AND cp.oem_article_number ILIKE ? ESCAPE '\\' ";
                    bindings.add(product.getOemArticleNumber());
                }

                if (product.getCrossArticleNumber() != null && !product.getCrossArticleNumber().isEmpty()) {
                    sql +=  "AND cp.cross_article_number ILIKE ? ESCAPE '\\' ";
                    bindings.add(product.getCrossArticleNumber());
                }

                Brand brand = product.getBrand();
                if (brand != null && brand.getBrandName() != null && !brand.getBrandName().isEmpty()) {
                    sql +=  "AND cbn.brand_name ILIKE ? ESCAPE '\\' ";
                    bindings.add(brand.getBrandName());
                }

                if (product.getNotes() != null && !product.getNotes().isEmpty()) {
                    sql +=  "AND cp.notes ILIKE ? ESCAPE '\\' ";
                    bindings.add(product.getNotes());
                }
            }
        }

        sql +=          "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name, cpe.group_id) ";


        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new RetailProductViewMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new RetailProductViewMapper());
    }

    public List<RetailProduct> findSkladProductSupplyListByProductId(Integer productId) {
        String sql =    "SELECT " +
                            "sas.id as pas_id, " +
                            "cp.id as cp_id, " +
                            "cbn.id as cbn_id, " +
                            "cbn.brand_name, " +
                            "cpn.id as cpn_id, " +
                            "cpn.product_name, " +
                            "cp.car_model, " +
                            "cp.engine_model, " +
                            "cp.oem_article_number, " +
                            "cp.cross_article_number, " +
                            "cp.notes, " +
                            "sas.price_dollar, " +
                            "sas.received_quantity, " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "psb.last_changed_date_time, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM sklad_all_supplies sas " +

                        "JOIN sklad_supply_baskets psb ON sas.basket_id = psb.id " +
                        "JOIN common_products cp ON sas.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN sklad_suppliers ps ON psb.supplier_id = ps.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE " +
                            "cp.id = ? AND " +
                            "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +

                        "ORDER BY(psb.last_changed_date_time)";

        return jdbcTemplate.query(sql, new Object[]{productId}, new RetailProductMapper());
    }
}
