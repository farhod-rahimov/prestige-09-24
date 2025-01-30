package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

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

    @Override
    public boolean equals(Object obj) {
        Basket other = (Basket)obj;

        if (other != null && this != other) {
            return  other.id.equals(id) && other.createdDateTime.equals(createdDateTime) &&
                    other.lastChangedDateTime.equals(lastChangedDateTime) && other.client.equals(client) &&
                    other.totalSum.equals(totalSum) && other.notes.equals(notes) && other.salesList.equals(salesList);
        }
        return false;
    }
}
