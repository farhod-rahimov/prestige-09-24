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
public class BasketFilter {

    private String productName;

    private String carModel;

    private String engineModel;

    private String oemArticleNumber;

    private String crossArticleNumber;

    private String brand;

    public void clear() {
        productName = "";
        carModel = "";
        engineModel = "";
        oemArticleNumber = "";
        crossArticleNumber = "";
        brand = "";
    }

    private String getPreparedField(String value) {

        return "%" +
                    value.replace("\\", "\\\\") // экранирование спец символа в ILIKE
                         .replace("%", "\\%") // экранирование спец символа в ILIKE
                         .replace("_", "\\_") // экранирование спец символа в ILIKE
                         .replace("*", "%") + // в ILIKE вместо * используется %
                "%";
    }

    public void prepareAllFields() {

        productName = getPreparedField(productName);
        carModel = getPreparedField(carModel);
        engineModel = getPreparedField(engineModel);
        oemArticleNumber = getPreparedField(oemArticleNumber);
        crossArticleNumber = getPreparedField(crossArticleNumber);
        brand = getPreparedField(brand);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BasketFilter other = (BasketFilter)obj;
        return  Objects.equals(productName, other.productName) &&
                Objects.equals(carModel, other.carModel) &&
                Objects.equals(engineModel, other.engineModel) &&
                Objects.equals(oemArticleNumber, other.oemArticleNumber) &&
                Objects.equals(crossArticleNumber, other.crossArticleNumber) &&
                Objects.equals(brand, other.brand);
    }
}
