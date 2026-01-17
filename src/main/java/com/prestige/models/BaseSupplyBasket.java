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
@NoArgsConstructor
public abstract class BaseSupplyBasket<S extends BaseSupply> extends BaseBasket {

    protected Supplier supplier;

    protected List<S> suppliesList;

    protected BaseSupplyBasket.EStatus status;

    public abstract List<S> getSuppliesListForSave();

    public BaseSupplyBasket(Integer id, Timestamp createdDateTime, Timestamp lastChangedDateTime,
                            Supplier supplier, BigDecimal totalSum, String notes, List<S> suppliesList,
                            BaseSupplyBasket.EStatus status, BasketFilter basketFilter) {

        super(id, createdDateTime, lastChangedDateTime, totalSum, notes, basketFilter);
        this.supplier = supplier;
        this.suppliesList = suppliesList;
        this.status = status;
    }

    public BaseSupplyBasket(BaseSupplyBasket supplyBasket) {
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

        public static BaseSupplyBasket.EStatus fromIntValue(int intValue) {
            for (BaseSupplyBasket.EStatus status : BaseSupplyBasket.EStatus.values()) {
                if (status.intValue == intValue) {
                    return status;
                }
            }
            throw new IllegalArgumentException("Invalid status value: " + intValue);
        }
    }

    public List<S> getSuppliesListForUpdate() {
        List<S> updateList = new LinkedList<>(suppliesList);
        updateList.removeIf(supply -> (supply.getId() == null));

        return updateList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BaseSupplyBasket other = (BaseSupplyBasket)obj;
        return  super.equals(other) &&
                Objects.equals(supplier, other.supplier) &&
                Objects.equals(suppliesList, other.suppliesList) &&
                Objects.equals(status, other.status);
    }
}
