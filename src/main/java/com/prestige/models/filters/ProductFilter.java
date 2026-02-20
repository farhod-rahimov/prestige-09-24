package com.prestige.models.filters;

import com.prestige.models.Brand;
import com.prestige.models.Product;
import com.prestige.models.ProductName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductFilter extends AbstractBaseFilter {

    protected Product product;

    public ProductFilter(ProductFilter productFilter) {

        if (productFilter != null) {
            product = new Product(productFilter.getProduct());
        }
    }

    @Override
    public void clear() {
        product = new Product();
    }

    @Override
    public void prepareAllFields() {

        if (product != null) {

            ProductName productName = product.getProductName();
            if (productName != null) {
                productName.setProductName(getPreparedField(productName.getProductName()));
            }

            product.setCarModel(getPreparedField(product.getCarModel()));
            product.setEngineModel(getPreparedField(product.getEngineModel()));
            product.setOemArticleNumber(getPreparedField(product.getOemArticleNumber()));
            product.setCrossArticleNumber(getPreparedField(product.getCrossArticleNumber()));

            Brand brand = product.getBrand();
            if (brand != null) {
                brand.setBrandName(getPreparedField(brand.getBrandName()));
            }

            product.setNotes(getPreparedField(product.getNotes()));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductFilter other = (ProductFilter)obj;
        return  Objects.equals(product, other.product);
    }

}
