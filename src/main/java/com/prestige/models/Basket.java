package com.prestige.models;

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
public class Basket {

    private Integer id;

    private Timestamp createdDateTime;

    private Timestamp lastChangedDateTime;

    private Client client;

    private BigDecimal totalSum;

    private String notes;

    private List<Sales> salesList;

    private EStatus status;

    private BasketFilter filter;

    @AllArgsConstructor
    @Getter
    public enum EStatus {
        SOLD(1),
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

    public List<Sales> getSalesListForUpdate() {
        List<Sales> updateList = new LinkedList<>(salesList);
        updateList.removeIf(sales -> (sales.getId() == null));

        return updateList;
    }

    public List<Sales> getSalesListForSave() {
        List<Sales> saveList = new LinkedList<>(salesList);
        saveList.removeIf(sales -> (sales.getQuantity().equals(0) && sales.getSoldPrice().compareTo(BigDecimal.ZERO) == 0)
                                    || sales.getId() != null);

        return saveList;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Basket other = (Basket)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(createdDateTime, other.createdDateTime) &&
                Objects.equals(lastChangedDateTime, other.lastChangedDateTime) &&
                Objects.equals(client, other.client) &&
                Objects.equals(totalSum, other.totalSum) &&
                Objects.equals(notes, other.notes) &&
                Objects.equals(salesList, other.salesList) &&
                Objects.equals(status, other.status) &&
                Objects.equals(filter, other.filter);
    }
}
