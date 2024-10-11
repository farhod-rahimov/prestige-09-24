package com.prestige.models.searchFilters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ClientSearchFilter {

    private String name;

    private String phoneNumber;

    public ClientSearchFilter(ClientSearchFilter other) {

        if (other != null) {
            name = other.name;
            phoneNumber = other.phoneNumber;
        }
    }

    public boolean hasAnyFilter() {
        return name != null || phoneNumber != null;
    }

    public void reset() {
        name = null;
        phoneNumber = null;
    }
}
