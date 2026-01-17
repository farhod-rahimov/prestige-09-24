package com.prestige.models.filters;

public abstract class AbstractBaseFilter {

    public String getPreparedField(String value) {

        if (value == null || value.isEmpty()) {
            return value;
        }

        return "%" +
                value.replace("\\", "\\\\") // экранирование спец символа в ILIKE
                        .replace("%", "\\%") // экранирование спец символа в ILIKE
                        .replace("_", "\\_") // экранирование спец символа в ILIKE
                        .replace("*", "%") + // в ILIKE вместо * используется %
                "%";
    }

    public abstract void prepareAllFields();

    public abstract void clear();
}
