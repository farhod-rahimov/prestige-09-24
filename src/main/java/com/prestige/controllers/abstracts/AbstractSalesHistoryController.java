package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import com.prestige.dao.abstracts.AbstractSalesBasketsDAO;
import com.prestige.models.SalesBasket;
import com.prestige.models.filters.SalesHistoryFilter;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@AllArgsConstructor
public class AbstractSalesHistoryController {

    protected final AbstractSalesBasketsDAO salesBasketsDAO;

    protected final AbstractAllSalesDAO allSalesDAO;

    protected final String uriPrefix;

    private String getModelFilterName() {
        return uriPrefix.contains("prestige") ? "prestigeSalesHistoryFilter" : "skladSalesHistoryFilter";
    }

    @GetMapping
    public String showSalesHistory(Model model) {

        List<SalesBasket> allBaskets = salesBasketsDAO.findAllSoldBaskets();

        for (SalesBasket basket : allBaskets) {
            basket.setSalesList(allSalesDAO.findAllSalesByBasketId(basket.getId()));
        }

        model.addAttribute("allBaskets", allBaskets);
        model.addAttribute(getModelFilterName(), new SalesHistoryFilter());

        return String.format("%s/sales/history/history", uriPrefix);
    }

    protected String postMethodDispatcherInternal(SalesHistoryFilter salesHistoryFilter, String method,
                                                  BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                                  Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при применении фильтра. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/sales/history", uriPrefix);
        }

        switch (method) {
            case "applyFilter":
                return applyFilter(salesHistoryFilter, model);
            case "resetFilter":
                return resetFilter(salesHistoryFilter);
            default:
                throw new RuntimeException(String.format("POST method is undefined: '%s'", method));
        }
    }

    private String applyFilter(SalesHistoryFilter salesHistoryFilter, Model model) {
        SalesHistoryFilter salesHistoryFilterCopy = new SalesHistoryFilter(salesHistoryFilter);
        salesHistoryFilterCopy.prepareAllFields();

        List<SalesBasket> allBaskets = salesBasketsDAO.findAllSoldBasketsWithFilter(salesHistoryFilterCopy);

        for (SalesBasket basket : allBaskets) {
            basket.setSalesList(allSalesDAO.findAllSalesByBasketIdWithFilter(basket.getId(), salesHistoryFilterCopy));
        }

        model.addAttribute("allBaskets", allBaskets);

        return String.format("%s/sales/history/history", uriPrefix);
    }

    private String resetFilter(SalesHistoryFilter salesHistoryFilter) {
        salesHistoryFilter.clear();

        return String.format("redirect:/%s/sales/history", uriPrefix);
    }
}
