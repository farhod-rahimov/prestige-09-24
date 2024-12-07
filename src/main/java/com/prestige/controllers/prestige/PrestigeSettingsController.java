package com.prestige.controllers.prestige;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/settings")
public class PrestigeSettingsController {

    @GetMapping
    public String showAllParameters() {
//        return "prestige/prestige"; // return /prestige/prestige.html
        return null;
    }
}
