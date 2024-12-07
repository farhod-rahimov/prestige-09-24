package com.prestige.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Supplier extends Client {

    private String storeName;

    public Supplier(Integer id, String name, String phoneNumber, String storeName) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.storeName = storeName;
    }

    @Override
    public boolean equals(Object obj) {
        Supplier other = (Supplier)obj;

        if (other != null && this != other) {
            return  super.equals(other) &&
                    other.storeName.equals(storeName);
        }
        return false;
    }
}
