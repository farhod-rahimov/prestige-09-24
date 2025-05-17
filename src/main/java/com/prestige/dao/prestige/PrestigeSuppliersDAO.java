package com.prestige.dao.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.SupplierMapper;
import com.prestige.models.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

@Component
public class PrestigeSuppliersDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils;

    @Autowired
    public PrestigeSuppliersDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Supplier> findAllSuppliers() {
        String sql =    "SELECT " +
                            "ps.id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name " +

                        "FROM prestige_suppliers ps " +
                        "ORDER BY (ps.store_name)";

        return jdbcTemplate.query(sql, new SupplierMapper());
    }

    public List<String> findUniqueSupplierStoreNames() {
        String sql =    "SELECT " +
                            "ps.store_name " +
                        "FROM prestige_suppliers ps " +
                        "GROUP BY (ps.store_name) " +
                        "ORDER BY (ps.store_name)";

        return jdbcTemplate.queryForList(sql, String.class);
    }

    public List<String> findSupplierNameByStoreName(String storeName) {
        String sql =    "SELECT " +
                            "ps.name " +
                        "FROM prestige_suppliers ps " +
                        "WHERE ps.store_name = ? " +
                        "GROUP BY (ps.name) " +
                        "ORDER BY (ps.name)";

        return jdbcTemplate.queryForList(sql, new Object[]{storeName}, String.class);
    }

    public List<String> findSupplierPhoneNumbersByStoreNameAndManagerName(String storeName, String managerName) {
        String sql =    "SELECT " +
                            "ps.phone_number " +
                        "FROM prestige_suppliers ps " +
                        "WHERE " +
                            "ps.store_name = ? AND " +
                            "ps.name = ? " +
                        "GROUP BY (ps.phone_number) " +
                        "ORDER BY (ps.phone_number)";

        return jdbcTemplate.queryForList(sql, new Object[]{storeName, managerName}, String.class);
    }

    public boolean checkIfSupplierExists(Supplier supplier) {
        String sql = "SELECT EXISTS (SELECT 1 FROM prestige_suppliers WHERE store_name = ?";

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
            String sql =    "INSERT INTO prestige_suppliers (name, phone_number, store_name) " +
                            "VALUES (?, ?, ?) RETURNING id";

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
        String sql =    "SELECT " +
                            "ps.id, " +
                            "ps.name, " +
                            "ps.phone_number, " +
                            "ps.store_name " +
                        "FROM prestige_suppliers ps " +
                        "WHERE ps.id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {storeId}, new SupplierMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateSupplier(Supplier supplier) {
        String sql =    "UPDATE prestige_suppliers SET name = ?, phone_number = ?, store_name = ? " +
                        "WHERE id = ?";

        Object[] bindings = new Object[]{supplier.getName(), supplier.getPhoneNumber(),
                                         supplier.getStoreName(), supplier.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteSupplierById(Integer storeId) {
        String sql = "DELETE FROM prestige_suppliers WHERE id = ?";

        jdbcTemplate.update(sql, storeId);
    }

    public Integer findSupplierId(Supplier supplier) {
        String sql =    "SELECT id " +
                        "FROM prestige_suppliers ps " +
                        "WHERE " +
                            "ps.name = ? AND " +
                            "ps.phone_number = ? AND " +
                            "ps.store_name = ? ";

        return jdbcTemplate.queryForObject(sql, new Object[] {supplier.getName(), supplier.getPhoneNumber(), supplier.getStoreName()}, Integer.class);
    }
}
