package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetailProduct {

    private Integer id;

    private Timestamp dateTime;

    private Product product;

    private BigDecimal priceDollar;

    private Integer stockBalance;

    private Store store;
}
