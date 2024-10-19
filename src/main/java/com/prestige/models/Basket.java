package com.prestige.models;

import com.prestige.models.searchFilters.ClientSearchFilter;
import com.prestige.models.searchFilters.RetailProductSearchFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
public class Basket {

    private Integer id;

    private Timestamp createdDateTime;

    private Timestamp lastChangedDateTime;

    private Client client;

    private BigDecimal totalSum;

    private int totalSumHash;

    private ESelectType newItemSelectType;

    private ESelectType newClientSelectType;

    private List<RetailSaleUnit> retailSaleUnitList;

    private RetailProductSearchFilter productSearchFilter;

    private ClientSearchFilter clientSearchFilter;

    public boolean shouldCreateNewPrestigeSaleUnit;

    public Basket() {
        setCreatedDateTime(new Timestamp(System.currentTimeMillis()));
        setLastChangedDateTime(getCreatedDateTime());

        client = new Client();
        totalSum = new BigDecimal(0);
        newItemSelectType = ESelectType.LIST;
        newClientSelectType = ESelectType.LIST;
        retailSaleUnitList = new LinkedList<>();
        productSearchFilter = new RetailProductSearchFilter();
        clientSearchFilter = new ClientSearchFilter();
        shouldCreateNewPrestigeSaleUnit = false;
    }

    private String getDateTimeFormatted(Timestamp dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(dateTime);
    }

    public String getCreatedDateTimeFormatted() {
        return getDateTimeFormatted(createdDateTime);
    }

    public String getLastChangedDateTimeFormatted() {
        return getDateTimeFormatted(lastChangedDateTime);
    }

    public void calculateTotalSumHash() {
        totalSum = new BigDecimal(0);
        totalSumHash = 0;

        for (int i = 0; i < retailSaleUnitList.size(); i++) {
            RetailSaleUnit unit = retailSaleUnitList.get(i);

            if (unit.getIsSaved()) {
                totalSum = totalSum.add(unit.getTotalSumSmn());
                totalSumHash += Objects.hash(unit.getRetailProduct().getProduct().getId(),
                                             unit.getQuantity(), unit.getSoldPriceSmn());
            }
        }
    }

    public void update(boolean shouldCalculateTotalSum) {

        if (shouldCalculateTotalSum) {
            calculateTotalSumHash();
        }
        setLastChangedDateTime(new Timestamp(System.currentTimeMillis()));
    }

    public void resetNewItemSelectType() {
        newItemSelectType = ESelectType.LIST;
    }

    public void resetProductSearchFilter() {
        productSearchFilter.reset();
    }

    public void resetClientSearchFilter() {
        clientSearchFilter.reset();
    }

    public boolean isClientSelectionDone() {
        return client != null && client.isAllFieldsSet();
    }
}
