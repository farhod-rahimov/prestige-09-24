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

    private EquivalentProductInfo equivalentProductInfo;

    public Product(Product product) {

        if (product != null) {
            this.id = product.getId();
            this.productName = new ProductName(product.getProductName());
            this.carModel = product.getCarModel();
            this.engineModel = product.getEngineModel();
            this.oemArticleNumber = product.getOemArticleNumber();
            this.crossArticleNumber = product.getCrossArticleNumber();
            this.brand = new Brand(product.getBrand());
            this.notes = product.getNotes();
            this.equivalentProductInfo = new EquivalentProductInfo(product.getEquivalentProductInfo());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Product other = (Product)obj;
        return  equalsWithoutNotesAndEquivalentProductInfo(other) &&
                Objects.equals(notes, other.notes) &&
                Objects.equals(equivalentProductInfo, other.equivalentProductInfo);
    }

    public boolean equalsWithoutNotesAndEquivalentProductInfo(Product other) {
        return  Objects.equals(id, other.id) &&
                Objects.equals(productName, other.productName) &&
                Objects.equals(carModel, other.carModel) &&
                Objects.equals(engineModel, other.engineModel) &&
                Objects.equals(oemArticleNumber, other.oemArticleNumber) &&
                Objects.equals(crossArticleNumber, other.crossArticleNumber) &&
                Objects.equals(brand, other.brand);
    }
}
