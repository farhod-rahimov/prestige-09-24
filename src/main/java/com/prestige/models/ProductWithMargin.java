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
public class ProductWithMargin extends Product {

    private BigDecimal margin;

    public ProductWithMargin(ProductWithMargin productWithMargin) {
        super(productWithMargin);
        this.margin = productWithMargin.getMargin();
    }

    public ProductWithMargin(Product product, BigDecimal margin) {
        super(product);
        this.margin = margin;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductWithMargin other = (ProductWithMargin)obj;
        return  super.equals(other) &&
                Objects.equals(margin, other.margin);
    }
}
