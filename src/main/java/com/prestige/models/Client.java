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
public class Client {

    protected Integer id;

    protected String name;

    protected String phoneNumber;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Client other = (Client)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(name, other.name) &&
                Objects.equals(phoneNumber, other.phoneNumber);
    }
}
