package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseBasket {

    private Integer id;

    private Timestamp createdDateTime;

    private Timestamp lastChangedDateTime;

    private BigDecimal totalSum;

    private String notes;

    private BasketFilter filter;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        BaseBasket other = (BaseBasket)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(createdDateTime, other.createdDateTime) &&
                Objects.equals(lastChangedDateTime, other.lastChangedDateTime) &&
                Objects.equals(totalSum, other.totalSum) &&
                Objects.equals(notes, other.notes) &&
                Objects.equals(filter, other.filter);
    }
}
