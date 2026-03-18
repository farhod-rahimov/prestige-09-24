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
import com.prestige.utils.SupplyTabState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/sklad/supplies/baskets")
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
        model.addAttribute(getModelBasketName(), new SkladSupplyBasket()); // mock

        return "sklad/supplies/baskets/baskets"; // return /sklad/supplies/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model, HttpSession session) {
        SupplyTabState state = new SupplyTabState(UUID.randomUUID().toString(), EStep.SUPPLIER_STORE_NAME_SELECTION, new SkladSupplyBasket());
        saveTabState(session, getTabStateMapName(), state);

        model.addAttribute("tabId", state.getTabId());
        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());
        model.addAttribute("allSupplierStoreNames", suppliersDAO.findUniqueSupplierStoreNames());

        return "sklad/supplies/baskets/add-new-basket";
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, HttpSession session, Model model) {
        return baseFindBasketById(basketId, session, model);
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladSupplyBasket") SkladSupplyBasket supplyBasket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "tabId", required = false) String tabId,
                                       @RequestParam(value = "selectedSuppliesIds", required = false) List<Integer> selectedSuppliesIds,
                                       BindingResult bindingResult, RedirectAttributes redirectAttributes,
                                       Model model, HttpSession session) {

        return basePostMethodDispatcher(supplyBasket, method, tabId, selectedSuppliesIds, bindingResult, redirectAttributes, model, session);
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

        if (supplyBasket.getFilter() != null) {
            supplyBasket.setFilter(null); // remove filter
        }
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

    @Override
    protected void patchProductQuantityAndPriceSelection(BaseSupplyBasket supplyBasket, BaseSupplyBasket supplyBasketForm) {
        int lastIndex = supplyBasket.getSuppliesList().size() - 1;

        SkladSupply supply = (SkladSupply)supplyBasket.getSuppliesList().get(lastIndex);
        SkladSupply supplyForm = (SkladSupply)supplyBasketForm.getSuppliesList().get(lastIndex);

        supply.setOrderedQuantity(supplyForm.getOrderedQuantity());
        supply.setReceivedQuantity(supplyForm.getReceivedQuantity());
        supply.setPriceSupplierCurrency(supplyForm.getPriceSupplierCurrency());
        supply.setImportCostPercentage(supplyForm.getImportCostPercentage());
    }

    @Override
    protected void patchDefault(BaseSupplyBasket supplyBasket, BaseSupplyBasket supplyBasketForm) {
        SkladSupplyBasket skladBasket = (SkladSupplyBasket)supplyBasket;
        SkladSupplyBasket skladBasketForm = (SkladSupplyBasket)supplyBasketForm;

        skladBasket.setUsdSupplierCurrencyRate(skladBasketForm.getUsdSupplierCurrencyRate());
        skladBasket.setBasketName(skladBasketForm.getBasketName());
        skladBasket.setNotes(skladBasketForm.getNotes());

        for (int i = 0; i < supplyBasket.getSuppliesList().size(); i++) {

            SkladSupply supply = (SkladSupply)supplyBasket.getSuppliesList().get(i);
            SkladSupply supplyForm = (SkladSupply)supplyBasketForm.getSuppliesList().get(i);

            supply.setOrderedQuantity(supplyForm.getOrderedQuantity());
            supply.setReceivedQuantity(supplyForm.getReceivedQuantity());
            supply.setPriceSupplierCurrency(supplyForm.getPriceSupplierCurrency());
            supply.setImportCostPercentage(supplyForm.getImportCostPercentage());
        }
    }

    @Override
    protected void patchBasket(BaseSupplyBasket supplyBasket) {
        supplyBasketsDAO.updateBasketNotes(supplyBasket);
        updateBasketTotalSum(supplyBasket);

        ((SkladSupplyBasketsDAO)supplyBasketsDAO).updateUsdSupplierCurrencyRate((SkladSupplyBasket)supplyBasket);
        ((SkladSupplyBasketsDAO)supplyBasketsDAO).updateBasketName((SkladSupplyBasket)supplyBasket);
    }
}
