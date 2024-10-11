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

    private Integer id;

    private String name;

    private String phoneNumber;

    public boolean isAllFieldsSet() {
        return id != null && name != null && phoneNumber != null;
    }
}
