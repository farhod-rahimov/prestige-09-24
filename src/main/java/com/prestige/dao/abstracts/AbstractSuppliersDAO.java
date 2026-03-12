package com.prestige.dao.abstracts;

import com.prestige.dao.mappers.SupplierMapper;
import com.prestige.models.Supplier;
import com.prestige.utils.Utils;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public abstract class AbstractSuppliersDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String tableName;

    private final Utils utils;

    public List<Supplier> findAllSuppliers() {
        String sql = String.format(
                        "SELECT " +
                            "ps.id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name " +

                        "FROM %s ps " +
                        "ORDER BY (ps.store_name)", tableName);

        return jdbcTemplate.query(sql, new SupplierMapper());
    }

    public List<String> findUniqueSupplierStoreNames() {
        String sql = String.format(
                        "SELECT " +
                            "ps.store_name " +

                        "FROM %s ps " +
                        "GROUP BY (ps.store_name) " +
                        "ORDER BY (ps.store_name)", tableName);

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findSupplierNameByStoreName(String storeName) {
        String sql = String.format(
                        "SELECT " +
                            "ps.name " +

                        "FROM %s ps " +
                        "WHERE ps.store_name = ? " +
                        "GROUP BY (ps.name) " +
                        "ORDER BY (ps.name)", tableName);

        return jdbcTemplate.queryForList(sql, new Object[]{storeName}, String.class);
    }

    public List<String> findSupplierPhoneNumbersByStoreNameAndManagerName(String storeName, String managerName) {
        String sql = String.format(
                        "SELECT " +
                            "ps.phone_number " +

                        "FROM %s ps " +
                        "WHERE " +
                            "ps.store_name = ? AND " +
                            "ps.name = ? " +
                        "GROUP BY (ps.phone_number) " +
                        "ORDER BY (ps.phone_number)", tableName);

        return jdbcTemplate.queryForList(sql, new Object[]{storeName, managerName}, String.class);
    }

    public boolean checkIfSupplierExists(Supplier supplier) {
        String sql = String.format(
                        "SELECT EXISTS (SELECT 1 FROM %s WHERE store_name = ?", tableName);

        List<Object> bindings = new ArrayList();

        // saving only based on the store_name, this column should be unique
        bindings.add(supplier.getStoreName());

        if (supplier.getId() != null) {
            sql += " AND id != ?";
            bindings.add(supplier.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public boolean saveNewSupplier(Supplier supplier) {
        supplier.setName(utils.capitalizeString(supplier.getName()));
        supplier.setPhoneNumber(supplier.getPhoneNumber().replaceAll("[- ]", "")); // удалить все "-" и " "
        supplier.setStoreName(utils.toUpperCase(supplier.getStoreName()));

        if (!checkIfSupplierExists(supplier)) {
            String sql = String.format(
                            "INSERT INTO %s (name, phone_number, store_name) " +
                            "VALUES (?, ?, ?) RETURNING id", tableName);

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, supplier.getName());
                ps.setString(2, supplier.getPhoneNumber());
                ps.setString(3, supplier.getStoreName());

                return ps;
            }, keyHolder);

            supplier.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public Supplier findSupplierById(Integer storeId) {
        String sql = String.format(
                        "SELECT " +
                            "ps.id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name " +

                        "FROM %s ps " +
                        "WHERE ps.id = ?", tableName);

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {storeId}, new SupplierMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateSupplier(Supplier supplier) {
        String sql = String.format(
                        "UPDATE %s SET name = ?, phone_number = ?, store_name = ? " +
                        "WHERE id = ?", tableName);

        Object[] bindings = new Object[]{supplier.getName(), supplier.getPhoneNumber(),
                                         supplier.getStoreName(), supplier.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteSupplierById(Integer storeId) {
        String sql = String.format(
                        "DELETE FROM %s WHERE id = ?", tableName);

        jdbcTemplate.update(sql, storeId);
    }

    public Integer findSupplierId(Supplier supplier) {
        String sql = String.format(
                        "SELECT id " +
                        "FROM %s ps " +
                        "WHERE " +
                            "ps.name = ? AND " +
                            "ps.phone_number = ? AND " +
                            "ps.store_name = ? ", tableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {supplier.getName(), supplier.getPhoneNumber(), supplier.getStoreName()}, Integer.class);
    }
}
