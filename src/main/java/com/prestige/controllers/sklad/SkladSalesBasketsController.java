package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSalesBasketsController;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.dao.sklad.SkladClientsDAO;
import com.prestige.dao.sklad.SkladSalesBasketsDAO;
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
@RequestMapping("/sklad/sales/baskets")
public class SkladSalesBasketsController extends AbstractSalesBasketsController {

    @Autowired
    public SkladSalesBasketsController(SkladSalesBasketsDAO salesBasketsDAO, SkladClientsDAO clientsDAO,
                                       SkladAllSuppliesDAO allSuppliesDAO, SkladAllSalesDAO allSalesDAO,
                                       CommonProductNamesDAO commonProductNamesDAO, CommonBrandNamesDAO commonBrandNamesDAO,
                                       CommonProductsDAO commonProductsDAO) {

        super(salesBasketsDAO, allSalesDAO, clientsDAO, allSuppliesDAO, null,
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
