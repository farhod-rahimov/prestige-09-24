package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductName {

    private Integer id;

    private String productName;

    @Override
    public boolean equals(Object obj) {
        ProductName other = (ProductName)obj;

        if (other != null && this != other) {
            return other.id.equals(id) && other.productName.equals(productName);
        }
        return false;
    }
}
