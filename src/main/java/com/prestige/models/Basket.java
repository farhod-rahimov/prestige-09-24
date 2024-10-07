package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class Basket {

    private Integer id;

    private Timestamp createdDateTime;

    private Timestamp lastChangedDateTime;

    private Client client;

    private BigDecimal totalSum;

    private List<RetailSaleUnit> retailSaleUnitList;

    public Basket() {
        setCreatedDateTime(new Timestamp(System.currentTimeMillis()));
        setLastChangedDateTime(getCreatedDateTime());

        retailSaleUnitList = new LinkedList<>();
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

    public void updateLastChangedDateTime() {
        setLastChangedDateTime(new Timestamp(System.currentTimeMillis()));
    }
}
