package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RetailProduct other = (RetailProduct)obj;
        return  super.equals(other) &&
                Objects.equals(id, other.id) &&
                Objects.equals(deliveryDateTime, other.deliveryDateTime) &&
                Objects.equals(supplier, other.supplier);
    }
}
