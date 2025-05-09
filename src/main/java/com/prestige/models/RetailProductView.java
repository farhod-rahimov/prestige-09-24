package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetailProductView {

    protected Product product;

    protected BigDecimal priceDollar;

    protected Integer stockBalance;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        RetailProductView other = (RetailProductView)obj;
        return  Objects.equals(product, other.product) &&
                Objects.equals(priceDollar, other.priceDollar) &&
                Objects.equals(stockBalance, other.stockBalance);
    }
}
