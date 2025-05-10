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
public class Product {

    private Integer id;

    private ProductName productName;

    private String carModel;

    private String engineModel;

    private String oemArticleNumber;

    private String crossArticleNumber;

    private Brand brand;

    private String notes;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Product other = (Product)obj;
        return equalsWithoutNotes(other) && Objects.equals(notes, other.notes);
    }

    public boolean equalsWithoutNotes(Product other) {
        return  Objects.equals(id, other.id) &&
                Objects.equals(productName, other.productName) &&
                Objects.equals(carModel, other.carModel) &&
                Objects.equals(engineModel, other.engineModel) &&
                Objects.equals(oemArticleNumber, other.oemArticleNumber) &&
                Objects.equals(crossArticleNumber, other.crossArticleNumber) &&
                Objects.equals(brand, other.brand);
    }
}
