package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
public class SkladSupply extends BaseSupply {

    private Integer orderedQuantity;

    private Integer receivedQuantity;

    private BigDecimal priceSupplierCurrency;

    private BigDecimal importCostPercentage;

    private String notes;

    public SkladSupply() {
        initDecimalAttributes();
    }

    public SkladSupply(Product product) {
        this.product = product;
        initDecimalAttributes();
    }

    public BigDecimal getReceivedSumSupplierCurrency() {

        if (receivedQuantity == null || priceSupplierCurrency == null) {
            return null;
        }
        return priceSupplierCurrency.multiply(BigDecimal.valueOf(receivedQuantity));
    }

    public BigDecimal getReceivedSumUsd() {

        if (receivedQuantity == null || priceDollar == null) {
            return null;
        }
        return priceDollar.multiply(BigDecimal.valueOf(receivedQuantity));
    }

    public SkladSupply(SkladSupply supply) {

        if (supply != null) {
            this.id = supply.getId();
            this.product = new Product(supply.getProduct());
            this.orderedQuantity = supply.getOrderedQuantity();
            this.receivedQuantity = supply.getReceivedQuantity();
            this.priceDollar = supply.getPriceDollar();
            this.priceSupplierCurrency = supply.getPriceSupplierCurrency();
            this.importCostPercentage = supply.getImportCostPercentage();
            this.notes = supply.getNotes();
        }
    }

    private void initDecimalAttributes() {
        this.orderedQuantity = new Integer(0);
        this.receivedQuantity = new Integer(0);
        this.priceDollar = new BigDecimal(0);
        this.priceSupplierCurrency = new BigDecimal(0);
        this.importCostPercentage = new BigDecimal(0);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SkladSupply other = (SkladSupply)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(product, other.product) &&
                Objects.equals(orderedQuantity, other.orderedQuantity) &&
                Objects.equals(receivedQuantity, other.receivedQuantity) &&
                Objects.equals(priceDollar, other.priceDollar) &&
                Objects.equals(priceSupplierCurrency, other.priceSupplierCurrency) &&
                Objects.equals(importCostPercentage, other.importCostPercentage) &&
                Objects.equals(notes, other.notes);
    }

    public boolean equalsOnlyProductWithoutNotesAndEquivalentProductInfo(SkladSupply other) {
        if (this == other) return true;
        if (other == null) return false;

        return product.equalsWithoutNotesAndEquivalentProductInfo(other.product);
    }
}
