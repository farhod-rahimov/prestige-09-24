package com.prestige.dao.common;

import com.prestige.dao.Utils;
import com.prestige.dao.mappers.BrandMapper;
import com.prestige.models.Brand;
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
public class CommonBrandNamesDAO {

    private final JdbcTemplate jdbcTemplate;

    private final Utils utils;

    @Autowired
    public CommonBrandNamesDAO(JdbcTemplate jdbcTemplate, Utils utils) {
        this.jdbcTemplate = jdbcTemplate;
        this.utils = utils;
    }

    public List<Brand> findAllBrands() {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_brand_names cbn " +
                        "ORDER BY (cbn.brand_name)";

        return jdbcTemplate.query(sql, new BrandMapper());
    }

    public boolean checkIfBrandExists(Brand brand) {
        String sql = "SELECT EXISTS (SELECT 1 FROM common_brand_names WHERE brand_name = ?";

        List<Object> bindings = new ArrayList();
        bindings.add(brand.getBrandName());

        if (brand.getId() != null) {
            sql += " AND id != ?";
            bindings.add(brand.getId());
        }
        sql += ")";

        return jdbcTemplate.queryForObject(sql, bindings.toArray(), Boolean.class);
    }

    public boolean saveNewBrand(Brand brand) {
        brand.setBrandName(utils.toUpperCase(brand.getBrandName()));

        if (!checkIfBrandExists(brand)) {
            String sql =    "INSERT INTO common_brand_names (brand_name) " +
                            "VALUES (?) RETURNING id";

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, brand.getBrandName());

                return ps;
            }, keyHolder);

            brand.setId(keyHolder.getKey().intValue());
            return true;
        }
        return false;
    }

    public Brand findBrandById(Integer brandId) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_brand_names cbn " +
                        "WHERE id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {brandId}, new BrandMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public Brand findBrandByBrandName(String brandName) {
        String sql =    "SELECT cbn.id, cbn.brand_name " +
                        "FROM common_brand_names cbn " +
                        "WHERE brand_name = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[] {brandName}, new BrandMapper());
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public void updateBrand(Brand brand) {
        brand.setBrandName(utils.toUpperCase(brand.getBrandName()));

        String sql =    "UPDATE common_brand_names SET brand_name = ? " +
                        "WHERE id = ?";

        Object[] bindings = new Object[]{brand.getBrandName(), brand.getId()};
        jdbcTemplate.update(sql, bindings);
    }

    public void deleteBrandById(Integer brandId) {
        String sql = "DELETE from common_brand_names WHERE id = ?";

        jdbcTemplate.update(sql, new Object[]{brandId});
    }
}
