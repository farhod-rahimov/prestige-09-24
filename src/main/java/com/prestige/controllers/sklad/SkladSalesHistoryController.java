package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSalesHistoryController;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladSalesBasketsDAO;
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
@RequestMapping("/sklad/sales/history")
public class SkladSalesHistoryController extends AbstractSalesHistoryController {

    @Autowired
    public SkladSalesHistoryController(SkladSalesBasketsDAO skladSalesBasketsDAO, SkladAllSalesDAO skladAllSalesDAO) {
        super(skladSalesBasketsDAO, skladAllSalesDAO, "sklad");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladSalesHistoryFilter") SalesHistoryFilter salesHistoryFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        return postMethodDispatcherInternal(salesHistoryFilter, method, bindingResult, redirectAttributes, model);
    }
}
