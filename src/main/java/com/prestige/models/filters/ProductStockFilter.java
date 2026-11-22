package com.prestige.models.filters;

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
public class ProductStockFilter extends ProductMarginFilter {

    protected Integer stockBalance;

    protected EComparison stockBalanceComparison;

    protected BigDecimal innerPrice;

    protected EComparison innerPriceComparison;

    public ProductStockFilter(ProductStockFilter productStockFilter) {
        super(productStockFilter);
        stockBalance = productStockFilter.getStockBalance();
        stockBalanceComparison = productStockFilter.getStockBalanceComparison();

        innerPrice = productStockFilter.getInnerPrice();
        innerPriceComparison = productStockFilter.getInnerPriceComparison();
    }

    public boolean isNotEmpty() {
        return super.isNotEmpty() || stockBalance != null ||
                 innerPrice != null;
    }

    @Override
    public void clear() {
        super.clear();
        stockBalance = null;
        stockBalanceComparison = null;
        innerPrice = null;
        innerPriceComparison = null;
    }

    @Override
    public void prepareAllFields() {
        super.prepareAllFields();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductStockFilter other = (ProductStockFilter)obj;
        return  super.equals(other) &&
                Objects.equals(stockBalance, other.stockBalance) &&
                Objects.equals(stockBalanceComparison, other.stockBalanceComparison) &&
                Objects.equals(innerPrice, other.innerPrice) &&
                Objects.equals(innerPriceComparison, other.innerPriceComparison);
    }
}
