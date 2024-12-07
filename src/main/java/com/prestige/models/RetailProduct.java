package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetailProduct extends RetailProductView {

    private Integer id;

    private Timestamp deliveryDateTime;

    private Supplier supplier;

    public RetailProduct(Integer id, Timestamp deliveryDateTime, Supplier supplier,
                         Product product, BigDecimal priceDollar, Integer stockBalance) {

        this.id = id;
        this.deliveryDateTime = deliveryDateTime;
        this.supplier = supplier;
        this.product = product;
        this.priceDollar = priceDollar;
        this.stockBalance = stockBalance;
    }

    @Override
    public boolean equals(Object obj) {
        RetailProduct other = (RetailProduct)obj;

        if (other != null && this != other) {
            return  super.equals(other) &&
                    other.id.equals(id) &&
                    other.deliveryDateTime.equals(deliveryDateTime) &&
                    other.supplier.equals(supplier);
        }
        return false;
    }
}
