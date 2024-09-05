package com.prestige.controllers.prestige;

import com.prestige.dao.PrestigeSalesDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/sales")
public class SalesController {

    private final PrestigeSalesDAO prestigeSalesDAO;

    @Autowired
    public SalesController(PrestigeSalesDAO prestigeSalesDAO) {
        this.prestigeSalesDAO = prestigeSalesDAO;
    }

    @GetMapping()
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", prestigeSalesDAO.showAllBaskets());
        return "prestige/sales/showAllBaskets"; // return /prestige/sales/showAllBaskets.html
    }
}
