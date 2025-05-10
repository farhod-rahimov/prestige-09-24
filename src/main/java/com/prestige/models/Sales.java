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
public class Sales {

    private Integer id;

    private Product product;

    private Integer quantity;

    private BigDecimal soldPrice;

    // private infos

    private Integer stockBeforeSale;

    private BigDecimal calculatedPrice;

    private BigDecimal recommendedPrice;

    private BigDecimal wholesalePrice;

    public Sales(Product product) {
        this.product = product;
        this.quantity = new Integer(0);
        this.soldPrice = new BigDecimal(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Sales other = (Sales)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(product, other.product) &&
                Objects.equals(quantity, other.quantity) &&
                Objects.equals(soldPrice, other.soldPrice) &&
                Objects.equals(stockBeforeSale, other.stockBeforeSale) &&
                Objects.equals(calculatedPrice, other.calculatedPrice) &&
                Objects.equals(recommendedPrice, other.recommendedPrice) &&
                Objects.equals(wholesalePrice, other.wholesalePrice);
    }

    public boolean equalsOnlyProductWithoutNotes(Sales other) {
        if (this == other) return true;
        if (other == null) return false;

        return product.equalsWithoutNotes(other.product);
    }
}
