package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractProductMarginController;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeProductsMarginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/prestige/product-management/products-margin")
public class PrestigeProductMarginController extends AbstractProductMarginController {

    @Autowired
    public PrestigeProductMarginController(PrestigeProductsMarginDAO prestigeProductsMarginDAO, CommonProductsDAO commonProductsDAO) {
        super(prestigeProductsMarginDAO, commonProductsDAO, "prestige_products_margin");
    }
}
