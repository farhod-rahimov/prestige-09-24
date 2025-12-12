package com.prestige.dao.abstracts;

import com.prestige.dao.mappers.ClientMapper;
import com.prestige.models.Client;
import com.prestige.service.Utils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractClientsDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    private final Utils utils;

    public AbstractClientsDAO(JdbcTemplate jdbcTemplate, String tableName, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.tableName = tableName;
        this.utils = utils;
    }

    public List<Client> findAllClients() {
        String sql = String.format(
                        "SELECT pc.id, pc.name, pc.phone_number " +
                        "FROM %s pc " +
                        "ORDER BY (pc.name, pc.phone_number)", tableName);

        return jdbcTemplate.query(sql, new ClientMapper());
    }

    public boolean checkIfClientExists(Client client) {
        String sql = String.format(
                            "SELECT EXISTS (" +
                                    "SELECT 1 FROM %s WHERE name = ? AND phone_number = ?", tableName);

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
            String sql = String.format(
                            "INSERT INTO %s (name, phone_number) " +
                            "VALUES (?, ?) RETURNING id", tableName);

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
        String sql = String.format(
                        "SELECT pc.id, pc.name, pc.phone_number " +
                        "FROM %s pc " +
                        "WHERE pc.id = ?", tableName);

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {clientId}, new ClientMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateClient(Client client) {
        String sql = String.format(
                        "UPDATE %s SET name = ?, phone_number = ? " +
                        "WHERE id = ?", tableName);

        Object[] bindings = new Object[]{client.getName(), client.getPhoneNumber(), client.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteClientById(Integer clientId) {
        String sql = String.format("DELETE FROM %s WHERE id = ?", tableName);

        jdbcTemplate.update(sql, new Object[]{clientId});
    }

    public List<String> findUniqueClientNames() {
        String sql = String.format(
                        "SELECT pc.name " +
                        "FROM %s pc " +
                        "GROUP BY (pc.name) " +
                        "ORDER BY (pc.name)", tableName);

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findPhoneNumbersByClientName(String clientName) {
        String sql = String.format(
                        "SELECT pc.phone_number " +
                        "FROM %s pc " +
                        "WHERE pc.name = ? " +
                        "ORDER BY (pc.phone_number)", tableName);

        return jdbcTemplate.queryForList(sql, new Object[] {clientName}, String.class);
    }

    public Integer findClientId(Client client) {
        String sql = String.format(
                        "SELECT id " +
                        "FROM %s pc " +
                        "WHERE " +
                            "pc.name = ? AND " +
                            "pc.phone_number = ? ", tableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {client.getName(), client.getPhoneNumber()}, Integer.class);
    }
}
