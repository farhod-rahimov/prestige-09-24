package com.prestige.controllers.prestige;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeBasketsDAO;
import com.prestige.dao.prestige.PrestigeClientsDAO;
import com.prestige.dao.prestige.PrestigeProductsDAO;
import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.models.Basket;
import com.prestige.models.Brand;
import com.prestige.models.Sales;
import com.prestige.models.Settings;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping("/prestige/baskets")
@SessionAttributes({"step", "basket"})
public class PrestigeBasketsController {

    private PrestigeBasketsDAO prestigeBasketsDAO;

    private PrestigeClientsDAO prestigeClientsDAO;

    private PrestigeProductsDAO prestigeProductsDAO;

    private PrestigeSettingsDAO prestigeSettingsDAO;

    private CommonProductsDAO commonProductsDAO;

    private CommonProductNamesDAO commonProductNamesDAO;

    private CommonBrandNamesDAO commonBrandNamesDAO;

    @AllArgsConstructor
    @Getter
    private enum EStep {
        CLIENT_NAME_SELECTION("CLIENT_NAME_SELECTION"),
        CLIENT_PHONE_NUMBER_SELECTION("CLIENT_PHONE_NUMBER_SELECTION"),
        PRODUCT_NAME_SELECTION("PRODUCT_NAME_SELECTION"),
        PRODUCT_CAR_MODEL_SELECTION("PRODUCT_CAR_MODEL_SELECTION"),
        PRODUCT_ENGINE_MODEL_SELECTION("PRODUCT_ENGINE_MODEL_SELECTION"),
        PRODUCT_OEM_ARTICLE_NUMBER_SELECTION("PRODUCT_OEM_ARTICLE_NUMBER_SELECTION"),
        PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION("PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION"),
        PRODUCT_BRAND_SELECTION("PRODUCT_BRAND_SELECTION"),
        PRODUCT_QUANTITY_AND_PRICE_SELECTION("PRODUCT_QUANTITY_AND_PRICE_SELECTION");

        private final String value;
    }

    @Autowired
    public PrestigeBasketsController(PrestigeBasketsDAO prestigeBasketsDAO, PrestigeClientsDAO prestigeClientsDAO,
                                     PrestigeProductsDAO prestigeProductsDAO, PrestigeSettingsDAO prestigeSettingsDAO,
                                     CommonProductsDAO commonProductsDAO, CommonProductNamesDAO commonProductNamesDAO,
                                     CommonBrandNamesDAO commonBrandNamesDAO) {
        this.prestigeBasketsDAO = prestigeBasketsDAO;
        this.prestigeClientsDAO = prestigeClientsDAO;
        this.prestigeProductsDAO = prestigeProductsDAO;
        this.prestigeSettingsDAO = prestigeSettingsDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", prestigeBasketsDAO.findAllBaskets());
        return "prestige/baskets/baskets"; // return /prestige/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("basket", new Basket());
        model.addAttribute("step", EStep.CLIENT_NAME_SELECTION);
        model.addAttribute("allClientNames", prestigeClientsDAO.findUniqueClientNames());
        return "prestige/baskets/add-new-basket";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("basket") Basket basket, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
//            case "put":
//                return updateRetailProduct(retailProduct, bindingResult, redirectAttributes);
//            case "delete":
//                return deleteRetailProduct(retailProduct.getId());
            default:
                return saveNewBasket(basket, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
                return EStep.CLIENT_PHONE_NUMBER_SELECTION;
            case CLIENT_PHONE_NUMBER_SELECTION:
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
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    EStep processAndGetNextStep(Basket basket, Model model, EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
                return processClientNameSelection(basket, model, step);
            case CLIENT_PHONE_NUMBER_SELECTION:
                return processPhoneNumberSelection(basket, model, step);
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
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    private EStep processClientNameSelection(Basket basket, Model model, EStep step) {
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

    private EStep processPhoneNumberSelection(Basket basket, Model model, EStep step) {
        basket.getClient().setId(prestigeClientsDAO.findClientId(basket.getClient()));
        basket.setSalesList(new LinkedList<>());
        basket.getSalesList().add(new Sales());

        prestigeBasketsDAO.saveNewPrestigeBasket(basket);
        model.addAttribute("allProductNames", prestigeProductsDAO.findAllProductNames());

        return getNextStep(step);
    }

    private EStep processProductNameSelection(Basket basket, Model model, EStep step) {
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

    private EStep processProductCarModelSelection(Basket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allEngineModelsList = prestigeProductsDAO.findAvailableEngineModels(sales.getProduct());

        if (allEngineModelsList.size() == 1) {
            sales.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else if (allEngineModelsList.isEmpty()) {
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(step);
    }

    private EStep processProductEngineModelSelection(Basket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allOemArticleNumbersList = prestigeProductsDAO.findAvailableOemArticleNumbers(sales.getProduct());

        if (allOemArticleNumbersList.size() == 1) {
            sales.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductOemArticleNumberSelection(Basket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<String> allCrossArticleNumbersList = prestigeProductsDAO.findAvailableCrossArticleNumbers(sales.getProduct());

        if (allCrossArticleNumbersList.size() == 1) {
            sales.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductCrossArticleNumberSelection(Basket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);
        List<Brand> allBrandsList = prestigeProductsDAO.findAvailableBrands(sales.getProduct());

        if (allBrandsList.size() == 1) {
            sales.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(basket, model, getNextStep(step));
        }
        else if (allBrandsList.isEmpty()) {
            return processProductBrandSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(step);
    }

    private EStep processProductBrandSelection(Basket basket, Model model, EStep step) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);

        if (sales.getProduct().getBrand().getBrandName() == null) {
            sales.getProduct().setBrand(commonBrandNamesDAO.findBrandById(sales.getProduct().getBrand().getId()));
        }
        sales.getProduct().setId(commonProductsDAO.findProductId(sales.getProduct()));
        setPrivateInfos(basket);

        return getNextStep(step);
    }

    private void setPrivateInfos(Basket basket) {
        Sales sales = basket.getSalesList().get(basket.getSalesList().size() - 1);

        sales.setStockBeforeSale(prestigeProductsDAO.findStockBeforeSale(sales.getProduct(), basket));
        sales.setWholesalePrice(prestigeProductsDAO.findWholesalePrice(sales.getProduct()));
        sales.setCalculatedPrice(getCalculatedPrice(sales.getWholesalePrice()));
        sales.setRecommendedPrice(prestigeProductsDAO.findRecommendedPrice(sales.getProduct(), sales.getCalculatedPrice()));
    }

    private BigDecimal getCalculatedPrice(BigDecimal wholesalePrice) {
        Settings exchangeRateSettings = prestigeSettingsDAO.findSettingsByName("exchangeRate");
        Settings magicValueSettings = prestigeSettingsDAO.findSettingsByName("magicValue");

        if (exchangeRateSettings == null || magicValueSettings == null) {
            throw new RuntimeException("Settings 'exchangeRate' or 'magicValue' doesn't exist. Please initialize it in the settings");
        }

        BigDecimal exchangeRate = new BigDecimal(exchangeRateSettings.getValue());
        BigDecimal magicValue = new BigDecimal(magicValueSettings.getValue());

        return wholesalePrice.multiply(new BigDecimal(100).add(magicValue).divide(new BigDecimal(100)))
                             .multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);
    }

    private EStep processProductQuantityAndPriceSelection(Basket basket, Model model, EStep step) {
        // save into DB
        return step;
    }

    private String saveNewBasket(Basket basket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/baskets/add");
        }

        EStep step = processAndGetNextStep(basket, model, (EStep)model.getAttribute("step"));
        model.addAttribute("step", step);

        return "prestige/baskets/add-new-basket";
    }
}
