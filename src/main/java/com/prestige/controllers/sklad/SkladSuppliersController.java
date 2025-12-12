package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSuppliersController;
import com.prestige.dao.sklad.SkladSuppliersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad/suppliers")
public class SkladSuppliersController extends AbstractSuppliersController {

    @Autowired
    public SkladSuppliersController(SkladSuppliersDAO skladSuppliersDAO) {
        super(skladSuppliersDAO, "sklad");
    }
}
