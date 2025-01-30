package com.prestige.dao.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.ClientMapper;
import com.prestige.models.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PrestigeClientsDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils;

    @Autowired
    public PrestigeClientsDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Client> findAllClients() {
        String sql =    "SELECT pc.id, pc.name, pc.phone_number " +
                        "FROM prestige_clients pc " +
                        "ORDER BY (pc.name, pc.phone_number)";

        return jdbcTemplate.query(sql, new ClientMapper());
    }

    public boolean checkIfClientExists(Client client) {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_clients WHERE name = ? AND phone_number = ?";

        List<Object> bindings = new ArrayList();
        bindings.addAll(Arrays.asList(client.getName(), client.getPhoneNumber()));

        if (client.getId() != null) {
            sql += " AND id != ?";
            bindings.add(client.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public boolean saveNewClient(Client newClient) {
        newClient.setName(utils.capitalizeString(newClient.getName()));
        newClient.setPhoneNumber(newClient.getPhoneNumber().replaceAll("[- ]", "")); // удалить все "-" и " "

        if (!checkIfClientExists(newClient)) {
            String sql =    "INSERT INTO prestige_clients (name, phone_number) " +
                            "VALUES (?, ?) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, newClient.getName());
                ps.setString(2, newClient.getPhoneNumber());

                return ps;
            }, keyHolder);

            newClient.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public Client findClientById(Integer clientId) {
        String sql =    "SELECT pc.id, pc.name, pc.phone_number " +
                        "FROM prestige_clients pc " +
                        "WHERE pc.id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {clientId}, new ClientMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateClient(Client client) {
        String sql =    "UPDATE prestige_clients SET name = ?, phone_number = ? " +
                        "WHERE id = ?";

        Object[] bindings = new Object[]{client.getName(), client.getPhoneNumber(), client.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteClientById(Integer clientId) {
        String sql = "DELETE from prestige_clients WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{clientId});
    }

    public List<String> findUniqueClientNames() {
        String sql =    "SELECT pc.name " +
                        "FROM prestige_clients pc " +
                        "GROUP BY (pc.name) " +
                        "ORDER BY (pc.name)";

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findPhoneNumbersByClientName(String clientName) {
        String sql =    "SELECT pc.phone_number " +
                        "FROM prestige_clients pc " +
                        "WHERE pc.name = ? " +
                        "ORDER BY (pc.phone_number)";

        return jdbcTemplate.queryForList(sql, new Object[] {clientName}, String.class);
    }
}
