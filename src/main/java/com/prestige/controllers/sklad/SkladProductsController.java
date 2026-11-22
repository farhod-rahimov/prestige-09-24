package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractProductsController;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.dao.sklad.SkladSettingsDAO;
import com.prestige.models.filters.ProductStockFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sklad/products")
public class SkladProductsController extends AbstractProductsController {

    @Autowired
    public SkladProductsController(SkladAllSuppliesDAO skladAllSuppliesDAO,
                                   SkladSettingsDAO skladSettingsDAO,
                                   SkladAllSalesDAO skladAllSalesDAO) {

        super(skladAllSuppliesDAO, skladAllSalesDAO, skladSettingsDAO, "sklad");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("productStockFilter") ProductStockFilter productStockFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        return postMethodDispatcherInternal(productStockFilter, method, bindingResult, redirectAttributes, model);
    }
}
