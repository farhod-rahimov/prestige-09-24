package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductName {

    private Integer id;

    private String productName;

    public ProductName(ProductName productName) {

        if (productName != null) {
            this.id = productName.getId();
            this.productName = productName.getProductName();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductName other = (ProductName)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(productName, other.productName);
    }
}
