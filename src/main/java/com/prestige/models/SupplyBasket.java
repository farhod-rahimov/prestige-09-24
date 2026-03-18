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

@Getter
@Setter
@NoArgsConstructor
public class SupplyBasket extends BaseSupplyBasket<Supply> {

    public SupplyBasket(Integer id, Timestamp createdDateTime, Timestamp lastChangedDateTime,
                        Supplier supplier, BigDecimal totalSum, String notes, List<Supply> suppliesList,
                        BaseSupplyBasket.EStatus status, BasketFilter basketFilter) {

        super(id, createdDateTime, lastChangedDateTime, supplier, totalSum, notes, suppliesList,
                status, basketFilter);
    }

    public SupplyBasket(SupplyBasket supplyBasket) {
        super(supplyBasket);
    }

    public void setSuppliesList(List<Supply> suppliesList) {
        this.suppliesList = new AutoPopulatingList<>(suppliesList, Supply.class);
    }

    public List<Supply> getSuppliesList() {
        if (suppliesList == null) {
            suppliesList = new AutoPopulatingList<>(Supply.class);
        }
        return suppliesList;
    }

    @Override
    public List<Supply> getSuppliesListForSave() {
        List<Supply> saveList = new LinkedList<>(suppliesList);
        saveList.removeIf(supply -> (supply.getQuantity().equals(0) && supply.getPriceDollar().compareTo(BigDecimal.ZERO) == 0)
                || supply.getId() != null);

        return saveList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SupplyBasket other = (SupplyBasket)obj;
        return super.equals(other);
    }
}
