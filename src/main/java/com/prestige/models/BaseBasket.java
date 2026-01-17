package com.prestige.models;

import com.prestige.models.filters.BasketFilter;
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

    protected Integer id;

    protected Timestamp createdDateTime;

    protected Timestamp lastChangedDateTime;

    protected BigDecimal totalSum;

    protected String notes;

    protected BasketFilter filter;

    public BaseBasket(BaseBasket baseBasket) {

        if (baseBasket != null) {
            this.id = baseBasket.getId();
            this.createdDateTime = baseBasket.getCreatedDateTime();
            this.lastChangedDateTime = baseBasket.getLastChangedDateTime();
            this.totalSum = baseBasket.getTotalSum();
            this.notes = baseBasket.getNotes();
            this.filter = new BasketFilter(baseBasket.getFilter());
        }
    }

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
