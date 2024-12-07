package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

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
        RetailProductView other = (RetailProductView)obj;

        if (other != null && this != other) {
            return  other.product.equals(product) &&
                    other.priceDollar.equals(priceDollar) &&
                    other.stockBalance.equals(stockBalance);
        }
        return false;
    }
}
