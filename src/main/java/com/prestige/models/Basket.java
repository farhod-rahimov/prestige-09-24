package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;


@Getter
@Setter
@AllArgsConstructor
public class Basket {

    private Integer id;

    private Timestamp dateTime;

    private Client client;

    private BigDecimal totalSum;

    public String getDateTimeFormatted() {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        return sdf.format(dateTime);
    }
}
