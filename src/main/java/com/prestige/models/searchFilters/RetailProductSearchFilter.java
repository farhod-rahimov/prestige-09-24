package com.prestige.models.searchFilters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetailProductSearchFilter {

    private String productName;

    private String carModel;

    private String articleNumber;

    private String brandName;

    public RetailProductSearchFilter(RetailProductSearchFilter other) {

        if (other != null) {
            productName = other.productName;
            carModel = other.carModel;
            articleNumber = other.articleNumber;
            brandName = other.brandName;
        }
    }

    public boolean hasAnyFilter() {
        return  (productName != null && productName.isEmpty() == false) ||
                (carModel != null && carModel.isEmpty() == false) ||
                (articleNumber != null && articleNumber.isEmpty() == false) ||
                (brandName != null && brandName.isEmpty() == false);

    }

    public boolean isSet() {
        return productName != null && carModel != null && articleNumber != null && brandName != null;
    }

    public void reset() {
        productName = null;
        carModel = null;
        articleNumber = null;
        brandName = null;
    }
}
