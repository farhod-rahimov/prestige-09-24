package com.prestige.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Settings {

    private Integer id;

    private String key;

    private String value;

    @Override
    public boolean equals(Object obj) {
        Settings other = (Settings)obj;

        if (other != null && this != other) {
            return other.id.equals(id) && other.key.equals(key) && other.value.equals(value);
        }
        return false;
    }
}
