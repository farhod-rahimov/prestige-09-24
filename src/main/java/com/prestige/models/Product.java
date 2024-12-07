package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


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
        Product other = (Product)obj;

        if (other != null && this != other) {
            return  other.id.equals(id) && other.productName.equals(productName) &&
                    other.carModel.equals(carModel) && other.engineModel.equals(engineModel) &&
                    other.oemArticleNumber.equals(oemArticleNumber) && other.crossArticleNumber.equals(crossArticleNumber) &&
                    other.brand.equals(brand) && other.notes.equals(notes);
        }
        return false;
    }
}
