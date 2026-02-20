package com.prestige.dao.abstracts;

import com.prestige.dao.mappers.SalesMapper;
import com.prestige.models.*;
import com.prestige.models.filters.SalesHistoryFilter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractAllSalesDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String allSalesTableName;

    public AbstractAllSalesDAO(JdbcTemplate jdbcTemplate, String allSalesTableName) {
        this.jdbcTemplate = jdbcTemplate;
        this.allSalesTableName = allSalesTableName;
    }

    public abstract Map<Integer, BigDecimal> findRecommendedPrice(Set<Integer> productIds, SalesBasket basket);

    public boolean checkIfProductExists(Integer productId) {
        String sql = String.format("SELECT EXISTS (SELECT 1 FROM %s WHERE common_product_id = ?)", allSalesTableName);

        return jdbcTemplate.queryForObject(sql, new Object[]{productId}, Boolean.class);
    }

    public List<Sales> findAllSalesByBasketId(Integer basketId) {
        return findAllSalesByBasketIdInternal(basketId, null);
    }

    public List<Sales> findAllSalesByBasketIdWithFilter(Integer basketId, SalesHistoryFilter salesHistoryFilter) {
        return findAllSalesByBasketIdInternal(basketId, salesHistoryFilter);
    }

    private List<Sales> findAllSalesByBasketIdInternal(Integer basketId, SalesHistoryFilter salesHistoryFilter) {
        List<Object> bindings = new ArrayList();

        String sql =    "SELECT " +

                            "pas.id, " +
                            "pas.quantity, " +
                            "pas.sold_price, " +
                            "pas.wholesale_price, " +

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


                        "FROM " + allSalesTableName +" pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE pas.basket_id = ? ";

        bindings.add(basketId);

        if (salesHistoryFilter != null && salesHistoryFilter.getProduct() != null) {
            Product product = salesHistoryFilter.getProduct();

            Integer groupId = product.getEquivalentProductInfo().getGroupId();
            if (groupId != null && groupId != 0) {
                sql +=  "AND cpe.group_id = ? ";
                bindings.add(groupId);
            }

            Integer productId = product.getId();
            if (productId != null && productId != 0) {
                sql +=  "AND cp.id  = ? ";
                bindings.add(productId);
            }

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
        sql +=          "ORDER BY (pas. id)";

        return jdbcTemplate.query(sql, bindings.toArray(), new SalesMapper());
    }

    public void saveNewSalesList(SalesBasket basket, List<Sales> salesList) {
        saveBatch(salesList, basket.getId());
        setSalesIds(salesList, basket.getId());
    }

    private void saveBatch(List<Sales> salesList, Integer basketId) {
        int batchSize = 500;
        int totalSize = salesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Sales> batch = salesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "INSERT INTO " + allSalesTableName + " (" +

                                "common_product_id, " +
                                "quantity, " +
                                "sold_price, " +
                                "wholesale_price, " +
                                "basket_id" +
                            ") VALUES (?, ?, ?, ?, ?)";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Sales sales = batch.get(index);

                    ps.setInt(1, sales.getProduct().getId());
                    ps.setInt(2, sales.getQuantity());
                    ps.setBigDecimal(3, sales.getSoldPrice());
                    ps.setBigDecimal(4, sales.getWholesalePrice());
                    ps.setInt(5, basketId);
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    private void setSalesIds(List<Sales> salesList, Integer basketId) {
        List<Sales> allBasketSavedSales = findAllSalesByBasketId(basketId);
        Map<Integer, Integer> productIdSalesIdMap = allBasketSavedSales.stream()
                .collect(Collectors.toMap(s -> s.getProduct().getId(), s -> s.getId())); // map <productId, salesId>

        for(Sales sales : salesList) {
            sales.setId(productIdSalesIdMap.get(sales.getProduct().getId()));
        }
    }

    public void updateQuantityAndPrices(List<Sales> salesList) {
        int batchSize = 500;
        int totalSize = salesList.size();

        for (int i = 0; i < totalSize; i += batchSize) {
            List<Sales> batch = salesList.subList(i, Math.min(i + batchSize, totalSize));

            String sql =    "UPDATE " + allSalesTableName + " SET quantity = ?, sold_price = ? WHERE id = ?";

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int index) throws SQLException {
                    Sales sales = batch.get(index);
                    ps.setInt(1, sales.getQuantity());
                    ps.setBigDecimal(2, sales.getSoldPrice());
                    ps.setInt(3, sales.getId());
                }

                @Override
                public int getBatchSize() {
                    return batch.size();
                }
            });
        }
    }

    public void deleteSalesIds(List<Integer> salesIds) {
        String sql =    "DELETE FROM " + allSalesTableName + " WHERE id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, salesIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return salesIds.size();
            }
        });
    }

    public void saveNewSale(SalesBasket basket, Sales sales) {
        String sql =    "INSERT INTO " + allSalesTableName + " (" +

                            "common_product_id, " +
                            "quantity, " +
                            "sold_price, " +
                            "wholesale_price, " +
                            "basket_id" +
                        ") VALUES (?, ?, ?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setInt(1, sales.getProduct().getId());
            ps.setInt(2, sales.getQuantity());
            ps.setBigDecimal(3, sales.getSoldPrice());
            ps.setBigDecimal(4, sales.getWholesalePrice());
            ps.setInt(5, basket.getId());

            return ps;
        }, keyHolder);

        sales.setId(keyHolder.getKey().intValue());
    }
}
