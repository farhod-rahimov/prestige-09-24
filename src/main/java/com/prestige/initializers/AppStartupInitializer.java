package com.prestige.initializers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AppStartupInitializer {

    private final DatabaseInitializer databaseInitializer;

    private final SkladSettingsInitializer skladSettingsInitializer;

    private final PrestigeSettingsInitializer prestigeInitializer;

    @Autowired
    public AppStartupInitializer(DatabaseInitializer databaseInitializer, SkladSettingsInitializer skladSettingsInitializer,
                                 PrestigeSettingsInitializer prestigeInitializer) {

        this.databaseInitializer = databaseInitializer;
        this.skladSettingsInitializer = skladSettingsInitializer;
        this.prestigeInitializer = prestigeInitializer;
    }

    @PostConstruct
    public void initAll() {

        // Порядок гарантирован: сначала база, потом настройки
        databaseInitializer.initializeDatabase();

        skladSettingsInitializer.init();
        prestigeInitializer.init();
    }
}
