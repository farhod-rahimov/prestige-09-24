package com.prestige.models;

import com.prestige.models.filters.BasketFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SupplyBasket extends BaseBasket {

    private Supplier supplier;

    private List<Supply> suppliesList;

    private EStatus status;

    public SupplyBasket(Integer id, Timestamp createdDateTime, Timestamp lastChangedDateTime,
                        Supplier supplier, BigDecimal totalSum, String notes, List<Supply> suppliesList,
                        EStatus status, BasketFilter basketFilter) {

        super(id, createdDateTime, lastChangedDateTime, totalSum, notes, basketFilter);
        this.supplier = supplier;
        this.suppliesList = suppliesList;
        this.status = status;
    }

    public SupplyBasket(SupplyBasket supplyBasket) {
        super(supplyBasket);

        if (supplyBasket != null) {
            this.supplier = new Supplier(supplyBasket.getSupplier());
            this.suppliesList = new LinkedList<>(supplyBasket.getSuppliesList());
            this.status = supplyBasket.getStatus();
        }
    }

    @AllArgsConstructor
    @Getter
    public enum EStatus {
        RECEIVED(1),
        DELETED(2),
        PENDING(3);

        private final Integer intValue;

        public static EStatus fromIntValue(int intValue) {
            for (EStatus status : EStatus.values()) {
                if (status.intValue == intValue) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + intValue);
        }
    }

    public List<Supply> getSuppliesListForUpdate() {
        List<Supply> updateList = new LinkedList<>(suppliesList);
        updateList.removeIf(supply -> (supply.getId() == null));

        return updateList;
    }

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
        return  super.equals(other) &&
                Objects.equals(supplier, other.supplier) &&
                Objects.equals(suppliesList, other.suppliesList) &&
                Objects.equals(status, other.status);
    }
}
