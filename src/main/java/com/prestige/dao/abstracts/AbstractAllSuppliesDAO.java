package com.prestige.dao.abstracts;

import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import com.prestige.models.filters.BasketFilter;
import com.prestige.models.filters.ProductFilter;
import com.prestige.service.Utils;
import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractAllSuppliesDAO<S extends BaseSupply, B extends BaseSupplyBasket> {

    protected final JdbcTemplate jdbcTemplate;

    protected final String allSuppliesTableName;

    protected final String supplyBasketTableName;

    protected final String suppliersTableName;

    protected final String allSalesTableName;

    protected final String salesBasketTableName;

    protected Utils utils;

    public abstract List<S> findAllSuppliesByBasketId(Integer basketId);

    protected abstract void saveBatch(List<S> suppliesList, Integer basketId);

    public abstract void saveNewSupply(B basket, S supply);

    public abstract void updateQuantityAndPrices(List<S> suppliesList);

    protected void setSuppliesIds(List<S> suppliesList, Integer basketId) {
        List<S> allBasketSavedSupplies = findAllSuppliesByBasketId(basketId);
        Map<Integer, Integer> productIdSupplyIdMap = allBasketSavedSupplies.stream()
                .collect(Collectors.toMap(s -> s.getProduct().getId(), s -> s.getId())); // map <productId, salesId>

        for(S supply : suppliesList) {
            supply.setId(productIdSupplyIdMap.get(supply.getProduct().getId()));
        }
    }

    public void deleteSuppliesIds(List<Integer> suppliesIds) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", allSuppliesTableName);

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int index) throws SQLException {
                ps.setInt(1, suppliesIds.get(index));
            }

            @Override
            public int getBatchSize() {
                return suppliesIds.size();
            }
        });
    }

    public void saveNewSuppliesList(Integer basketId, List<S> suppliesList) {
        saveBatch(suppliesList, basketId);
        setSuppliesIds(suppliesList, basketId);
    }

    public boolean checkIfProductExists(Integer productId) {
        String sql = String.format("SELECT EXISTS (SELECT 1 FROM %s WHERE common_product_id = ?)", allSuppliesTableName);

        return jdbcTemplate.queryForObject(sql, new Object[]{productId}, Boolean.class);
    }

    public Map<Integer, Integer> findStockBeforeSale(Set<Integer> productIds, BaseBasket basket) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "SELECT cp.id AS product_id, " +
                            "(" +
                                "(COALESCE( ";

                                    if (allSuppliesTableName.contains("sklad")) {
        sql +=                          "(SELECT SUM(pas1.received_quantity) ";
                                    }
                                    else {
        sql +=                          "(SELECT SUM(pas1.quantity) ";
                                    }

        sql +=                      "FROM " + allSuppliesTableName + " pas1 " +
                                    "JOIN " + supplyBasketTableName + " psb1 ON pas1.basket_id = psb1.id " +
                                    "WHERE " +
                                        "pas1.common_product_id = cp.id AND " +
                                        "psb1.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " AND " +
                                        "psb1.created_date_time < ?), 0))" +

                                " - " +

                                "(COALESCE( " +
                                    "(SELECT SUM(pas2.quantity) " +
                                    "FROM " + allSalesTableName + " pas2 " +
                                    "JOIN " + salesBasketTableName + " psb2 on pas2.basket_id = psb2.id " +
                                    "WHERE " +
                                        "pas2.common_product_id = cp.id AND " +
                                        "psb2.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " AND " +
                                        "psb2.created_date_time < ?), 0))" +
                            ") AS stock_before_sale " +
                        "FROM common_products cp " +
                        "WHERE cp.id IN (" + inClause + ")";

        List<Object> bindings = new ArrayList();

        bindings.add(basket.getCreatedDateTime());
        bindings.add(basket.getCreatedDateTime());
        bindings.addAll(productIds);

        return jdbcTemplate.query(sql, bindings.toArray(), rs -> {
            Map<Integer, Integer> result = new HashMap<>();

            while (rs.next()) {
                result.put(rs.getInt(1), rs.getInt(2));
            }
            return result;
        });
    }

    public List<ProductName> findAllProductNames() {
        return findAllProductNamesInternal(null);
    }

    public List<ProductName> findAllProductNamesWithFilter(BasketFilter filter) {
        return findAllProductNamesInternal(filter);
    }

    private List<ProductName> findAllProductNamesInternal(BasketFilter filter) {
        String sql =    "SELECT cpn.id, cpn.product_name " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN " + supplyBasketTableName + " psb ON pas.basket_id = psb.id " +

                        "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " ";

        List<Object> bindings = new ArrayList();

        if (filter != null) {
            sql += getFilterClause(filter, bindings);
        }

        sql +=          "GROUP BY (cpn.id) " +
                        "ORDER BY (cpn.product_name)";

        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new ProductNameMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new ProductNameMapper());
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

                        "FROM " + allSuppliesTableName + " pas " +
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

                                            "FROM " + allSuppliesTableName + " pas " +
                                            "JOIN " + supplyBasketTableName + " psb ON psb.id = pas.basket_id " +
                                            "JOIN base_table bt ON bt.id = pas.common_product_id " +

                                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                                    ") tmp_table " +

                            "WHERE rn = 1), " +

                        "stock_data AS (" +
                            "SELECT " +
                                "pas.common_product_id AS id, ";

        if (allSuppliesTableName.contains("sklad")) {
            sql +=              "SUM(pas.received_quantity) AS total_stock, " +
                                "SUM(pas.received_quantity * pas.price_dollar) AS total_stock_value ";
        }
        else {
            sql +=              "SUM(pas.quantity) AS total_stock, " +
                                "SUM(pas.quantity * pas.price_dollar) AS total_stock_value ";
        }
        sql +=              "FROM " + allSuppliesTableName + " pas " +
                            "JOIN " + supplyBasketTableName + " psb ON psb.id = pas.basket_id " +
                            "JOIN base_table bt ON bt.id = pas.common_product_id " +

                            "WHERE psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +
                            "GROUP BY pas.common_product_id), " +

                        "sales_data AS (" +
                            "SELECT " +
                                "pas.common_product_id AS id, " +
                                "SUM(pas.quantity) AS total_sold, " +
                                "SUM(pas.quantity * pas.wholesale_price) AS total_sold_value " +

                            "FROM " + allSalesTableName + " pas " +
                            "JOIN " + salesBasketTableName + " psb ON psb.id = pas.basket_id " +
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

    private String getFilterClause(BasketFilter filter, List<Object> bindings) {
        String sql = "";

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

        return sql;
    }

    public List<String> findAvailableCarModels(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.car_model " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE cpn.id = ? ";

        List<Object> bindings = new ArrayList();
        bindings.add(product.getProductName().getId());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeCarModel();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.car_model) " +
                        "ORDER by (cp.car_model)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableEngineModels(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.engine_model " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.add(product.getProductName().getId());
        bindings.add(product.getCarModel());

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeEngineModel();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.engine_model) " +
                        "ORDER by (cp.engine_model)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableOemArticleNumbers(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.oem_article_number " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeOemArticleNumber();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=              "GROUP BY (cp.oem_article_number) " +
                            "ORDER by (cp.oem_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> findAvailableCrossArticleNumbers(Product product, BasketFilter filter) {
        String sql =    "SELECT cp.cross_article_number " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeCrossArticleNumber();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cp.cross_article_number) " +
                        "ORDER by (cp.cross_article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<Brand> findAvailableBrands(Product product, BasketFilter filter) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_product_names cpn ON cpn.id = cp.product_name_id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +

                        "WHERE " +
                            "cpn.id = ? AND " +
                            "cp.car_model = ? ";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(product.getProductName().getId(), product.getCarModel()));

        if (product.getEngineModel() != null) {
            sql +=          "AND cp.engine_model = ? ";
            bindings.add(product.getEngineModel());
        }

        if (product.getOemArticleNumber() != null) {
            sql +=          "AND cp.oem_article_number = ? ";
            bindings.add(product.getOemArticleNumber());
        }

        if (product.getCrossArticleNumber() != null) {
            sql +=          "AND cp.cross_article_number = ? ";
            bindings.add(product.getCrossArticleNumber());
        }

        if (filter != null) {
            BasketFilter filterCpy = new BasketFilter(filter);
            filterCpy.clearAllBeforeBrand();

            sql += getFilterClause(filterCpy, bindings);
        }

        sql +=          "GROUP BY (cbn.id) " +
                        "ORDER by (cbn.brand_name)";

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }

    public List<RetailProductView> findAllProductsInStock() {
        return findAllProductsInStockInternal(null);
    }

    public List<RetailProductView> findAllProductsInStockWIthFilter(ProductFilter productFilter) {
        return findAllProductsInStockInternal(productFilter);
    }

    private List<RetailProductView> findAllProductsInStockInternal(ProductFilter productFilter) {
        List<Object> bindings = new ArrayList();

        String quantity = allSalesTableName.contains("sklad") ? "received_quantity" : "quantity";

        String sql =    "WITH sales_info AS (" +
                            "SELECT " +
                                "common_product_id AS id, " +
                                "SUM(quantity) AS total_quantity " +
                            "FROM " + allSalesTableName + " pas " +
                            "JOIN " + salesBasketTableName + " psb ON pas.basket_id = psb.id " +
                            "WHERE psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                            "GROUP BY pas.common_product_id), " +

                            "recommended_price_info AS (" +
                                "SELECT " +
                                    "pas.common_product_id AS id, " +
                                    "SUM(COALESCE(pas.quantity, 0) * COALESCE(pas.sold_price, 0)) / " +
                                        "NULLIF(SUM(pas.quantity), 0) AS price " + // в случае если pas.quantity = 0, результат будет null
                                "FROM prestige_all_sales pas " +
                                "JOIN prestige_sales_baskets psb ON psb.id = pas.basket_id " +
                                "WHERE " +
                                    "psb.created_date_time >= NOW() - INTERVAL '2 years' AND " +
                                    "psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                                "GROUP BY pas.common_product_id" +
                            "), " +

                            "supply_info AS (" +
                                "SELECT " +
                                    "common_product_id AS id, " +
                                    "SUM(" + quantity + ") as total_quantity, " +
                                    "CASE " +
                                        "WHEN SUM(pas." + quantity + ") = 0 " +
                                        "THEN 0 " +
                                        "ELSE ROUND(SUM(pas.price_dollar * pas." + quantity + ") / SUM(pas." + quantity + "), 2) " +
                                    "END AS avg_price " +
                                "FROM " + allSuppliesTableName + " pas " +
                                "JOIN " + supplyBasketTableName + " psb ON pas.basket_id = psb.id " +
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
                                "recommended_price_info.price AS rec_price, " +
                                "stock_table.stock AS stock, " +
                                "cpe.id, " +
                                "cpe.group_id, " +
                                "cpe.product_id " +

                            "FROM common_products cp " +

                            "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                            "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                            "LEFT JOIN recommended_price_info ON cp.id = recommended_price_info.id " +
                            "LEFT JOIN supply_info ON cp.id = supply_info.id " +
                            "LEFT JOIN stock_table ON cp.id = stock_table.id " +
                            "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                            "WHERE 1 = 1 ";

        if (productFilter != null && productFilter.getProduct() != null) {
            Product product = productFilter.getProduct();

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

        sql +=          "ORDER BY   (cpn.product_name, cp.car_model, cp.engine_model, " +
                                    "cp.oem_article_number, cp.cross_article_number, cbn.brand_name, cpe.group_id) ";


        if (bindings.isEmpty()) {
            return jdbcTemplate.query(sql, new RetailProductViewMapper());
        }
        return jdbcTemplate.query(sql, bindings.toArray(), new RetailProductViewMapper());
    }

    public List<RetailProduct> findProductSupplyListByProductId(Integer productId) {
        String quantity = allSalesTableName.contains("sklad") ? "received_quantity" : "quantity";

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
                            "pas." + quantity + ", " +
                            "ps.id as psn_id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name, " +
                            "psb.last_changed_date_time, " +
                            "cpe.id, " +
                            "cpe.group_id, " +
                            "cpe.product_id " +

                        "FROM " + allSuppliesTableName + " pas " +

                        "JOIN " + supplyBasketTableName + " psb ON pas.basket_id = psb.id " +
                        "JOIN common_products cp ON pas.common_product_id = cp.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id  = cbn.id " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN " + suppliersTableName + " ps ON psb.supplier_id = ps.id " +
                        "LEFT JOIN common_product_equivalents cpe ON cp.id = cpe.product_id " +

                        "WHERE " +
                            "cp.id = ? AND " +
                            "psb.status = " + SupplyBasket.EStatus.RECEIVED.getIntValue() + " " +

                        "ORDER BY(psb.last_changed_date_time)";

        return jdbcTemplate.query(sql, new Object[]{productId}, new RetailProductMapper());
    }
}
