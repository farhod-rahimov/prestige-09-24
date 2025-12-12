package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractClientsController;
import com.prestige.dao.sklad.SkladClientsDAO;
import com.prestige.dao.sklad.SkladSalesBasketsDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad/clients")
public class SkladClientsController extends AbstractClientsController {

    @Autowired
    public SkladClientsController(SkladClientsDAO skladClientsDAO, SkladSalesBasketsDAO skladSalesBasketsDAO) {
        super(skladClientsDAO, skladSalesBasketsDAO, "sklad");
    }
}
