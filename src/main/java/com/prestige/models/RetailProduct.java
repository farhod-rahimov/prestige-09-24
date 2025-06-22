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

    private Timestamp receivedDateTime; // TODO: заменить значение на receivedDateTime

    private Supplier supplier;

    public RetailProduct(Integer id, Timestamp receivedDateTime, Supplier supplier,
                         Product product, BigDecimal priceDollar, Integer stockBalance) {

        this.id = id;
        this.receivedDateTime = receivedDateTime;
        this.supplier = supplier;
        this.product = product;
        this.priceDollar = priceDollar;
        this.stockBalance = stockBalance;
    }

    public RetailProduct(RetailProduct retailProduct) {
        super(retailProduct);

        if (retailProduct != null) {
            this.id = retailProduct.getId();
            this.receivedDateTime = retailProduct.getReceivedDateTime();
            this.supplier = new Supplier(retailProduct.getSupplier());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RetailProduct other = (RetailProduct)obj;
        return  super.equals(other) &&
                Objects.equals(id, other.id) &&
                Objects.equals(receivedDateTime, other.receivedDateTime) &&
                Objects.equals(supplier, other.supplier);
    }
}
