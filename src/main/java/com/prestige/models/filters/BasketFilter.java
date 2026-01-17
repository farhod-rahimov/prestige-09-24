package com.prestige.models.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BasketFilter extends AbstractBaseFilter {

    private String productName;

    private String carModel;

    private String engineModel;

    private String oemArticleNumber;

    private String crossArticleNumber;

    private String brand;

    public BasketFilter(BasketFilter basketFilter) {

        if (basketFilter != null) {
            this.productName = basketFilter.getProductName();
            this.carModel = basketFilter.getCarModel();
            this.engineModel = basketFilter.getEngineModel();
            this.oemArticleNumber = basketFilter.getOemArticleNumber();
            this.crossArticleNumber = basketFilter.getCrossArticleNumber();
            this.brand = basketFilter.getBrand();
        }
    }

    @Override
    public void clear() {
        productName = "";
        carModel = "";
        engineModel = "";
        oemArticleNumber = "";
        crossArticleNumber = "";
        brand = "";
    }

    public void clearAllBeforeCarModel() {
        productName = "";
    }

    public void clearAllBeforeEngineModel() {
        clearAllBeforeCarModel();
        carModel = "";
    }

    public void clearAllBeforeOemArticleNumber() {
        clearAllBeforeEngineModel();
        engineModel = "";
    }

    public void clearAllBeforeCrossArticleNumber() {
        clearAllBeforeOemArticleNumber();
        oemArticleNumber = "";
    }

    public void clearAllBeforeBrand() {
        clearAllBeforeCrossArticleNumber();
        crossArticleNumber = "";
    }

    @Override
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
