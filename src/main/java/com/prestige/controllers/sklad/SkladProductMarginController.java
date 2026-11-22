package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractProductMarginController;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.sklad.SkladProductsMarginDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/sklad/product-management/products-margin")
public class SkladProductMarginController extends AbstractProductMarginController {

    @Autowired
    public SkladProductMarginController(SkladProductsMarginDAO skladProductsMarginDAO, CommonProductsDAO commonProductsDAO) {
        super(skladProductsMarginDAO, commonProductsDAO, "sklad_products_margin");
    }
}
