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
public class Supply extends BaseSupply {

    private Integer quantity = 0;

    public Supply(Product product) {
        this.product = product;
        this.quantity = new Integer(0);
        this.priceDollar = new BigDecimal(0);
    }

    public Supply(Supply supply) {

        if (supply != null) {
            this.id = supply.getId();
            this.product = new Product(supply.getProduct());
            this.quantity = supply.getQuantity();
            this.priceDollar = supply.getPriceDollar();
        }
    }

    public BigDecimal getSumUsd() {

        if (quantity == null || priceDollar == null) {
            return null;
        }
        return priceDollar.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Supply other = (Supply)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(product, other.product) &&
                Objects.equals(quantity, other.quantity) &&
                Objects.equals(priceDollar, other.priceDollar) &&
                Objects.equals(stockBeforeSale, other.stockBeforeSale);
    }

    public boolean equalsOnlyProductWithoutNotes(Supply other) {
        if (this == other) return true;
        if (other == null) return false;

        return super.equalsOnlyProductWithoutNotes(other);
    }
}
