package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractClientsController;
import com.prestige.dao.prestige.PrestigeClientsDAO;
import com.prestige.dao.prestige.PrestigeSalesBasketsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/clients")
public class PrestigeClientsController extends AbstractClientsController {

    @Autowired
    public PrestigeClientsController(PrestigeClientsDAO prestigeClientsDAO, PrestigeSalesBasketsDAO prestigeSalesBasketsDAO) {
        super(prestigeClientsDAO, prestigeSalesBasketsDAO, "prestige");
    }
}
