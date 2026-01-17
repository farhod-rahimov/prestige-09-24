package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSettingsController;
import com.prestige.dao.sklad.SkladSettingsDAO;
import com.prestige.initializers.SkladSettingsInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad/settings")
public class SkladSettingsController extends AbstractSettingsController<SkladSettingsDAO> {

    @Autowired
    public SkladSettingsController(SkladSettingsInitializer settingsInitializer, SkladSettingsDAO skladSettingsDAO) {
        super(settingsInitializer, skladSettingsDAO, "sklad");
    }
}
