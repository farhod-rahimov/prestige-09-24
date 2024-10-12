package com.prestige.dao;

import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import com.prestige.models.searchFilters.ClientSearchFilter;
import com.prestige.models.searchFilters.RetailProductSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
public class ProductListManagementDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils = new Utils();

    @Autowired
    public ProductListManagementDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Client> showAllClients() {
        return jdbcTemplate.query(

            "SELECT " +

                    "pc.id, " +
                    "pc.name, " +
                    "pc.phone_number " +

                "FROM prestige_clients pc " +

                "ORDER BY (pc.name)", new ShowAllClientsMapper());
    }

    public Integer getClientIdByNameAndPhoneNumber(String name, String phoneNumber) {
        String sql =    "SELECT pc.id " +
                        "FROM prestige_clients pc " +
                        "WHERE pc.name = ? AND pc.phone_number = ? ";

        return jdbcTemplate.queryForObject(sql, new Object[]{name, phoneNumber}, Integer.class);
    }

    private String buildWhereClauseForClientSearchFilter(ClientSearchFilter filter, List<Object> bindings) {

        String whereSql = "WHERE ";

        if (filter.getName() != null) {
            whereSql += "pc.name ~* ? AND ";
            bindings.add(filter.getName());
        }

        if (filter.getPhoneNumber() != null) {
            whereSql += "pc.phone_number ~* ? AND ";
            bindings.add(filter.getPhoneNumber());
        }

        return whereSql + "1 = 1 ";
    }

    private List<String> getAllClientNamesInternal(ClientSearchFilter filter) {
        List<Object> bindings = new ArrayList();

        String sql =    "SELECT pc.name " +
                        "FROM prestige_clients pc ";

        if (filter != null && filter.hasAnyFilter()) {
            sql += buildWhereClauseForClientSearchFilter(filter, bindings);
        }

        sql +=  "GROUP BY (pc.name)" +
                "ORDER BY (pc.name)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> getAllClientNames() {
        return getAllClientNamesInternal(null);
    }

    public List<String> getAllClientNames(ClientSearchFilter filter) {
        return getAllClientNamesInternal(filter);
    }

    private List<String> getAllClientPhoneNumbersByNamesInternal(String name, ClientSearchFilter filter) {
        List<Object> bindings = new ArrayList();
        ClientSearchFilter tmpFilter = new ClientSearchFilter(filter);

        tmpFilter.setName(null); // we don't need it because we have name

        String sql =    "SELECT pc.phone_number " +
                        "FROM prestige_clients pc ";

        if (tmpFilter != null && tmpFilter.hasAnyFilter()) {
            sql += buildWhereClauseForClientSearchFilter(tmpFilter, bindings);
            sql +=      "AND ";
        }
        else {
            sql +=      "WHERE ";
        }

        sql +=          "pc.name = ? ";
        bindings.add(name);

        sql +=          "ORDER BY (pc.phone_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> getAllClientPhoneNumbersByNames(String name) {
        return getAllClientPhoneNumbersByNamesInternal(name, null);
    }

    public List<String> getAllClientPhoneNumbersByNames(String name, ClientSearchFilter filter) {
        return getAllClientPhoneNumbersByNamesInternal(name, filter);
    }

    public boolean checkIfClientExists(Client client) {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_clients WHERE name = ? AND phone_number = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, client.getName(), client.getPhoneNumber());
    }

    public boolean saveNewClient(Client newClient) {
        newClient.setName(utils.capitalizeString(newClient.getName()));
        newClient.setPhoneNumber(newClient.getPhoneNumber().replaceAll("[- ]", "")); // удалить все "-" и " "

        if (!checkIfClientExists(newClient)) {
            jdbcTemplate.update("INSERT INTO prestige_clients (name, phone_number) VALUES(?, ?)",
                    newClient.getName(), newClient.getPhoneNumber());

            return true;
        }
        return false;
    }

    public List<Store> showAllStores() {
        return jdbcTemplate.query(
            "SELECT " +

                    "prestige_store_names.id, " +
                    "prestige_store_names.store_name " +

                "FROM prestige_store_names " +

                "ORDER BY (prestige_store_names.store_name)",
        new ShowAllStoresMapper());
    }

    public boolean checkIfStoreExists(Store store) {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_store_names WHERE store_name = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, store.getStoreName());
    }

    public boolean saveNewStore(Store newStore) {
        newStore.setStoreName(utils.toUpperCase(newStore.getStoreName()));

        if (!checkIfStoreExists(newStore)) {
            jdbcTemplate.update("INSERT INTO prestige_store_names (store_name) VALUES(?)",
                    newStore.getStoreName());

            return true;
        }
        return false;
    }

    public List<Brand> showAllBrands() {
        return jdbcTemplate.query(
                "SELECT " +

                        "common_brand_names.id, " +
                        "common_brand_names.brand_name " +

                        "FROM common_brand_names " +

                        "ORDER BY (common_brand_names.brand_name)",
                new ShowAllBrandsMapper());
    }

    public String getBrandNameById(int id) {
        String sql = "SELECT brand_name FROM common_brand_names WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public boolean checkIfBrandExists(Brand brand) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_brand_names WHERE brand_name = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, brand.getBrandName());
    }

    public boolean saveNewBrand(Brand brand) {
        brand.setBrandName(utils.toUpperCase(brand.getBrandName()));

        if (!checkIfBrandExists(brand)) {
            jdbcTemplate.update("INSERT INTO common_brand_names (brand_name) VALUES(?)",
                    brand.getBrandName());

            return true;
        }
        return false;
    }

    private String buildWhereClauseForBasketSearchFilter(RetailProductSearchFilter filter, List<Object> bindings) {

        String whereSql = "WHERE ";

        if (filter.getProductName() != null) {
            whereSql += "cpn.product_name ~* ? AND ";
            bindings.add(filter.getProductName());
        }

        if (filter.getCarModel() != null) {
            whereSql += "cp.car_model ~* ? AND ";
            bindings.add(filter.getCarModel());
        }

        if (filter.getArticleNumber() != null) {
            whereSql += "cp.article_number ~* ? AND ";
            bindings.add(filter.getArticleNumber());
        }

        if (filter.getBrandName() != null) {
            whereSql += "cbn.brand_name ~* ? AND ";
            bindings.add(filter.getBrandName());
        }

        return whereSql + "1 = 1 ";
    }

    public List<ProductName> getAllPrestigeProductNames(RetailProductSearchFilter filter) {
        List<Object> bindings = new ArrayList<>();

        String sql =    "SELECT cpn.id, cpn.product_name " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN prestige_products pp ON pp.product_id = cp.id ";

        if (filter != null && filter.hasAnyFilter()) {
            sql += buildWhereClauseForBasketSearchFilter(filter, bindings);
        }

        sql +=          "GROUP BY (cpn.id, cpn.product_name) " +
                        "ORDER BY (cpn.product_name)";

        return jdbcTemplate.query(sql, bindings.toArray(), new ShowAllProductNamesMapper());
    }

    public List<ProductName> showAllProductNames() {
        String sql =    "SELECT cpn.id, cpn.product_name " +
                        "FROM common_product_names cpn " +
                        "GROUP BY (cpn.id, cpn.product_name) " +
                        "ORDER BY (cpn.product_name)";

        return jdbcTemplate.query(sql, new ShowAllProductNamesMapper());
    }

    public String getProductNameById(int id) {
        String sql = "SELECT product_name FROM common_product_names WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, String.class, id);
    }

    public boolean checkIfProductNameExists(ProductName productName) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_product_names WHERE product_name = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, productName.getProductName());
    }

    public boolean saveNewProductName(ProductName productName) {
        productName.setProductName(utils.capitalizeString(productName.getProductName()));

        if (!checkIfProductNameExists(productName)) {
            jdbcTemplate.update("INSERT INTO common_product_names (product_name) VALUES(?)",
                    productName.getProductName());

            return true;
        }
        return false;
    }

    public List<Product> showAllProducts() {
        return jdbcTemplate.query(
                "SELECT " +

                    "common_products.id, " +
                    "common_product_names.id, " +
                    "common_product_names.product_name, " +
                    "common_products.car_model, " +
                    "common_products.article_number, " +
                    "common_brand_names.id, " +
                    "common_brand_names.brand_name, " +
                    "common_products.notes " +

                "FROM common_products " +

                "JOIN common_product_names ON common_products.product_name_id = common_product_names.id " +
                "JOIN common_brand_names ON common_products.brand_name_id = common_brand_names.id " +

                "ORDER BY   (common_product_names.product_name, common_products.car_model, " +
                            "common_products.article_number, common_brand_names.brand_name)",
                new ShowAllProductsMapper());
    }

    public boolean checkIfProductExists(Product product) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_products WHERE " +
                "common_products.product_name_id = ? AND " +
                "common_products.article_number = ? AND " +
                "common_products.brand_name_id = ?)";

        Object[] bindings =  new Object[] {product.getProductName().getId(), product.getArticleNumber(), product.getBrand().getId()};

        return jdbcTemplate.queryForObject(sql, bindings, Boolean.class);
    }

    public boolean saveNewProduct(Product product) {
        utils.formatProductAttributes(product);

        if (!checkIfProductExists(product)) {
            String sql = "INSERT INTO common_products (product_name_id, car_model, article_number, brand_name_id, notes) " +
                         "VALUES (?, ?, ?, ?, ?)";

            Object[] bindings =  new Object[] {product.getProductName().getId(), product.getCarModel(),
                    product.getArticleNumber(), product.getBrand().getId(), product.getNotes()};

            jdbcTemplate.update(sql, bindings);

            return true;
        }
        return false;
    }

    public List<String> getPrestigeCarModelsByProductNameId(int productNameId, RetailProductSearchFilter filter) {
        List<Object> bindings = new ArrayList<>();
        RetailProductSearchFilter tmpFilter = new RetailProductSearchFilter(filter);

        tmpFilter.setProductName(null); // we don't need it because we have productNameId

        String sql =    "SELECT cp.car_model " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN prestige_products pp ON pp.product_id = cp.id ";


        if (tmpFilter != null && tmpFilter.hasAnyFilter()) {
            sql += buildWhereClauseForBasketSearchFilter(tmpFilter, bindings);
            sql +=      "AND ";
        }
        else {
            sql +=      "WHERE ";
        }

        sql +=          "cp.product_name_id = ? ";
        bindings.add(productNameId);

        sql +=          "GROUP BY (cp.car_model) " +
                        "ORDER BY (cp.car_model)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> getCarModelsByProductNameId(int productNameId) {
        String sql =    "SELECT cp.car_model " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "WHERE cp.product_name_id = ? " +
                        "GROUP BY (cp.car_model) " +
                        "ORDER BY (cp.car_model)";

        return jdbcTemplate.queryForList(sql, new Object[]{productNameId}, String.class);
    }

    public List<String> getPrestigeArticleNumbersByProductNameIdAndCarModel(int productNameId, String carModel, RetailProductSearchFilter filter) {
        List<Object> bindings = new ArrayList<>();
        RetailProductSearchFilter tmpFilter = new RetailProductSearchFilter(filter);

        tmpFilter.setProductName(null); // we don't need it because we have productNameId
        tmpFilter.setCarModel(null); // we don't need it because we have carModel

        String sql =    "SELECT cp.article_number " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN prestige_products pp ON pp.product_id = cp.id ";

        if (tmpFilter != null && tmpFilter.hasAnyFilter()) {
            sql += buildWhereClauseForBasketSearchFilter(tmpFilter, bindings);
            sql +=  "AND ";
        }
        else {
            sql += "WHERE ";
        }

        sql +=      "cp.product_name_id = ? " +
                "AND cp.car_model = ? ";

        bindings.add(productNameId);
        bindings.add(carModel);

        sql +=          "GROUP BY (article_number) " +
                        "ORDER BY (article_number)";

        return jdbcTemplate.queryForList(sql, bindings.toArray(), String.class);
    }

    public List<String> getArticleNumbersByProductNameIdAndCarModel(int productNameId, String carModel) {
        String sql =    "SELECT cp.article_number " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "WHERE cp.product_name_id = ? " +
                        "AND cp.car_model = ? " +
                        "GROUP BY (article_number) " +
                        "ORDER BY (article_number)";

        return jdbcTemplate.queryForList(sql, new Object[]{productNameId, carModel}, String.class);
    }

    public List<Brand> getPrestigeBrandsByProductNameIdAndCarModelAndArticleNumber(int productNameId, String carModel,
                                                                                   String articleNumber, RetailProductSearchFilter filter) {

        List<Object> bindings = new ArrayList<>();
        RetailProductSearchFilter tmpFilter = new RetailProductSearchFilter(filter);

        tmpFilter.setProductName(null);
        tmpFilter.setCarModel(null);
        tmpFilter.setArticleNumber(null);

        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "JOIN prestige_products pp ON pp.product_id = cp.id ";

        if (tmpFilter.hasAnyFilter()) {
            sql += buildWhereClauseForBasketSearchFilter(tmpFilter, bindings);
            sql +=  "AND ";
        }
        else {
            sql += "WHERE ";
        }

        sql +=      "cp.product_name_id = ? " +
                "AND cp.car_model = ? " +
                "AND cp.article_number = ? ";

        sql +=  "GROUP BY (cbn.id, cbn.brand_name) " +
                "ORDER BY (cbn.brand_name)";

        bindings.add(productNameId);
        bindings.add(carModel);
        bindings.add(articleNumber);

        return jdbcTemplate.query(sql, bindings.toArray(), new BrandMapper());
    }

    public List<Brand> getBrandsByProductNameIdAndCarModelAndArticleNumber(int productNameId, String carModel, String articleNumber) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_products cp " +
                        "JOIN common_product_names cpn ON cp.product_name_id = cpn.id " +
                        "JOIN common_brand_names cbn ON cp.brand_name_id = cbn.id " +
                        "WHERE cp.product_name_id = ? " +
                        "AND cp.car_model = ? " +
                        "AND cp.article_number = ? " +
                        "GROUP BY (cbn.id, cbn.brand_name) " +
                        "ORDER BY (cbn.brand_name)";

        return jdbcTemplate.query(sql, new Object[]{productNameId, carModel, articleNumber}, new BrandMapper());
    }

    public String getStoreNameById(int id) {
        String sql = "SELECT store_name FROM prestige_store_names WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);
    }

    public void saveNewRetailProduct(RetailProduct retailProduct) {

        String sql = "INSERT INTO prestige_products (date_time, product_id, price_dollar, stock_balance, store_name_id) " +
                "VALUES (?, ?, ?, ?, ?)";


        Object[] bindings = new Object[]{new Timestamp(System.currentTimeMillis()), retailProduct.getProduct().getId(),
                                         retailProduct.getPriceDollar(), retailProduct.getStockBalance(),
                                         retailProduct.getStore().getId()};

        jdbcTemplate.update(sql, bindings);
    }

   public Integer getCommonProductId(int brandNameId, int productNameId, String carModel, String articleNumber) {
       String sql = "SELECT id FROM common_products WHERE " +
                   "brand_name_id = ? AND " +
                   "product_name_id = ? AND " +
                   "car_model = ? AND " +
                   "article_number = ?";

       return jdbcTemplate.queryForObject(sql, new Object[]{brandNameId, productNameId, carModel, articleNumber}, Integer.class);
   }

   public String getCommonProductNotesById(Integer id) {
        String sql = "SELECT notes FROM common_products WHERE id = ?";
        String notes = jdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);

        return notes != null ? notes : "";
   }

   public List<RetailProduct> showAllPrestigeProducts() {
        return jdbcTemplate.query(
                "SELECT " +
                        "common_product_names.product_name, " +
                        "common_products.car_model, " +
                        "common_products.article_number, " +
                        "common_brand_names.brand_name, " +
                        "common_products.notes, " +

                        // средневзвешенная цена
                        "ROUND(SUM(prestige_products.price_dollar * prestige_products.stock_balance) / " +
                        "SUM(prestige_products.stock_balance), 2), " +

                        "SUM(prestige_products.stock_balance) " +

                        "FROM prestige_products " +

                        "JOIN common_products ON common_products.id = prestige_products.product_id " +
                        "JOIN common_product_names ON common_product_names.id = common_products.product_name_id " +
                        "JOIN common_brand_names ON common_brand_names.id = common_products.brand_name_id " +

                        "GROUP BY(common_product_names.product_name, common_products.car_model, common_products.article_number, " +
                                 "common_brand_names.brand_name, common_products.notes) " +

                        "ORDER BY(common_product_names.product_name, common_products.car_model, " +
                                 "common_products.article_number, common_brand_names.brand_name)", new ShowAllPrestigeProductsMapper());
   }

   public void updateSettings(String key, String value) {
       jdbcTemplate.update("UPDATE prestige_settings SET settings_value = ? WHERE settings_name = ?", value, key);
   }

   public String getClientNameById(int id) {
       String sql = "SELECT name FROM prestige_clients WHERE id = ?";

       return jdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);
   }

    public String getClientPhoneNumberById(int id) {
        String sql = "SELECT phone_number FROM prestige_clients WHERE id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, String.class);
    }

    public BigDecimal getWACCPriceByProductId(Integer productId) {
        return jdbcTemplate.queryForObject(
                "SELECT " +
                        "ROUND(SUM(price_dollar * stock_balance) / SUM(stock_balance), 2) " +

                    "FROM prestige_products " +

                    "WHERE product_id = ? " +

                    "GROUP BY(product_id)", new Object[]{productId}, BigDecimal.class);
    }

    public Integer getTotalStockBalanceByProductId(Integer productId) {
        String sql = "SELECT " +
                            "COALESCE(pp.total_stock, 0) - COALESCE(pas.total_sale, 0) AS current_stock " +
                     "FROM " +
                            "(SELECT product_id, SUM(stock_balance) AS total_stock FROM prestige_products GROUP BY(product_id)) AS pp " +
                     "FULL JOIN " +
                            "(SELECT product_id, SUM(quantity) AS total_sale FROM prestige_all_sales GROUP BY(product_id)) AS pas " +
                     "ON pp.product_id = pas.product_id " +
                     "WHERE pp.product_id = ? OR pas.product_id = ?";


        return jdbcTemplate.queryForObject(sql, new Object[]{productId, productId}, Integer.class);
    }

    private void saveNewPrestigeBasket(Basket newBasket) {
        String sql = "INSERT INTO prestige_baskets (created_date_time, last_changed_date_time, client_id) " +
                     "VALUES (?, ?, ?) RETURNING id";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTimestamp(1, newBasket.getCreatedDateTime());
            ps.setTimestamp(2, newBasket.getLastChangedDateTime());
            ps.setInt(3, newBasket.getClient().getId());
            return ps;
        }, keyHolder);

        newBasket.setId(keyHolder.getKey().intValue());
    }

    public void saveNewPrestigeSaleUnit(Basket newBasket, RetailSaleUnit newPrestigeSaleUnit) {

        if (newBasket.getId() == null) {
            saveNewPrestigeBasket(newBasket);
        }

        String sql = "INSERT INTO prestige_all_sales (product_id, quantity, sold_price_smn, basket_id) " +
                     "VALUES (?, ?, ?, ?)";

        Object[] bindings = new Object[]{newPrestigeSaleUnit.getRetailProduct().getProduct().getId(),
                newPrestigeSaleUnit.getQuantity(), newPrestigeSaleUnit.getSoldPriceSmn(), newBasket.getId()};

        jdbcTemplate.update(sql, bindings);
        newPrestigeSaleUnit.setIsSaved(true);
    }

    public void updateNewPrestigeSaleUnitList(Basket newBasket, List<RetailSaleUnit> newPrestigeSaleUnitList) {
        String sql = "UPDATE prestige_all_sales SET quantity = ?, sold_price_smn = ? " +
                     "WHERE product_id = ? AND basket_id = ?";

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                RetailSaleUnit entity = newPrestigeSaleUnitList.get(i);
                ps.setInt(1, entity.getQuantity());
                ps.setBigDecimal(2, entity.getSoldPriceSmn());
                ps.setInt(3, entity.getRetailProduct().getProduct().getId());
                ps.setInt(4, newBasket.getId());
            }

            @Override
            public int getBatchSize() {
                return newPrestigeSaleUnitList.size() - 1; // because last element is new
            }
        });
    }

    public void removePrestigeSaleUnitFromBasket(RetailSaleUnit removePrestigeSaleUnit, Basket newBasket) {
        String sql = "DELETE FROM prestige_all_sales WHERE product_id = ? AND basket_id = ?";

        jdbcTemplate.update(sql, new Object[]{removePrestigeSaleUnit.getRetailProduct().getProduct().getId(), newBasket.getId()});
    }
}
