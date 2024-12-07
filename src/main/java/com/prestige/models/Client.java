package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Client {

    protected Integer id;

    protected String name;

    protected String phoneNumber;

    @Override
    public boolean equals(Object obj) {
        Client other = (Client)obj;

        if (other != null && this != other) {
            return other.id.equals(id) && other.name.equals(name) && other.phoneNumber.equals(phoneNumber);
        }
        return false;
    }
}
