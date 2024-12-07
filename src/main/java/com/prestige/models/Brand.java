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
public class Brand {

    private Integer id;

    private String brandName;

    @Override
    public boolean equals(Object obj) {
        Brand other = (Brand)obj;

        if (other != null && this != other) {
            return other.id.equals(id) && other.brandName.equals(brandName);
        }
        return false;
    }
}
