package com.prestige.initializers;

import com.prestige.dao.abstracts.AbstractSettingsDAO;
import com.prestige.models.Settings;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractSettingsInitializer<S extends AbstractSettingsDAO> {

    private final S settingsDAO;

    abstract protected void initializeMustHaveSettings();

    public void init() {
        initializeMustHaveSettings();
    }

    protected void createIfNotExists(String key, String value) {
        Settings settings = new Settings(null, key, value);
        if (!settingsDAO.checkIfSettingsExists(settings)) {
            settingsDAO.saveNewSettings(settings);
        }
    }
}
