package com.prestige.controllers.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.prestige.PrestigeSuppliersDAO;
import com.prestige.dao.prestige.PrestigeSupplyBasketsDAO;
import com.prestige.models.*;
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
@RequestMapping("/prestige/supplies/baskets")
@SessionAttributes({"step", "basket", "utils"})
public class PrestigeSupplyBasketsController {

    private PrestigeSupplyBasketsDAO prestigeSupplyBasketsDAO;

    private PrestigeSuppliersDAO prestigeSuppliersDAO;

    private PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;

    private CommonProductsDAO commonProductsDAO;

    private CommonProductNamesDAO commonProductNamesDAO;

    private CommonBrandNamesDAO commonBrandNamesDAO;

    private Utils utils;

    @AllArgsConstructor
    @Getter
    private enum EStep {
        SUPPLIER_STORE_NAME_SELECTION("SUPPLIER_STORE_NAME_SELECTION"),
        SUPPLIER_MANAGER_NAME_SELECTION("SUPPLIER_MANAGER_NAME_SELECTION"),
        SUPPLIER_PHONE_NUMBER_SELECTION("SUPPLIER_PHONE_NUMBER_SELECTION"),
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
    public PrestigeSupplyBasketsController(PrestigeSupplyBasketsDAO prestigeSupplyBasketsDAO,
                                           PrestigeSuppliersDAO prestigeSuppliersDAO,
                                           PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                           CommonProductsDAO commonProductsDAO,
                                           CommonProductNamesDAO commonProductNamesDAO,
                                           CommonBrandNamesDAO commonBrandNamesDAO,
                                           Utils utils) {
        this.prestigeSupplyBasketsDAO = prestigeSupplyBasketsDAO;
        this.prestigeSuppliersDAO = prestigeSuppliersDAO;
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
        this.utils = utils;
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", prestigeSupplyBasketsDAO.findAllBaskets());
        return "prestige/supplies/baskets/baskets"; // return /prestige/supplies/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("basket", new SupplyBasket());
        model.addAttribute("utils", utils);
        model.addAttribute("step", EStep.SUPPLIER_STORE_NAME_SELECTION);
        model.addAttribute("allSupplierStoreNames", prestigeSuppliersDAO.findUniqueSupplierStoreNames());
        return "prestige/supplies/baskets/add-new-basket";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("basket") SupplyBasket basket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSuppliesIds", required = false) List<Integer> selectedSuppliesIds,
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
            case "markAsReceived":
                return markAsReceivedBasket(basket, bindingResult, redirectAttributes);
            case "deleteSelected":
                return deleteSelected(basket, selectedSuppliesIds);
            case "delete":
                return deleteBasket(basket, bindingResult, redirectAttributes);
            case "close":
                return "redirect:/prestige/supplies/baskets";
            case "patch":
                return patchBasket(basket, bindingResult, redirectAttributes, model);
            default:
                return saveNewBasket(basket, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep step) {

        switch (step) {
            case SUPPLIER_STORE_NAME_SELECTION:
                return EStep.SUPPLIER_MANAGER_NAME_SELECTION;
            case SUPPLIER_MANAGER_NAME_SELECTION:
                return EStep.SUPPLIER_PHONE_NUMBER_SELECTION;
            case SUPPLIER_PHONE_NUMBER_SELECTION:
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

    EStep processAndGetNextStep(SupplyBasket basket, Model model, EStep step) {

        switch (step) {
            case SUPPLIER_STORE_NAME_SELECTION:
                return processSupplierStoreNameSelection(basket, model, step);
            case SUPPLIER_MANAGER_NAME_SELECTION:
                return processSupplierManagerNameSelection(basket, model, step);
            case SUPPLIER_PHONE_NUMBER_SELECTION:
                return processSupplierPhoneNumberSelection(basket, model, step);
            case PRODUCT_SELECTION_WITH_FULL_LIST:
                return processProductSelectionWithFullList(basket, model, step);
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

    private EStep processSupplierStoreNameSelection(SupplyBasket basket, Model model, EStep step) {
        List<String> allSupplierNamesList = prestigeSuppliersDAO.findSupplierNameByStoreName(basket.getSupplier().getStoreName());

        if (allSupplierNamesList.size() == 1) {
            basket.getSupplier().setName(allSupplierNamesList.get(0));
            return processSupplierManagerNameSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allSupplierNames", allSupplierNamesList);
        }
        return getNextStep(step);
    }

    private EStep processSupplierManagerNameSelection(SupplyBasket basket, Model model, EStep step) {
        Supplier supplier = basket.getSupplier();
        List<String> allSupplierPhoneNumbers = prestigeSuppliersDAO.findSupplierPhoneNumbersByStoreNameAndManagerName(supplier.getStoreName(), supplier.getName());

        if (allSupplierPhoneNumbers.size() == 1) {
            basket.getSupplier().setPhoneNumber(allSupplierPhoneNumbers.get(0));
            return processSupplierPhoneNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allSupplierPhoneNumbers", allSupplierPhoneNumbers);
        }
        return getNextStep(step);
    }

    private EStep processSupplierPhoneNumberSelection(SupplyBasket basket, Model model, EStep step) {
        basket.getSupplier().setId(prestigeSuppliersDAO.findSupplierId(basket.getSupplier()));
        basket.setSuppliesList(new LinkedList<>());
        basket.setStatus(SupplyBasket.EStatus.PENDING);

        prestigeSupplyBasketsDAO.saveNewBasket(basket);

        return getNextStep(step);
    }

    private EStep processProductSelectionWithFullList(SupplyBasket basket, Model model, EStep step) {
        prestigeAllSuppliesDAO.updateQuantityAndPrices(basket.getSuppliesListForUpdate());
        prestigeAllSuppliesDAO.saveNewPrestigeSuppliesList(basket, basket.getSuppliesListForSave());
        return step;
    }

    private EStep processProductSelectionWithFilter(SupplyBasket basket, Model model, EStep step) {
        basket.getFilter().prepareAllFields();
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNamesWithFilter(basket.getFilter()));
        return getNextStep(step);
    }

    private EStep processProductNameSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);
        supply.getProduct().setProductName(commonProductNamesDAO.findProductNameById(supply.getProduct().getProductName().getId()));
        List<String> allCarModelsList = commonProductsDAO.findCarModelsByProductNameId(supply.getProduct().getProductName().getId());

        if (allCarModelsList.size() == 1) {
            supply.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(step);
    }

    private EStep processProductCarModelSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);
        List<String> allEngineModelsList = commonProductsDAO.findAvailableEngineModels(supply.getProduct());

        if (allEngineModelsList.size() == 1) {
            supply.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else if (allEngineModelsList.isEmpty()) {
            supply.getProduct().setEngineModel("");
            return processProductEngineModelSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(step);
    }

    private EStep processProductEngineModelSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);
        List<String> allOemArticleNumbersList = commonProductsDAO.findAvailableOemArticleNumbers(supply.getProduct());

        if (allOemArticleNumbersList.size() == 1) {
            supply.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            supply.getProduct().setOemArticleNumber("");
            return processProductOemArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductOemArticleNumberSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);
        List<String> allCrossArticleNumbersList = commonProductsDAO.findAvailableCrossArticleNumbers(supply.getProduct());

        if (allCrossArticleNumbersList.size() == 1) {
            supply.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            supply.getProduct().setCrossArticleNumber("");
            return processProductCrossArticleNumberSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(step);
    }

    private EStep processProductCrossArticleNumberSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);
        List<Brand> allBrandsList = commonProductsDAO.findAvailableBrands(supply.getProduct());

        if (allBrandsList.size() == 1) {
            supply.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(basket, model, getNextStep(step));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(step);
    }

    private EStep processProductBrandSelection(SupplyBasket basket, Model model, EStep step) {
        Supply supply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);

        if (supply.getProduct().getBrand().getBrandName() == null) {
            supply.getProduct().setBrand(commonBrandNamesDAO.findBrandById(supply.getProduct().getBrand().getId()));
        }
        supply.getProduct().setId(commonProductsDAO.findProductId(supply.getProduct()));

        setPrivateInfos(basket, basket.getSuppliesList(), true);

        for (int i = 0; i < basket.getSuppliesList().size() - 1; i++) {

            if (supply.equalsOnlyProductWithoutNotes(basket.getSuppliesList().get(i))) {
                basket.getSuppliesList().remove(basket.getSuppliesList().size() - 1);

                String errorMsg = String.format("|%s| |%s| |%s| |%s| |%s| |%s| уже есть в корзине!",
                        supply.getProduct().getProductName().getProductName(),
                        supply.getProduct().getCarModel(),
                        supply.getProduct().getEngineModel(),
                        supply.getProduct().getOemArticleNumber(),
                        supply.getProduct().getCrossArticleNumber(),
                        supply.getProduct().getBrand().getBrandName());

                model.addAttribute("error", errorMsg);
                return EStep.PRODUCT_SELECTION_DONE;
            }
        }
        return getNextStep(step);
    }

    private void setPrivateInfos(SupplyBasket basket, List<Supply> suppliesList, boolean onlyForLastSupplies) {
        Set<Integer> productIds;

        if (suppliesList.isEmpty()) {
            return;
        }

        if (onlyForLastSupplies) {
            productIds = new HashSet<>();
            productIds.add(suppliesList.get(suppliesList.size() - 1).getProduct().getId());
        }
        else {
            productIds = suppliesList.stream()
                    .map(s -> s.getProduct().getId())
                    .collect(Collectors.toCollection(HashSet::new));
        }

        Map<Integer, Integer> stockBeforeSaleMap = prestigeAllSuppliesDAO.findStockBeforeSale(productIds, basket);

        int suppliesListSize = suppliesList.size();

        for (int i = onlyForLastSupplies ? suppliesListSize - 1 : 0; i < suppliesListSize; i++) {
            Supply supply = suppliesList.get(i);
            supply.setStockBeforeSale(stockBeforeSaleMap.get(supply.getProduct().getId()));
        }
    }

    private void updateBasketTotalSum(SupplyBasket basket) {
        List<Supply> suppliesList = basket.getSuppliesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Supply supply : suppliesList) {
            totalSum = totalSum.add(supply.getPriceDollar().multiply(BigDecimal.valueOf(supply.getQuantity())));
        }
        basket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        prestigeAllSuppliesDAO.updateBasketTotalSum(basket);
    }

    private EStep processProductQuantityAndPriceSelection(SupplyBasket basket, Model model, EStep step) {
        Supply newSupply = basket.getSuppliesList().get(basket.getSuppliesList().size() - 1);

        prestigeAllSuppliesDAO.saveNewPrestigeSupply(basket, newSupply);
        updateBasketTotalSum(basket);
        return getNextStep(step);
    }

    private EStep processProductSelectionDone(SupplyBasket basket, Model model, EStep step) {
        basket.getSuppliesList().add(new Supply());
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
        return getNextStep(step);
    }

    private void patchBasketInternal(SupplyBasket basket) {
        prestigeAllSuppliesDAO.saveNewPrestigeSuppliesList(basket, basket.getSuppliesListForSave());
        prestigeAllSuppliesDAO.updateQuantityAndPrices(basket.getSuppliesListForUpdate());
        prestigeSupplyBasketsDAO.updateBasketNotes(basket);
        updateBasketTotalSum(basket);
    }

    private String changeBasketStatus(SupplyBasket basket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, SupplyBasket.EStatus newStatus) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return String.format("redirect:/prestige/supplies/baskets/%d", basket.getId());
        }

        patchBasketInternal(basket); // перед обновлением статуса корзины обязательно обновить ее
        basket.setStatus(newStatus);
        prestigeSupplyBasketsDAO.updateBasketStatus(basket);

        return "redirect:/prestige/supplies/baskets";
    }

    private String changeBasketType(SupplyBasket basket, Model model) {
        EStep step = (EStep)model.getAttribute("step");
        List<Supply> suppliesList = basket.getSuppliesList();

        if (step == EStep.PRODUCT_SELECTION_DONE) {
            step = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(suppliesList.size());
            for (Supply supply : basket.getSuppliesList()) {
                excludeProductIds.add(supply.getProduct().getId());
            }
            List<Supply> fullSuppliesList = commonProductsDAO.findAllProductsWrappedInSupply(excludeProductIds);
            setPrivateInfos(basket, fullSuppliesList, false);

            suppliesList.addAll(fullSuppliesList);
        }
        else {
            step = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
            basket.setSuppliesList(basket.getSuppliesListForUpdate()); // delete all unsaved sales
        }

        model.addAttribute("step", step);
        return "prestige/supplies/baskets/add-new-basket";
    }

    private String resetAddingNewProduct(SupplyBasket basket, Model model) {
        basket.getSuppliesList().remove(basket.getSuppliesList().size() - 1);
        model.addAttribute("step", EStep.PRODUCT_SELECTION_DONE);

        return "prestige/supplies/baskets/add-new-basket";
    }

    private String changeSearchType(SupplyBasket basket, Model model) {
        EStep step = (EStep)model.getAttribute("step");

        if (step != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            step = EStep.PRODUCT_SELECTION_WITH_FILTER;

            if (basket.getFilter() != null) {
                basket.getFilter().clear();
            }
        }
        else {
            step = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
        }
        model.addAttribute("step", step);
        return "prestige/supplies/baskets/add-new-basket";
    }

    private String markAsReceivedBasket(SupplyBasket basket, BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                basket.getStatus() == SupplyBasket.EStatus.DELETED ? "восстановлении" : "оприходовании");

        return changeBasketStatus(basket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.RECEIVED);
    }

    private String deleteSelected(SupplyBasket basket, List<Integer> selectedSuppliesIds) {
        for (Integer supplyId : selectedSuppliesIds) {
            basket.getSuppliesList().removeIf(supply -> supply.getId().equals(supplyId));
        }

        prestigeAllSuppliesDAO.deleteSuppliesIds(selectedSuppliesIds);
        updateBasketTotalSum(basket);
        return "redirect:/prestige/supplies/baskets/" + basket.getId();
    }

    private String deleteBasket(SupplyBasket basket, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(basket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.DELETED);
    }

    private String patchBasket(SupplyBasket basket, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        // обновляем кол-во и цену товаров и возвращаемся на страницу "/prestige/supplies/baskets"

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/supplies/baskets/%d", basket.getId());
        }

        patchBasketInternal(basket);

        return "redirect:/prestige/supplies/baskets";
    }

    private String saveNewBasket(SupplyBasket basket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/supplies/baskets/add");
        }

        EStep step = processAndGetNextStep(basket, model, (EStep)model.getAttribute("step"));
        model.addAttribute("step", step);

        return "prestige/supplies/baskets/add-new-basket";
    }

    private SupplyBasket findBasketById(Integer basketId) {

        try {
            SupplyBasket basket = prestigeSupplyBasketsDAO.findBasketById(basketId);
            basket.setSuppliesList(prestigeAllSuppliesDAO.findAllSuppliesByBasketId(basketId));
            setPrivateInfos(basket, basket.getSuppliesList(), false);
            return basket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/supplies/baskets/add-new-basket");
        SupplyBasket basket = findBasketById(basketId);

        if (basket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/supplies/baskets");
        }
        else {
            mav.addObject("basket", basket);
            mav.addObject("step", PrestigeSupplyBasketsController.EStep.PRODUCT_SELECTION_DONE);
        }
        return mav;
    }
}
