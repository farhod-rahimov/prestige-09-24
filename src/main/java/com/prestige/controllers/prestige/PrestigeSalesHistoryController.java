package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractSalesHistoryController;
import com.prestige.dao.prestige.PrestigeAllSalesDAO;
import com.prestige.dao.prestige.PrestigeSalesBasketsDAO;
import com.prestige.models.filters.SalesHistoryFilter;
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
@RequestMapping("/prestige/sales/history")
public class PrestigeSalesHistoryController extends AbstractSalesHistoryController {

    @Autowired
    public PrestigeSalesHistoryController(PrestigeSalesBasketsDAO prestigeSalesBasketsDAO, PrestigeAllSalesDAO prestigeAllSalesDAO) {
        super(prestigeSalesBasketsDAO, prestigeAllSalesDAO, "prestige");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("salesHistoryFilter") SalesHistoryFilter salesHistoryFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        return postMethodDispatcherInternal(salesHistoryFilter, method, bindingResult, redirectAttributes, model);
    }
}
