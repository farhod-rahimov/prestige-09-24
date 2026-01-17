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
public abstract class BaseSupply {

    protected Integer id;

    protected Product product;

    protected BigDecimal priceDollar;

    // private infos

    protected Integer stockBeforeSale;

    public boolean equalsOnlyProductWithoutNotes(BaseSupply other) {
        if (this == other) return true;
        if (other == null) return false;

        return product.equalsWithoutNotesAndEquivalentProductInfo(other.product);
    }
}
