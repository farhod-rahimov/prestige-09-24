package com.prestige.controllers.prestige;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige")
public class PrestigeController {

    @GetMapping
    public String main() {
        return "prestige/prestige"; // return /prestige/prestige.html
    }
}
