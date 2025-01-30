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

    @Override
    public boolean equals(Object obj) {
        Sales other = (Sales)obj;

        if (other != null && this != other) {
            return  other.id.equals(id) && other.product.equals(product) &&
                    other.quantity.equals(quantity) && other.soldPrice.equals(soldPrice);
        }
        return false;
    }
}
