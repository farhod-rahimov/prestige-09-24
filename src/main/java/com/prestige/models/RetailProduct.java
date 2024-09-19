package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;


@Getter
@Setter
@AllArgsConstructor
public class RetailProduct {

    private Integer id;

    private Timestamp dateTime;

    private Product product;

    private BigDecimal priceDollar;

    private Integer stockBalance;

    private Store store;

    public RetailProduct() {
        product = new Product();
        product.setProductName(new ProductName());
    }

    public BigDecimal getConvertedPriceToSmn(BigDecimal magicValue, BigDecimal exchangeRate) {
        BigDecimal hundred = new BigDecimal("100");

        // (100 + magicValue) / 100) * exchangeRate
        BigDecimal convertedPriceToSmn = priceDollar.multiply(hundred.add(magicValue).divide(hundred)).multiply(exchangeRate);

        return convertedPriceToSmn.setScale(0, RoundingMode.HALF_UP);
    }
}
