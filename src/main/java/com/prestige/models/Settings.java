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
public class Settings {

    private Integer id;

    private String key;

    private String value;

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Settings other = (Settings)obj;
        return  Objects.equals(id, other.id) &&
                Objects.equals(key, other.key) &&
                Objects.equals(value, other.value);
    }
}
