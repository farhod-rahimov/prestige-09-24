package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Brand {

    private Integer id;

    private String brandName;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Brand other = (Brand)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(brandName, other.brandName);
    }
}
