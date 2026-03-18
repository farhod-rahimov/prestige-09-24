package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractProductsController;
import com.prestige.dao.prestige.PrestigeAllSalesDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.models.filters.ProductFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prestige/products")
public class PrestigeProductsController extends AbstractProductsController {

    @Autowired
    public PrestigeProductsController(PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                      PrestigeSettingsDAO prestigeSettingsDAO,
                                      PrestigeAllSalesDAO prestigeAllSalesDAO) {

        super(prestigeAllSuppliesDAO, prestigeAllSalesDAO, prestigeSettingsDAO, "prestige");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("productFilter") ProductFilter productFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        return postMethodDispatcherInternal(productFilter, method, bindingResult, redirectAttributes, model);
    }
}
