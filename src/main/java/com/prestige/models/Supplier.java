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
public class Supplier extends Client {

    private String storeName;

    public Supplier(Integer id, String name, String phoneNumber, String storeName) {
        super(id, name, phoneNumber);
        this.storeName = storeName;
    }

    public Supplier(Supplier supplier) {
        super(supplier);

        if (supplier != null) {
            this.storeName = supplier.getStoreName();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Supplier other = (Supplier)obj;
        return  super.equals(other) && Objects.equals(storeName, other.storeName);
    }
}
