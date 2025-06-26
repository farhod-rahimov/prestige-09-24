package com.prestige.controllers.sklad;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad")
public class SkladController {

    @GetMapping
    public String main() {
        return "sklad/sklad"; // return /sklad/sklad.html
    }
}
