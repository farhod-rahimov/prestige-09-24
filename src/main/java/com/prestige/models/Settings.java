package com.prestige.models;

import com.prestige.dao.ProductListManagementDAO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Component
public class Settings {

    private Map<String, String> settingsMap;

    public String getValueByKey(String key) {
        return settingsMap.get(key);
    }

    public void setValueByKey(String key, String value) {
        settingsMap.put(key, value);
    }

    public Settings() {
        settingsMap = new HashMap<>();

        // default values
        settingsMap.put("magicValue", "20");
        settingsMap.put("exchangeRate", "11");
    }
}
