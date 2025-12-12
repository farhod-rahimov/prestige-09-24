package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractSuppliersController;
import com.prestige.dao.prestige.PrestigeSuppliersDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/suppliers")
public class PrestigeSuppliersController extends AbstractSuppliersController {

    @Autowired
    public PrestigeSuppliersController(PrestigeSuppliersDAO prestigeSuppliersDAO) {
        super(prestigeSuppliersDAO, "prestige");
    }
}
