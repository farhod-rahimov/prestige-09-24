package com.prestige.initializers;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@AllArgsConstructor
public class AppStartupInitializer {

    private final DatabaseInitializer databaseInitializer;

    private final SkladSettingsInitializer skladSettingsInitializer;

    private final PrestigeSettingsInitializer prestigeInitializer;

    @PostConstruct
    public void initAll() {

        // Порядок гарантирован: сначала база, потом настройки
        databaseInitializer.initializeDatabase();

        skladSettingsInitializer.init();
        prestigeInitializer.init();
    }
}
