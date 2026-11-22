package com.prestige.initializers;

import com.prestige.dao.sklad.SkladSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkladSettingsInitializer extends AbstractSettingsInitializer<SkladSettingsDAO> {

    @Autowired
    public SkladSettingsInitializer(SkladSettingsDAO settingsDAO) {
        super(settingsDAO);
    }

    @Override
    protected void initializeMustHaveSettings() {
        createIfNotExists("magicValue", "20");
        createIfNotExists("skladDefaultImportCostPercentage", "10");
        createIfNotExists("skladDefaultUsdSupplierCurrencyRate", "1475");
    }
}
