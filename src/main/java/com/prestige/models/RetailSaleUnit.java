package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class RetailSaleUnit {

    private RetailProduct retailProduct;

    private Integer quantity;

    private BigDecimal soldPriceSmn;

    private Boolean isSaved;

    public RetailSaleUnit() {
        retailProduct = new RetailProduct();
        isSaved = false;
    }

    public BigDecimal getTotalSumSmn() {

        if (quantity == null || soldPriceSmn == null) {
            return null;
        }
        return new BigDecimal(quantity).multiply(soldPriceSmn).setScale(2, RoundingMode.HALF_UP);
    }
}
