package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
public class RetailSaleUnit {

    private RetailProduct retailProduct;

    private Integer quantity;

    private BigDecimal soldPriceSmn;

    private BigDecimal totalSumSmn;

    private Boolean isSaved;

    public RetailSaleUnit() {
        retailProduct = new RetailProduct();
        isSaved = false;
    }
}
