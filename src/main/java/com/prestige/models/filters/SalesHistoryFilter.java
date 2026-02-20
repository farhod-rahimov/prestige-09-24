package com.prestige.models.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SalesHistoryFilter extends ProductFilter {

    private String clientName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate soldDate;

    private Integer basketId;

    public SalesHistoryFilter(SalesHistoryFilter salesHistoryFilter) {
        super(salesHistoryFilter);

        if (salesHistoryFilter != null) {
            clientName = salesHistoryFilter.getClientName();
            soldDate = salesHistoryFilter.getSoldDate();
            basketId = salesHistoryFilter.getBasketId();
        }
    }

    @Override
    public void clear() {
        clientName = "";
        soldDate = null;
        basketId = null;
        super.clear();
    }

    @Override
    public void prepareAllFields() {
        clientName = getPreparedField(clientName);
        super.prepareAllFields();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        SalesHistoryFilter other = (SalesHistoryFilter)obj;
        return  super.equals(other) &&
                Objects.equals(clientName, other.clientName) &&
                Objects.equals(soldDate, other.soldDate) &&
                Objects.equals(basketId, other.basketId);
    }
}
