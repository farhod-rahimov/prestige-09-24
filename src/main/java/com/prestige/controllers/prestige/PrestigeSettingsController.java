package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractSettingsController;
import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.initializers.PrestigeSettingsInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/settings")
public class PrestigeSettingsController extends AbstractSettingsController<PrestigeSettingsDAO> {

    @Autowired
    public PrestigeSettingsController(PrestigeSettingsInitializer settingsInitializer, PrestigeSettingsDAO prestigeSettingsDAO) {
        super(settingsInitializer, prestigeSettingsDAO, "prestige");
    }
}
