package com.prestige.controllers.common;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping({"/prestige/product-management",
                 "/sklad/product-management"})
public class CommonProductManagementController {

    @GetMapping
    public String main() {
        return "common/product-management";
    }
}
