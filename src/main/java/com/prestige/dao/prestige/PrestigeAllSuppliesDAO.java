package com.prestige.dao.prestige;

import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import com.prestige.models.filters.BasketFilter;
import com.prestige.models.filters.ProductFilter;
import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PrestigeAllSuppliesDAO extends AbstractAllSuppliesDAO<Supply, SupplyBasket> {

    @Autowired
    public PrestigeAllSuppliesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        super(jdbcTemplate, "prestige_all_supplies", "prestige_supply_baskets",
                "prestige_all_sales", "prestige_sales_baskets", utils);
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

    public List<RetailProductView> findAllPrestigeProductsInStock() {
        return findAllPrestigeProductsInStockInternal(null);
    }

    public List<RetailProductView> findAllPrestigeProductsInStockWIthFilter(ProductFilter productFilter) {
        return findAllPrestigeProductsInStockInternal(productFilter);
    }

    private List<RetailProductView> findAllPrestigeProductsInStockInternal(ProductFilter productFilter) {
        List<Object> bindings = new ArrayList();

        String sql =    "WITH sales_info AS (" +
                            "SELECT " +
                                "common_product_id AS id, " +
                                "SUM(quantity) AS total_quantity " +
                            "FROM prestige_all_sales pas " +
                            "JOIN prestige_sales_baskets psb ON pas.basket_id = psb.id " +
                            "WHERE psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                            "GROUP BY pas.common_product_id), " +

                        "supply_info AS (" +
                            "SELECT " +
                                "common_product_id AS id, " +
                                "SUM(quantity) as total_quantity, " +
                                "CASE " +
                                    "WHEN SUM(pas.quantity) = 0 " +
                                    "THEN 0 " +
                                    "ELSE ROUND(SUM(pas.price_dollar * pas.quantity) / SUM(pas.quantity), 2) " +
                                "END AS avg_price " +
                            "FROM prestige_all_supplies pas " +
                            "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY pas.common_product_id " +
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

    public List<Sales> findAllPrestigeProductsWrappedInSales(List<Integer> excludeProductIds) {
        String sql =    "SELECT " +

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
                        "JOIN common_products cp ON cp.id = pas.common_product_id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id ";

        if (!excludeProductIds.isEmpty()) {
            String inClause = excludeProductIds.stream().map(id -> "?").collect(Collectors.joining(", "));
            sql +=      "WHERE cp.id NOT IN ("+ inClause +") ";
        }

        sql +=          "GROUP BY   (cp.id, cpn.id, cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.id, cbn.brand_name," +
                                    "cp.notes, cpe.id, cpe.group_id, cpe.product_id) " +

                        "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name," +
                                    "cp.notes, cpe.id, cpe.group_id, cpe.product_id)";

        if (!excludeProductIds.isEmpty()) {
            return jdbcTemplate.query(sql, excludeProductIds.toArray(), new ProductMapperWrappedInSales());
        }
        return jdbcTemplate.query(sql, new ProductMapperWrappedInSales());
    }

    private List<ProductName> findAllProductNamesInternal(BasketFilter filter) {
        String sql =    "SELECT cpn.id, cpn.product_name " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +

                        "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " ";

        List<Object> bindings = new ArrayList();

        if (filter != null) {
            if (!filter.getProductName().isEmpty()) {
                sql += "AND cpn.product_name ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getProductName());
            }

            if (!filter.getCarModel().isEmpty()) {
                sql += "AND cp.car_model ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getCarModel());
            }

            if (!filter.getEngineModel().isEmpty()) {
                sql += "AND cp.engine_model ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getEngineModel());
            }

            if (!filter.getOemArticleNumber().isEmpty()) {
                sql += "AND cp.oem_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getOemArticleNumber());
            }

            if (!filter.getCrossArticleNumber().isEmpty()) {
                sql += "AND cp.cross_article_number ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getCrossArticleNumber());
            }

            if (!filter.getBrand().isEmpty()) {
                sql += "AND cbn.brand_name ILIKE ? ESCAPE '\\' ";
                bindings.add(filter.getBrand());
            }
        }

        sql +=          "GROUP BY (cpn.id) " +
                        "ORDER BY (cpn.product_name)";

        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new ProductNameMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new ProductNameMapper());
    }

    public List<ProductName> findAllProductNames() {
        return findAllProductNamesInternal(null);
    }

    public List<ProductName> findAllProductNamesWithFilter(BasketFilter filter) {
        return findAllProductNamesInternal(filter);
    }

    public List<String> findAvailableCarModels(Product product) {
        String sql =    "SELECT cp.car_model " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +

                        "WHERE cpn.id = ? " +
                        "GROUP BY (cp.car_model) " +
                        "ORDER by (cp.car_model)";

        return jdbcTemplate.queryForList(sql, new Object[]{product.getProductName().getId()}, String.class);
    }

    public List<String> findAvailableEngineModels(Product product) {
        String sql =    "SELECT cp.engine_model " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND " +
                            "cp.engine_model IS NOT NULL AND " +
                            "cp.engine_model != '' " +

                        "GROUP BY (cp.engine_model) " +
                        "ORDER by (cp.engine_model)";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableOemArticleNumbers(Product product) {
        String sql =    "SELECT cp.oem_article_number " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? AND ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "cp.engine_model = ? AND ";
            bindings.add(product.getEngineModel());
        }

        sql +=              "cp.oem_article_number IS NOT NULL AND " +
                            "cp.oem_article_number != '' " +

                            "GROUP BY (cp.oem_article_number) " +
                            "ORDER by (cp.oem_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableCrossArticleNumbers(Product product) {
        String sql =    "SELECT cp.cross_article_number " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        sql +=              "AND cp.cross_article_number IS NOT NULL " +
                            "AND cp.cross_article_number != '' " +

                            "GROUP BY (cp.cross_article_number) " +
                            "ORDER by (cp.cross_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<Brand> findAvailableBrands(Product product) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null && !product.getEngineModel().isEmpty()) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null && !product.getOemArticleNumber().isEmpty()) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        if (product.getCrossArticleNumber() != null && !product.getCrossArticleNumber().isEmpty()) {
            sql +=          "AND cp.cross_article_number = ? ";
            bindings.add(product.getCrossArticleNumber());
        }

        sql +=              "GROUP BY (cbn.id) " +
                            "ORDER by (cbn.brand_name)";

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }

    public Map<Integer, BigDecimal> findWholesalePrice(Set<Integer> productIds) {
        String jointProductIds = productIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        String sql = "WITH " +
                        "base_table AS (" +
                            "SELECT id FROM unnest(ARRAY[" + jointProductIds + "]::int4[]) AS base_table(id)), " +

                        "latest_prices AS (" +
                            "SELECT " +
                                "id, " +
                                "price_dollar AS latest_price " +

                            "FROM   (" +
                                        "SELECT " +
                                            "pas.common_product_id AS id, " +
                                            "pas.price_dollar AS price_dollar, " +
                                            "row_number() over (" +
                                                "PARTITION BY pas.common_product_id " +
                                                "ORDER BY psb.created_date_time DESC) rn " +

                                        "FROM prestige_all_supplies pas " +
                                        "JOIN prestige_supply_baskets psb ON psb.id = pas.basket_id " +
                                        "JOIN base_table bt ON bt.id = pas.common_product_id " +

                                        "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                                    ") tmp_table " +

                            "WHERE rn = 1), " +

                        "stock_data AS (" +
                            "SELECT " +
                                "pas.common_product_id AS id, " +
                                "SUM(pas.quantity) AS total_stock, " +
                                "SUM(pas.quantity * pas.price_dollar) AS total_stock_value " +

                            "FROM prestige_all_supplies pas " +
                            "JOIN prestige_supply_baskets psb ON psb.id = pas.basket_id " +
                            "JOIN base_table bt ON bt.id = pas.common_product_id " +

                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY pas.common_product_id), " +

                        "sales_data AS (" +
                            "SELECT " +
                                "pas.common_product_id AS id, " +
                                "SUM(pas.quantity) AS total_sold, " +
                                "SUM(pas.quantity * pas.wholesale_price) AS total_sold_value " +

                            "FROM prestige_all_sales pas " +
                            "JOIN prestige_sales_baskets psb ON psb.id = pas.basket_id " +
                            "JOIN base_table bt ON bt.id = pas.common_product_id " +

                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY pas.common_product_id) " +

                        "SELECT " +
                            "bt.id, " +
                            "CASE " +
                                "WHEN total_stock IS NULL " +
                                "THEN 0 " +

                                "WHEN total_stock = 0 OR (total_stock -  COALESCE(total_sold, 0) = 0) " +
                                "THEN COALESCE(lp.latest_price, 0) " +

                                "WHEN total_sold IS NULL " +
                                "THEN COALESCE(stock_data.total_stock_value / NULLIF(stock_data.total_stock, 0), 0) " +

                                "ELSE COALESCE(" +
                                        "(stock_data.total_stock_value - sales_data.total_sold_value)" +
                                            " / " +
                                        "NULLIF(stock_data.total_stock - sales_data.total_sold, 0), 0) " +
                            "END as calculated_price " +
                            "FROM base_table bt " +
                            "LEFT JOIN latest_prices lp ON lp.id = bt.id " +
                            "LEFT JOIN stock_data ON stock_data.id = bt.id " +
                            "LEFT JOIN sales_data ON sales_data.id = bt.id";

        return jdbcTemplate.query(sql, rs -> {
            Map<Integer, BigDecimal> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getInt(1), rs.getBigDecimal(2).setScale(2, RoundingMode.HALF_UP));
            }
            return result;
        });
    }

    public List<RetailProduct> findPrestigeProductSupplyListByProductId(Integer productId) {
        String sql =    "SELECT " +
                            "pas.id as pas_id, " +
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
                            "pas.price_dollar, " +
                            "pas.quantity, " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "psb.created_date_time, " + // TODO: заменить на received_date_time
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM prestige_all_supplies pas " +

                        "JOIN prestige_supply_baskets psb ON pas.basket_id = psb.id " +
                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN prestige_suppliers ps ON psb.supplier_id = ps.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE " +
                            "cp.id = ? AND " +
                            "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +

                        "ORDER BY(psb.created_date_time)";

        return jdbcTemplate.query(sql, new Object[]{productId}, new RetailProductMapper());
    }
}
