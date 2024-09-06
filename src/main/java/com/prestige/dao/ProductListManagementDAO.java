package com.prestige.dao;

import com.prestige.dao.mappers.ShowAllBrandsMapper;
import com.prestige.dao.mappers.ShowAllClientsMapper;
import com.prestige.dao.mappers.ShowAllStoresMapper;
import com.prestige.models.Brand;
import com.prestige.models.Client;
import com.prestige.models.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductListManagementDAO {

    private final JdbcTemplate jdbcTemplate;

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

    public boolean checkIfBrandExists(Brand brand) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_brand_names WHERE brand_name = ?)";

        return jdbcTemplate.queryForObject(sql, Boolean.class, brand.getBrandName());
    }

    public boolean saveNewBrand(Brand brand) {
        if (!checkIfBrandExists(brand)) {
            jdbcTemplate.update("INSERT INTO common_brand_names (brand_name) VALUES(?)",
                    brand.getBrandName());

            return true;
        }
        return false;
    }
}
