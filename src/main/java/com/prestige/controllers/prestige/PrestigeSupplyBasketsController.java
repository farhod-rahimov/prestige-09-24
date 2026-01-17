package com.prestige.controllers.prestige;

import com.prestige.controllers.abstracts.AbstractSupplyBasketsController;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.*;
import com.prestige.models.BaseSupplyBasket;
import com.prestige.models.Supply;
import com.prestige.models.SupplyBasket;
import com.prestige.models.enums.EStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
@RequestMapping("/prestige/supplies/baskets")

@SessionAttributes({"supplyStep", "supplyBasket"})
public class PrestigeSupplyBasketsController extends AbstractSupplyBasketsController {

    @Autowired
    public PrestigeSupplyBasketsController(PrestigeSettingsDAO prestigeSettingsDAO,
                                           PrestigeSupplyBasketsDAO prestigeSupplyBasketsDAO,
                                           PrestigeSuppliersDAO prestigeSuppliersDAO,
                                           PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                           PrestigeAllSalesDAO prestigeAllSalesDAO,
                                           CommonProductsDAO commonProductsDAO,
                                           CommonProductNamesDAO commonProductNamesDAO,
                                           CommonBrandNamesDAO commonBrandNamesDAO) {

        super(Supply.class, prestigeSettingsDAO, prestigeSupplyBasketsDAO, prestigeSuppliersDAO, prestigeAllSuppliesDAO, prestigeAllSalesDAO,
              commonProductsDAO, commonProductNamesDAO, commonBrandNamesDAO, "prestige");
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", supplyBasketsDAO.findAllBaskets());
        model.addAttribute("supplyBasket", new SupplyBasket()); // mock

        return "prestige/supplies/baskets/baskets"; // return /prestige/supplies/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("supplyBasket", new SupplyBasket());
        model.addAttribute("supplyStep", EStep.SUPPLIER_STORE_NAME_SELECTION);
        model.addAttribute("allSupplierStoreNames", suppliersDAO.findUniqueSupplierStoreNames());
        return "prestige/supplies/baskets/add-new-basket";
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        return baseFindBasketById(basketId, model);
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("supplyBasket") SupplyBasket supplyBasket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSuppliesIds", required = false) List<Integer> selectedSuppliesIds,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        return basePostMethodDispatcher(supplyBasket, method, selectedSuppliesIds, bindingResult, redirectAttributes, model);
    }

    @Override
    protected void updateBasketTotalSum(BaseSupplyBasket supplyBasket) {
        List<Supply> suppliesList = supplyBasket.getSuppliesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Supply supply : suppliesList) {
            totalSum = totalSum.add(supply.getPriceDollar().multiply(BigDecimal.valueOf(supply.getQuantity())));
        }
        supplyBasket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        ((PrestigeAllSuppliesDAO)allSuppliesDAO).updateBasketTotalSum((SupplyBasket)supplyBasket);
    }

    @Override
    protected EStep processSupplierPhoneNumberSelection(BaseSupplyBasket supplyBasket, EStep supplyStep) {
        baseProcessSupplierPhoneNumberSelection(supplyBasket);
        supplyBasketsDAO.saveNewBasket(supplyBasket);
        return getNextStep(supplyStep);
    }

    @Override
    protected EStep processProductQuantityAndPriceSelection(BaseSupplyBasket supplyBasket, EStep supplyStep) {
        Supply newSupply = (Supply)supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);

        allSuppliesDAO.saveNewSupply(supplyBasket, newSupply);
        updateBasketTotalSum(supplyBasket);
        return getNextStep(supplyStep);
    }

    @Override
    protected EStep processProductSelectionDone(BaseSupplyBasket supplyBasket, Model model, EStep supplyStep) {
        supplyBasket.getSuppliesList().add(new Supply());
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames(supplyBasket.getFilter()));
        return getNextStep(supplyStep);
    }

    @Override
    protected void prepareDataForPatching(BaseSupplyBasket supplyBasket) {

    }
}
