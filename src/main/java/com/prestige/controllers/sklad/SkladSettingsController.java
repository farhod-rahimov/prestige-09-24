package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSettingsController;
import com.prestige.dao.sklad.SkladSettingsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad/settings")
public class SkladSettingsController extends AbstractSettingsController {

    @Autowired
    public SkladSettingsController(SkladSettingsDAO skladSettingsDAO) {
        super(skladSettingsDAO, "sklad");
    }
}
