package com.prestige.dao;

import com.prestige.dao.mappers.ShowAllClientsMapper;
import com.prestige.models.Client;
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

    public void addNewClient() {

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

    public void saveNewClient(Client newClient) {
        jdbcTemplate.update("INSERT INTO prestige_clients (name, phone_number) VALUES(?, ?)",
                newClient.getName(), newClient.getPhoneNumber());
    }
}
