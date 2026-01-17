package com.prestige.initializers;

import com.prestige.dao.prestige.PrestigeSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PrestigeSettingsInitializer extends AbstractSettingsInitializer<PrestigeSettingsDAO> {

    @Autowired
    public PrestigeSettingsInitializer(PrestigeSettingsDAO settingsDAO) {
        super(settingsDAO);
    }

    @Override
    protected void initializeMustHaveSettings() {
        createIfNotExists("magicValue", "20");
        createIfNotExists("exchangeRate", "11.22");
    }
}
