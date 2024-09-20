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

    private Basket basket;

    private RetailProduct retailProduct;

    private Integer quantity;

    private BigDecimal soldPriceSmn;

    private BigDecimal totalSumSmn;

    public RetailSaleUnit() {
        basket = new Basket();
        basket.setCreatedDateTime(new Timestamp(System.currentTimeMillis()));
        basket.setLastChangedDateTime(basket.getCreatedDateTime());

        retailProduct = new RetailProduct();
    }
}
