package com.prestige.controllers.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.*;
import com.prestige.models.SalesBasket;
import com.prestige.models.Brand;
import com.prestige.models.Sales;
import com.prestige.models.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/prestige/sales/baskets")
@SessionAttributes({"step", "basket", "utils"})
public class PrestigeSalesBasketsController {

    private PrestigeSalesBasketsDAO prestigeSalesBasketsDAO;

    private PrestigeAllSalesDAO prestigeAllSalesDAO;

    private PrestigeClientsDAO prestigeClientsDAO;

    private PrestigeProductsDAO prestigeProductsDAO;

    private PrestigeSettingsDAO prestigeSettingsDAO;

    private CommonProductsDAO commonProductsDAO;

    private CommonProductNamesDAO commonProductNamesDAO;

    private CommonBrandNamesDAO commonBrandNamesDAO;

    private Utils utils;

    @AllArgsConstructor
    @Getter
    private enum EStep {
        CLIENT_NAME_SELECTION("CLIENT_NAME_SELECTION"),
        CLIENT_PHONE_NUMBER_SELECTION("CLIENT_PHONE_NUMBER_SELECTION"),
        PRODUCT_SELECTION_WITH_FULL_LIST("PRODUCT_SELECTION_WITH_FULL_LIST"),
        PRODUCT_SELECTION_WITH_FILTER("PRODUCT_SELECTION_WITH_FILTER"),
        PRODUCT_NAME_SELECTION("PRODUCT_NAME_SELECTION"),
        PRODUCT_CAR_MODEL_SELECTION("PRODUCT_CAR_MODEL_SELECTION"),
        PRODUCT_ENGINE_MODEL_SELECTION("PRODUCT_ENGINE_MODEL_SELECTION"),
        PRODUCT_OEM_ARTICLE_NUMBER_SELECTION("PRODUCT_OEM_ARTICLE_NUMBER_SELECTION"),
        PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION("PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION"),
        PRODUCT_BRAND_SELECTION("PRODUCT_BRAND_SELECTION"),
        PRODUCT_QUANTITY_AND_PRICE_SELECTION("PRODUCT_QUANTITY_AND_PRICE_SELECTION"),
        PRODUCT_SELECTION_DONE("PRODUCT_SELECTION_DONE");

        private final String value;
    }

    @Autowired
    public PrestigeSalesBasketsController(PrestigeSalesBasketsDAO prestigeSalesBasketsDAO, PrestigeAllSalesDAO prestigeAllSalesDAO,
                                          PrestigeClientsDAO prestigeClientsDAO, PrestigeProductsDAO prestigeProductsDAO,
                                          PrestigeSettingsDAO prestigeSettingsDAO, CommonProductsDAO commonProductsDAO,
                                          CommonProductNamesDAO commonProductNamesDAO, CommonBrandNamesDAO commonBrandNamesDAO,
                                          Utils utils) {
        this.prestigeSalesBasketsDAO = prestigeSalesBasketsDAO;
        this.prestigeAllSalesDAO = prestigeAllSalesDAO;
        this.prestigeClientsDAO = prestigeClientsDAO;
        this.prestigeProductsDAO = prestigeProductsDAO;
        this.prestigeSettingsDAO = prestigeSettingsDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;

        this.utils = utils;
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", prestigeSalesBasketsDAO.findAllBaskets());
        return "prestige/sales/baskets/baskets"; // return /prestige/sales/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("basket", new SalesBasket());
        model.addAttribute("utils", utils);
        model.addAttribute("step", EStep.CLIENT_NAME_SELECTION);
        model.addAttribute("allClientNames", prestigeClientsDAO.findUniqueClientNames());
        return "prestige/sales/baskets/add-new-basket";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("basket") SalesBasket basket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSalesIds", required = false) List<Integer> selectedSalesIds,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "changeBasketType":
                return changeBasketType(basket, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(basket, model);
            case "changeSearchType":
                return changeSearchType(basket, model);
            case "markAsSold":
                return markAsSoldBasket(basket, bindingResult, redirectAttributes);
            case "deleteSelected":
                return deleteSelected(basket, selectedSalesIds);
            case "delete":
                return deleteBasket(basket, bindingResult, redirectAttributes);
            case "close":
                return "redirect:/prestige/sales/baskets";
            case "patch":
                return patchBasket(basket, bindingResult, redirectAttributes, model);
            default:
                return saveNewBasket(basket, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
                return EStep.CLIENT_PHONE_NUMBER_SELECTION;
            case CLIENT_PHONE_NUMBER_SELECTION:
                return EStep.PRODUCT_SELECTION_DONE;
            case PRODUCT_SELECTION_DONE:
            case PRODUCT_SELECTION_WITH_FILTER:
                return EStep.PRODUCT_NAME_SELECTION;
            case PRODUCT_NAME_SELECTION:
                return EStep.PRODUCT_CAR_MODEL_SELECTION;
            case PRODUCT_CAR_MODEL_SELECTION:
                return EStep.PRODUCT_ENGINE_MODEL_SELECTION;
            case PRODUCT_ENGINE_MODEL_SELECTION:
                return EStep.PRODUCT_OEM_ARTICLE_NUMBER_SELECTION;
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                return EStep.PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION;
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                return EStep.PRODUCT_BRAND_SELECTION;
            case PRODUCT_BRAND_SELECTION:
                return EStep.PRODUCT_QUANTITY_AND_PRICE_SELECTION;
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                return EStep.PRODUCT_SELECTION_DONE;
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    EStep processAndGetNextStep(SalesBasket basket, Model model, EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
                return processClientNameSelection(basket, model, step);
            case CLIENT_PHONE_NUMBER_SELECTION:
                return processPhoneNumberSelection(basket, model, step);
            case PRODUCT_SELECTION_WITH_FILTER:
                return processProductSelectionWithFilter(basket, model, step);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(basket, model, step);
            case PRODUCT_CAR_MODEL_SELECTION:
                return processProductCarModelSelection(basket, model, step);
            case PRODUCT_ENGINE_MODEL_SELECTION:
                return processProductEngineModelSelection(basket, model, step);
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                return processProductOemArticleNumberSelection(basket, model, step);
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                return processProductCrossArticleNumberSelection(basket, model, step);
            case PRODUCT_BRAND_SELECTION:
                return processProductBrandSelection(basket, model, step);
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                return processProductQuantityAndPriceSelection(basket, model, step);
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(basket, model, step);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    private EStep processClientNameSelection(SalesBasket basket, Model model, EStep step) {
        List<String> allPhoneNumbersList = prestigeClientsDAO.findPhoneNumbersByClientName(basket.getClient().getName());

        if (allPhoneNumbersList.size() == 1) {
            basket.getClient().setPhoneNumber(allPhoneNumbersList.get(0));
            return processPhoneNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allPhoneNumbers", allPhoneNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processPhoneNumberSelection(SalesBasket basket, Model model, EStep step) {
        basket.getClient().setId(prestigeClientsDAO.findClientId(basket.getClient()));
        basket.setSalesList(new LinkedList<>());
        basket.setStatus(SalesBasket.EStatus.PENDING);

        prestigeSalesBasketsDAO.saveNewPrestigeBasket(basket);

        return getNextStep(step);
    }

    private EStep processProductSelectionWithFilter(SalesBasket basket, Model model, EStep step) {
        basket.getFilter().prepareAllFields();
        model.addAttribute("allProductNames", prestigeProductsDAO.findAllProductNamesWithFilter(basket.getFilter()));
        return getNextStep(step);
    }

    private EStep processProductNameSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        sales.getProduct().setProductName(commonProductNamesDAO.findProductNameById(sales.getProduct().getProductName().getId()));
        List<String> allCarModelsList = prestigeProductsDAO.findAvailableCarModels(sales.getProduct());

        if (allCarModelsList.size() == 1) {
            sales.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(step);
    }

    private EStep processProductCarModelSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allEngineModelsList = prestigeProductsDAO.findAvailableEngineModels(sales.getProduct());

        if (allEngineModelsList.size() == 1) {
            sales.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else if (allEngineModelsList.isEmpty()) {
            sales.getProduct().setEngineModel("");
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(step);
    }

    private EStep processProductEngineModelSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allOemArticleNumbersList = prestigeProductsDAO.findAvailableOemArticleNumbers(sales.getProduct());

        if (allOemArticleNumbersList.size() == 1) {
            sales.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            sales.getProduct().setOemArticleNumber("");
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductOemArticleNumberSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allCrossArticleNumbersList = prestigeProductsDAO.findAvailableCrossArticleNumbers(sales.getProduct());

        if (allCrossArticleNumbersList.size() == 1) {
            sales.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            sales.getProduct().setCrossArticleNumber("");
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductCrossArticleNumberSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<Brand> allBrandsList = prestigeProductsDAO.findAvailableBrands(sales.getProduct());

        if (allBrandsList.size() == 1) {
            sales.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(step);
    }

    private EStep processProductBrandSelection(SalesBasket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);

        if (sales.getProduct().getBrand().getBrandName() == null) {
            sales.getProduct().setBrand(commonBrandNamesDAO.findBrandById(sales.getProduct().getBrand().getId()));
        }
        sales.getProduct().setId(commonProductsDAO.findProductId(sales.getProduct()));

        setPrivateInfos(basket, basket.getSalesList(), true);

        for (int i = 0; i < basket.getSalesList().size() - 1; i++) {

            if (sales.equalsOnlyProductWithoutNotes(basket.getSalesList().get(i))) {
                basket.getSalesList().remove(basket.getSalesList().size() - 1);

                String errorMsg = String.format("|%s| |%s| |%s| |%s| |%s| |%s| уже есть в корзине!",
                        sales.getProduct().getProductName().getProductName(),
                        sales.getProduct().getCarModel(),
                        sales.getProduct().getEngineModel(),
                        sales.getProduct().getOemArticleNumber(),
                        sales.getProduct().getCrossArticleNumber(),
                        sales.getProduct().getBrand().getBrandName());

                model.addAttribute("error", errorMsg);
                return EStep.PRODUCT_SELECTION_DONE;
            }
        }
        return getNextStep(step);
    }

    private void setPrivateInfos(SalesBasket basket, List<Sales> salesList, boolean onlyForLastSales) {
        Set<Integer> productIds;

        if (salesList.isEmpty()) {
            return;
        }

        if (onlyForLastSales) {
            productIds = new HashSet<>();
            productIds.add(salesList.get(salesList.size() - 1).getProduct().getId());
        }
        else {
            productIds = salesList.stream()
                    .map(s -> s.getProduct().getId())
                    .collect(Collectors.toCollection(HashSet::new));
        }

        Map<Integer, Integer> stockBeforeSaleMap = prestigeProductsDAO.findStockBeforeSale(productIds, basket);
        Map<Integer, BigDecimal> wholeSalePriceMap = prestigeProductsDAO.findWholesalePrice(productIds);
        Settings exchangeRateSettings = prestigeSettingsDAO.findSettingsByName("exchangeRate");
        Settings magicValueSettings = prestigeSettingsDAO.findSettingsByName("magicValue");
        Map<Integer, BigDecimal> recommendedPriceMap = prestigeProductsDAO.findRecommendedPrice(productIds, basket);

        int salesListSize = salesList.size();

        for (int i = onlyForLastSales ? salesListSize - 1 : 0; i < salesListSize; i++) {
            Sales sales = salesList.get(i);

            sales.setStockBeforeSale(stockBeforeSaleMap.get(sales.getProduct().getId()));
            sales.setWholesalePrice(wholeSalePriceMap.get(sales.getProduct().getId()));
            sales.setCalculatedPrice(getCalculatedPrice(sales.getWholesalePrice(), exchangeRateSettings, magicValueSettings));

            if (recommendedPriceMap.get(sales.getProduct().getId()) == null) {
                sales.setRecommendedPrice(sales.getCalculatedPrice());
            }
            else {
                sales.setRecommendedPrice(recommendedPriceMap.get(sales.getProduct().getId()));
            }
        }
    }

    private BigDecimal getCalculatedPrice(BigDecimal wholesalePrice, Settings exchangeRateSettings, Settings magicValueSettings) {
        if (exchangeRateSettings == null || magicValueSettings == null) {
            throw new RuntimeException("Settings 'exchangeRate' or 'magicValue' doesn't exist. Please initialize it in the settings");
        }

        BigDecimal exchangeRate = new BigDecimal(exchangeRateSettings.getValue());
        BigDecimal magicValue = new BigDecimal(magicValueSettings.getValue());

        return wholesalePrice.multiply(new BigDecimal(100).add(magicValue).divide(new BigDecimal(100)))
                             .multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private void updateBasketTotalSum(SalesBasket basket) {
        List<Sales> salesList = basket.getSalesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Sales sales : salesList) {
            totalSum = totalSum.add(sales.getSoldPrice().multiply(BigDecimal.valueOf(sales.getQuantity())));
        }
        basket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        prestigeSalesBasketsDAO.updateBasketTotalSum(basket);
    }

    private EStep processProductQuantityAndPriceSelection(SalesBasket basket, Model model, EStep step) {
        Sales newSales = basket.getSalesList().get(basket.getSalesList().size() - 1);

        prestigeAllSalesDAO.saveNewPrestigeSale(basket, newSales);
        updateBasketTotalSum(basket);
        return getNextStep(step);
    }

    private EStep processProductSelectionDone(SalesBasket basket, Model model, EStep eStep) {
        basket.getSalesList().add(new Sales());
        model.addAttribute("allProductNames", prestigeProductsDAO.findAllProductNames());

        return getNextStep(eStep);
    }

    private String changeBasketStatus(SalesBasket basket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, SalesBasket.EStatus newStatus) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return String.format("redirect:/prestige/sales/baskets/%d", basket.getId());
        }

        patchBasketInternal(basket); // перед обновлением статуса корзины обязательно обновить ее
        basket.setStatus(newStatus);
        prestigeSalesBasketsDAO.updateBasketStatus(basket);

        return "redirect:/prestige/sales/baskets";
    }

    private String deleteBasket(SalesBasket basket, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(basket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.DELETED);
    }

    private String changeBasketType(SalesBasket basket, Model model) {
        EStep step = (EStep)model.getAttribute("step");
        List<Sales> salesList = basket.getSalesList();

        if (step == EStep.PRODUCT_SELECTION_DONE) {
            step = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(salesList.size());
            for (Sales sales : basket.getSalesList()) {
                excludeProductIds.add(sales.getProduct().getId());
            }
            List<Sales> fullSalesList = prestigeProductsDAO.findAllPrestigeProductsWrappedInSales(excludeProductIds);
            setPrivateInfos(basket, fullSalesList, false);

            salesList.addAll(fullSalesList);
        }
        else {
            step = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", prestigeProductsDAO.findAllProductNames());
            basket.setSalesList(basket.getSalesListForUpdate()); // delete all unsaved sales
        }

        model.addAttribute("step", step);
        return "prestige/sales/baskets/add-new-basket";
    }

    private String resetAddingNewProduct(SalesBasket basket, Model model) {
        basket.getSalesList().remove(basket.getSalesList().size() - 1);
        model.addAttribute("step", EStep.PRODUCT_SELECTION_DONE);

        return "prestige/sales/baskets/add-new-basket";
    }

    private String changeSearchType(SalesBasket basket, Model model) {
        EStep step = (EStep)model.getAttribute("step");

        if (step != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            step = EStep.PRODUCT_SELECTION_WITH_FILTER;

            if (basket.getFilter() != null) {
                basket.getFilter().clear();
            }
        }
        else {
            step = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", prestigeProductsDAO.findAllProductNames());
        }
        model.addAttribute("step", step);
        return "prestige/sales/baskets/add-new-basket";
    }

    private String markAsSoldBasket(SalesBasket basket, BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                basket.getStatus() == SalesBasket.EStatus.DELETED ? "восстановлении" : "продаже");

        return changeBasketStatus(basket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.SOLD);
    }

    private String deleteSelected(SalesBasket basket, List<Integer> selectedSalesIds) {
        for (Integer salesId : selectedSalesIds) {
            basket.getSalesList().removeIf(sales -> sales.getId().equals(salesId));
        }

        prestigeAllSalesDAO.deleteSalesIds(selectedSalesIds);
        updateBasketTotalSum(basket);
        return "redirect:/prestige/sales/baskets/" + basket.getId();
    }

    private void patchBasketInternal(SalesBasket basket) {
        prestigeAllSalesDAO.saveNewPrestigeSalesList(basket, basket.getSalesListForSave());
        prestigeAllSalesDAO.updateQuantityAndPrices(basket.getSalesListForUpdate());
        prestigeSalesBasketsDAO.updateBasketNotes(basket);
        updateBasketTotalSum(basket);
    }

    private String patchBasket(SalesBasket basket, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        // обновляем кол-во и цену товаров и возвращаемся на страницу "/prestige/sales/baskets"

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/sales/baskets/%d", basket.getId());
        }

        patchBasketInternal(basket);

        return "redirect:/prestige/sales/baskets";
    }

    private String saveNewBasket(SalesBasket basket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/sales/baskets/add");
        }

        EStep step = processAndGetNextStep(basket, model, (EStep)model.getAttribute("step"));
        model.addAttribute("step", step);

        return "prestige/sales/baskets/add-new-basket";
    }

    private SalesBasket findBasketById(Integer basketId) {

        try {
            SalesBasket basket = prestigeSalesBasketsDAO.findBasketById(basketId);
            basket.setSalesList(prestigeAllSalesDAO.findAllSalesByBasketId(basketId));
            setPrivateInfos(basket, basket.getSalesList(), false);
            return basket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/sales/baskets/add-new-basket");
        SalesBasket basket = findBasketById(basketId);

        if (basket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/sales/baskets");
        }
        else {
            mav.addObject("basket", basket);
            mav.addObject("step", EStep.PRODUCT_SELECTION_DONE);
        }
        return mav;
    }
}
