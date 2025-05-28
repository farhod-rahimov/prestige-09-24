package com.prestige.dao.common;

import com.prestige.service.Utils;
import com.prestige.dao.mappers.ProductNameMapper;
import com.prestige.models.ProductName;
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
public class CommonProductNamesDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils;

    @Autowired
    public CommonProductNamesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<ProductName> findAllProductNames() {
        String sql =    "SELECT cpn.id, cpn.product_name " +
                        "FROM common_product_names cpn " +
                        "ORDER BY (cpn.product_name)";

        return jdbcTemplate.query(sql, new ProductNameMapper());
    }

    public boolean checkIfProductNameExists(ProductName productName) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_product_names WHERE product_name = ?";

        List<Object> bindings = new ArrayList();
        bindings.add(productName.getProductName());

        if (productName.getId() != null) {
            sql += " AND id != ?";
            bindings.add(productName.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public boolean saveNewProductName(ProductName productName) {
        productName.setProductName(utils.capitalizeString(productName.getProductName()));

        if (!checkIfProductNameExists(productName)) {
            String sql =    "INSERT INTO common_product_names (product_name) " +
                            "VALUES (?) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, productName.getProductName());

                return ps;
            }, keyHolder);

            productName.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public ProductName findProductNameById(Integer productNameId) {
        String sql =    "SELECT cpn.id, cpn.product_name " +
                        "FROM common_product_names cpn " +
                        "WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {productNameId}, new ProductNameMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateProductName(ProductName productName) {
        productName.setProductName(utils.capitalizeString(productName.getProductName()));

        String sql =    "UPDATE common_product_names SET product_name = ? " +
                        "WHERE id = ?";

        Object[] bindings = new Object[]{productName.getProductName(), productName.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteProductNameById(Integer productNameId) {
        String sql = "DELETE FROM common_product_names WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{productNameId});
    }
}
