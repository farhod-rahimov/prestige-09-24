package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSalesBasketsController;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.sklad.*;
import com.prestige.models.SalesBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;


@Controller
@RequestMapping("/sklad/sales/baskets")
public class SkladSalesBasketsController extends AbstractSalesBasketsController {

    @Autowired
    public SkladSalesBasketsController(SkladSalesBasketsDAO salesBasketsDAO, SkladClientsDAO clientsDAO,
                                       SkladAllSuppliesDAO allSuppliesDAO, SkladAllSalesDAO allSalesDAO,
                                       SkladSettingsDAO settingsDAO, SkladProductsMarginDAO marginDAO,
                                       CommonProductNamesDAO commonProductNamesDAO, CommonBrandNamesDAO commonBrandNamesDAO,
                                       CommonProductsDAO commonProductsDAO) {

        super(salesBasketsDAO, allSalesDAO, clientsDAO, allSuppliesDAO, settingsDAO, marginDAO,
              commonProductsDAO, commonProductNamesDAO, commonBrandNamesDAO, "sklad");
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladSalesBasket") SalesBasket salesBasketForm,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "tabId", required = false) String tabId,
                                       @RequestParam(value = "selectedSalesIds", required = false) List<Integer> selectedSalesIds,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                       Model model, HttpSession session) {

        return postMethodDispatcherInternal(salesBasketForm, method, tabId, selectedSalesIds, bindingResult, redirectAttributes, model, session);
    }
}
