package com.prestige.dao.mappers;

import com.prestige.models.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SkladSupplyMapper  implements RowMapper<SkladSupply> {

    @Override
    public SkladSupply mapRow(ResultSet rs, int rowNum) throws SQLException {

        SkladSupply supply = new SkladSupply();

        supply.setId(rs.getInt(1));
        supply.setOrderedQuantity(rs.getInt(2));
        supply.setReceivedQuantity(rs.getInt(3));
        supply.setPriceDollar(rs.getBigDecimal(4).setScale(2, RoundingMode.HALF_UP));
        supply.setPriceSupplierCurrency(rs.getBigDecimal(5).setScale(2, RoundingMode.HALF_UP));
        supply.setImportCostPercentage(rs.getBigDecimal(6).setScale(2, RoundingMode.HALF_UP));
        supply.setNotes(rs.getString(7));

        Product product = new Product();
        product.setId(rs.getInt(8));
        product.setProductName(new ProductName(rs.getInt(9), rs.getString(10)));
        product.setCarModel(rs.getString(11));
        product.setEngineModel(rs.getString(12));
        product.setOemArticleNumber(rs.getString(13));
        product.setCrossArticleNumber(rs.getString(14));
        product.setBrand(new Brand(rs.getInt(15), rs.getString(16)));
        product.setNotes(rs.getString(17));
        product.setEquivalentProductInfo(new EquivalentProductInfo(rs.getInt(18), rs.getInt(19), rs.getInt(20)));

        supply.setProduct(product);

        return supply;
    }
}
