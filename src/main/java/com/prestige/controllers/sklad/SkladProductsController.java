package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractProductsController;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.dao.sklad.SkladSettingsDAO;
import com.prestige.models.filters.ProductFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
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
    public String postMethodDispatcher(@ModelAttribute("skladProductFilter") ProductFilter productFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        return postMethodDispatcherInternal(productFilter, method, bindingResult, redirectAttributes, model);
    }
}
