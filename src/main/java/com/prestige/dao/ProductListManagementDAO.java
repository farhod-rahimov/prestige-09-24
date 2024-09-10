package com.prestige.dao;

import com.prestige.dao.mappers.*;
import com.prestige.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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

                    "prestige_clients.id, " +
                    "prestige_clients.name, " +
                    "prestige_clients.phone_number " +

                "FROM prestige_clients " +

                "ORDER BY (prestige_clients.name)", new ShowAllClientsMapper());
    }

    public boolean checkIfClientExists(Client client) {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_clients WHERE name = ? AND phone_number = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, client.getName(), client.getPhoneNumber());
    }

    public boolean saveNewClient(Client newClient) {
        newClient.setName(utils.capitalizeString(newClient.getName()));

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

    public List<ProductName> showAllProductNames() {
        return jdbcTemplate.query(
                "SELECT " +

                        "common_product_names.id, " +
                        "common_product_names.product_name " +

                        "FROM common_product_names " +

                        "ORDER BY (common_product_names.product_name)",
                new ShowAllProductNamesMapper());
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

                "order by (common_product_names.product_name)",
                new ShowAllProductsMapper());
    }

    public boolean checkIfProductExists(Product product) {
        String rawSql = "SELECT EXISTS (SELECT 1 FROM common_products WHERE " +
                "common_products.product_name_id = %d AND " +
                "common_products.article_number = '%s' AND " +
                "common_products.brand_name_id = %d)";

        String sql = String.format(rawSql, product.getProductName().getId(),
                product.getArticleNumber(), product.getBrand().getId());

        return jdbcTemplate.queryForObject(sql, Boolean.class);
    }

    public boolean saveNewProduct(Product product) {
        utils.formatProductAttributes(product);

        if (!checkIfProductExists(product)) {
            String rawSql = "INSERT INTO common_products (product_name_id, car_model, article_number, brand_name_id, notes) " +
                    "VALUES (%d, '%s', '%s', %d, '%s')";

            String sql = String.format(rawSql, product.getProductName().getId(), product.getCarModel(),
                    product.getArticleNumber(), product.getBrand().getId(), product.getNotes());

            jdbcTemplate.update(sql);

            return true;
        }
        return false;
    }
}
