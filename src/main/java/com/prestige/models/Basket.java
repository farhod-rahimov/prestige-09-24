package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


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
}
