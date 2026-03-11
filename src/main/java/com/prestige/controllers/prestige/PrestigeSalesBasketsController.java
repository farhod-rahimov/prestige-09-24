package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractSalesBasketsController;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.*;
import com.prestige.models.SalesBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/prestige/sales/baskets")
public class PrestigeSalesBasketsController extends AbstractSalesBasketsController {

    @Autowired
    public PrestigeSalesBasketsController(PrestigeSalesBasketsDAO salesBasketsDAO, PrestigeAllSalesDAO allSalesDAO,
                                          PrestigeClientsDAO clientsDAO, PrestigeAllSuppliesDAO allSuppliesDAO,
                                          PrestigeSettingsDAO settingsDAO, CommonProductsDAO commonProductsDAO,
                                          CommonProductNamesDAO commonProductNamesDAO, CommonBrandNamesDAO commonBrandNamesDAO) {

        super(salesBasketsDAO, allSalesDAO, clientsDAO, allSuppliesDAO, settingsDAO,
                commonProductsDAO, commonProductNamesDAO, commonBrandNamesDAO, "prestige");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("salesBasket") SalesBasket salesBasketForm,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "tabId", required = false) String tabId,
                                       @RequestParam(value = "selectedSalesIds", required = false) List<Integer> selectedSalesIds,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                       Model model, HttpSession session) {

        return postMethodDispatcherInternal(salesBasketForm, method, tabId, selectedSalesIds, bindingResult, redirectAttributes, model, session);
    }
}
