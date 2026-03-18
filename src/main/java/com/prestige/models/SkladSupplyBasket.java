package com.prestige.models;

import com.prestige.models.filters.BasketFilter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.util.AutoPopulatingList;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
public class SkladSupplyBasket extends BaseSupplyBasket<SkladSupply> {

    private String basketName;

    protected BigDecimal totalSumSupplierCurrency;

    protected BigDecimal usdSupplierCurrencyRate;

    protected String currencyName;

    public SkladSupplyBasket(Integer id, Timestamp createdDateTime, Timestamp lastChangedDateTime,
                             Supplier supplier, BigDecimal totalSum, String notes, List<SkladSupply> suppliesList,
                             EStatus status, BasketFilter basketFilter, String basketName, BigDecimal totalSumSupplierCurrency,
                             BigDecimal usdSupplierCurrencyRate, String currencyName) {

        super(id, createdDateTime, lastChangedDateTime, supplier, totalSum, notes, suppliesList, status, basketFilter);
        this.basketName = basketName;
        this.totalSumSupplierCurrency = totalSumSupplierCurrency;
        this.usdSupplierCurrencyRate = usdSupplierCurrencyRate;
        this.currencyName = currencyName;
    }

    public void setSuppliesList(List<SkladSupply> suppliesList) {
        this.suppliesList = new AutoPopulatingList<>(suppliesList, SkladSupply.class);
    }

    public List<SkladSupply> getSuppliesList() {
        if (suppliesList == null) {
            suppliesList = new AutoPopulatingList<>(SkladSupply.class);
        }
        return suppliesList;
    }

    @Override
    public List<SkladSupply> getSuppliesListForSave() {
        List<SkladSupply> saveList = new LinkedList<>(suppliesList);
        saveList.removeIf(supply -> (supply.getOrderedQuantity().equals(0) && supply.getReceivedQuantity().equals(0) &&
                                     supply.getPriceDollar().compareTo(BigDecimal.ZERO) == 0 &&
                                     supply.getPriceSupplierCurrency().compareTo(BigDecimal.ZERO) == 0) || supply.getId() != null);

        return saveList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SkladSupplyBasket other = (SkladSupplyBasket)obj;
        return  super.equals(other) &&
                Objects.equals(basketName, other.basketName) &&
                Objects.equals(totalSumSupplierCurrency, other.totalSumSupplierCurrency) &&
                Objects.equals(usdSupplierCurrencyRate, other.usdSupplierCurrencyRate) &&
                Objects.equals(currencyName, other.currencyName);
    }

    public BigDecimal getTotalSumUsd() {
        return this.totalSum;
    }

    public BigDecimal setTotalSumUsd(BigDecimal other) {
        return this.totalSum = other;
    }
}
