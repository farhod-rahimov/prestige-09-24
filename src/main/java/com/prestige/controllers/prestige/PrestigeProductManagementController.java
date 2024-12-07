package com.prestige.controllers.prestige;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/prestige/product-management")
public class PrestigeProductManagementController {

    @GetMapping
    public String main() {
        return "prestige/product-management";
    }
}
