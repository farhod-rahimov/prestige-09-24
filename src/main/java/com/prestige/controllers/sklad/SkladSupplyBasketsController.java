package com.prestige.controllers.sklad;

import com.prestige.controllers.abstracts.AbstractSupplyBasketsController;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.sklad.*;
import com.prestige.models.BaseSupplyBasket;
import com.prestige.models.SkladSupply;
import com.prestige.models.SkladSupplyBasket;
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
@RequestMapping("/sklad/supplies/baskets")
@SessionAttributes({"skladSupplyStep", "skladSupplyBasket"})
public class SkladSupplyBasketsController extends AbstractSupplyBasketsController {

    @Autowired
    public SkladSupplyBasketsController(SkladSettingsDAO skladSettingsDAO,
                                        SkladSupplyBasketsDAO skladSupplyBasketsDAO,
                                        SkladSuppliersDAO skladSuppliersDAO,
                                        SkladAllSuppliesDAO skladAllSuppliesDAO,
                                        SkladAllSalesDAO skladAllSalesDAO,
                                        CommonProductNamesDAO commonProductNamesDAO,
                                        CommonProductsDAO commonProductsDAO,
                                        CommonBrandNamesDAO commonBrandNamesDAO) {

        super(SkladSupply.class, skladSettingsDAO, skladSupplyBasketsDAO, skladSuppliersDAO, skladAllSuppliesDAO, skladAllSalesDAO,
                commonProductsDAO, commonProductNamesDAO, commonBrandNamesDAO, "sklad");
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", supplyBasketsDAO.findAllBaskets());
        model.addAttribute("skladSupplyBasket", new SkladSupplyBasket()); // mock

        return "sklad/supplies/baskets/baskets"; // return /sklad/supplies/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("skladSupplyBasket", new SkladSupplyBasket());
        model.addAttribute("skladSupplyStep", EStep.SUPPLIER_STORE_NAME_SELECTION);
        model.addAttribute("allSupplierStoreNames", suppliersDAO.findUniqueSupplierStoreNames());

        return "sklad/supplies/baskets/add-new-basket";
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        return baseFindBasketById(basketId, model);
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladSupplyBasket") SkladSupplyBasket supplyBasket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSuppliesIds", required = false) List<Integer> selectedSuppliesIds,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        return basePostMethodDispatcher(supplyBasket, method, selectedSuppliesIds, bindingResult, redirectAttributes, model);
    }

    @Override
    protected void updateBasketTotalSum(BaseSupplyBasket supplyBasket) {
        List<SkladSupply> suppliesList = supplyBasket.getSuppliesListForUpdate();
        BigDecimal totalSumUsd = new BigDecimal(0);
        BigDecimal totalSumSupplierCurrency = new BigDecimal(0);

        for (SkladSupply supply : suppliesList) {
            BigDecimal receivedQuantity = BigDecimal.valueOf(supply.getReceivedQuantity());

            totalSumUsd = totalSumUsd.add(supply.getPriceDollar().multiply(receivedQuantity));
            totalSumSupplierCurrency = totalSumSupplierCurrency.add(supply.getPriceSupplierCurrency().multiply(receivedQuantity));
        }
        ((SkladSupplyBasket)supplyBasket).setTotalSumUsd(totalSumUsd.setScale(2, RoundingMode.HALF_UP));
        ((SkladSupplyBasket)supplyBasket).setTotalSumSupplierCurrency(totalSumSupplierCurrency.setScale(2, RoundingMode.HALF_UP));

        ((SkladAllSuppliesDAO)allSuppliesDAO).updateBasketTotalSumUsd((SkladSupplyBasket)supplyBasket);
        ((SkladAllSuppliesDAO)allSuppliesDAO).updateBasketTotalSumSupplierCurrency((SkladSupplyBasket)supplyBasket);
    }

    @Override
    protected EStep processSupplierPhoneNumberSelection(BaseSupplyBasket supplyBasket, EStep supplyStep) {
        baseProcessSupplierPhoneNumberSelection(supplyBasket);

        ((SkladSupplyBasket)supplyBasket).setUsdSupplierCurrencyRate(
                new BigDecimal(settingsDAO.findSettingsByName("skladDefaultUsdSupplierCurrencyRate").getValue()));

        ((SkladSupplyBasket)supplyBasket).setCurrencyName("ВОН");

        supplyBasketsDAO.saveNewBasket(supplyBasket);
        return getNextStep(supplyStep);
    }

    @Override
    protected EStep processProductQuantityAndPriceSelection(BaseSupplyBasket supplyBasket, EStep supplyStep) {
        SkladSupply newSupply = (SkladSupply)supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);

        newSupply.setPriceDollar(newSupply.getPriceSupplierCurrency().
                divide(((SkladSupplyBasket)supplyBasket).getUsdSupplierCurrencyRate(), 2, RoundingMode.HALF_UP));

        allSuppliesDAO.saveNewSupply(supplyBasket, newSupply);
        updateBasketTotalSum(supplyBasket);
        return getNextStep(supplyStep);
    }

    @Override
    protected EStep processProductSelectionDone(BaseSupplyBasket supplyBasket, Model model, EStep supplyStep) {
        SkladSupply skladSupply = new SkladSupply();
        skladSupply.setImportCostPercentage(new BigDecimal(settingsDAO.findSettingsByName("skladDefaultImportCostPercentage").getValue()));

        supplyBasket.getSuppliesList().add(skladSupply);

        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames(supplyBasket.getFilter()));
        return getNextStep(supplyStep);
    }

    @Override
    protected void prepareDataForPatching(BaseSupplyBasket supplyBasket) {
        List<SkladSupply> supplyList = supplyBasket.getSuppliesListForUpdate();

        for (SkladSupply s : supplyList) {
            s.setPriceDollar(s.getPriceSupplierCurrency().divide(((SkladSupplyBasket)supplyBasket).getUsdSupplierCurrencyRate(), 2, RoundingMode.HALF_UP));
        }
    }
}
