package com.prestige.controllers.prestige;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.prestige.PrestigeSuppliersDAO;
import com.prestige.dao.prestige.PrestigeSupplyBasketsDAO;
import com.prestige.models.*;
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
@SessionAttributes({"supplyStep", "supplyBasket"})
public class PrestigeSupplyBasketsController {

    private PrestigeSupplyBasketsDAO prestigeSupplyBasketsDAO;

    private PrestigeSuppliersDAO prestigeSuppliersDAO;

    private PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;

    private CommonProductsDAO commonProductsDAO;

    private CommonProductNamesDAO commonProductNamesDAO;

    private CommonBrandNamesDAO commonBrandNamesDAO;


    @Autowired
    public PrestigeSupplyBasketsController(PrestigeSupplyBasketsDAO prestigeSupplyBasketsDAO,
                                           PrestigeSuppliersDAO prestigeSuppliersDAO,
                                           PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                           CommonProductsDAO commonProductsDAO,
                                           CommonProductNamesDAO commonProductNamesDAO,
                                           CommonBrandNamesDAO commonBrandNamesDAO) {
        this.prestigeSupplyBasketsDAO = prestigeSupplyBasketsDAO;
        this.prestigeSuppliersDAO = prestigeSuppliersDAO;
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", prestigeSupplyBasketsDAO.findAllBaskets());
        return "prestige/supplies/baskets/baskets"; // return /prestige/supplies/baskets/baskets.html
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("supplyBasket", new SupplyBasket());
        model.addAttribute("supplyStep", EStep.SUPPLIER_STORE_NAME_SELECTION);
        model.addAttribute("allSupplierStoreNames", prestigeSuppliersDAO.findUniqueSupplierStoreNames());
        return "prestige/supplies/baskets/add-new-basket";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("supplyBasket") SupplyBasket supplyBasket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSuppliesIds", required = false) List<Integer> selectedSuppliesIds,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "changeBasketType":
                return changeBasketType(supplyBasket, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(supplyBasket, model);
            case "changeSearchType":
                return changeSearchType(supplyBasket, model);
            case "markAsReceived":
                return markAsReceivedBasket(supplyBasket, bindingResult, redirectAttributes);
            case "deleteSelected":
                return deleteSelected(supplyBasket, selectedSuppliesIds);
            case "delete":
                return deleteBasket(supplyBasket, bindingResult, redirectAttributes);
            case "close":
                return "redirect:/prestige/supplies/baskets";
            case "patch":
                return patchBasket(supplyBasket, bindingResult, redirectAttributes, model);
            default:
                return saveNewBasket(supplyBasket, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep supplyStep) {

        switch (supplyStep) {
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
                throw new RuntimeException(String.format("Step is undefined: '%s'", supplyStep.getValue()));
        }
    }

    EStep processAndGetNextStep(SupplyBasket supplyBasket, Model model, EStep supplyStep) {

        switch (supplyStep) {
            case SUPPLIER_STORE_NAME_SELECTION:
                return processSupplierStoreNameSelection(supplyBasket, model, supplyStep);
            case SUPPLIER_MANAGER_NAME_SELECTION:
                return processSupplierManagerNameSelection(supplyBasket, model, supplyStep);
            case SUPPLIER_PHONE_NUMBER_SELECTION:
                return processSupplierPhoneNumberSelection(supplyBasket, model, supplyStep);
            case PRODUCT_SELECTION_WITH_FULL_LIST:
                return processProductSelectionWithFullList(supplyBasket, model, supplyStep);
            case PRODUCT_SELECTION_WITH_FILTER:
                return processProductSelectionWithFilter(supplyBasket, model, supplyStep);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(supplyBasket, model, supplyStep);
            case PRODUCT_CAR_MODEL_SELECTION:
                return processProductCarModelSelection(supplyBasket, model, supplyStep);
            case PRODUCT_ENGINE_MODEL_SELECTION:
                return processProductEngineModelSelection(supplyBasket, model, supplyStep);
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                return processProductOemArticleNumberSelection(supplyBasket, model, supplyStep);
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                return processProductCrossArticleNumberSelection(supplyBasket, model, supplyStep);
            case PRODUCT_BRAND_SELECTION:
                return processProductBrandSelection(supplyBasket, model, supplyStep);
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                return processProductQuantityAndPriceSelection(supplyBasket, model, supplyStep);
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(supplyBasket, model, supplyStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", supplyStep.getValue()));
        }
    }

    private EStep processSupplierStoreNameSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        List<String> allSupplierNamesList = prestigeSuppliersDAO.findSupplierNameByStoreName(supplyBasket.getSupplier().getStoreName());

        if (allSupplierNamesList.size() == 1) {
            supplyBasket.getSupplier().setName(allSupplierNamesList.get(0));
            return processSupplierManagerNameSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allSupplierNames", allSupplierNamesList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processSupplierManagerNameSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supplier supplier = supplyBasket.getSupplier();
        List<String> allSupplierPhoneNumbers = prestigeSuppliersDAO.findSupplierPhoneNumbersByStoreNameAndManagerName(supplier.getStoreName(), supplier.getName());

        if (allSupplierPhoneNumbers.size() == 1) {
            supplyBasket.getSupplier().setPhoneNumber(allSupplierPhoneNumbers.get(0));
            return processSupplierPhoneNumberSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allSupplierPhoneNumbers", allSupplierPhoneNumbers);
        }
        return getNextStep(supplyStep);
    }

    private EStep processSupplierPhoneNumberSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        supplyBasket.getSupplier().setId(prestigeSuppliersDAO.findSupplierId(supplyBasket.getSupplier()));
        supplyBasket.setSuppliesList(new LinkedList<>());
        supplyBasket.setStatus(SupplyBasket.EStatus.PENDING);

        prestigeSupplyBasketsDAO.saveNewBasket(supplyBasket);

        return getNextStep(supplyStep);
    }

    private EStep processProductSelectionWithFullList(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        prestigeAllSuppliesDAO.updateQuantityAndPrices(supplyBasket.getSuppliesListForUpdate());
        prestigeAllSuppliesDAO.saveNewPrestigeSuppliesList(supplyBasket, supplyBasket.getSuppliesListForSave());
        return supplyStep;
    }

    private EStep processProductSelectionWithFilter(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        supplyBasket.getFilter().prepareAllFields();
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNamesWithFilter(supplyBasket.getFilter()));
        return getNextStep(supplyStep);
    }

    private EStep processProductNameSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        supply.getProduct().setProductName(commonProductNamesDAO.findProductNameById(supply.getProduct().getProductName().getId()));
        List<String> allCarModelsList = commonProductsDAO.findCarModelsByProductNameId(supply.getProduct().getProductName().getId());

        if (allCarModelsList.size() == 1) {
            supply.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductCarModelSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allEngineModelsList = commonProductsDAO.findAvailableEngineModels(supply.getProduct());

        if (allEngineModelsList.size() == 1) {
            supply.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else if (allEngineModelsList.isEmpty()) {
            supply.getProduct().setEngineModel("");
            return processProductEngineModelSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductEngineModelSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allOemArticleNumbersList = commonProductsDAO.findAvailableOemArticleNumbers(supply.getProduct());

        if (allOemArticleNumbersList.size() == 1) {
            supply.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            supply.getProduct().setOemArticleNumber("");
            return processProductOemArticleNumberSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductOemArticleNumberSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allCrossArticleNumbersList = commonProductsDAO.findAvailableCrossArticleNumbers(supply.getProduct());

        if (allCrossArticleNumbersList.size() == 1) {
            supply.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            supply.getProduct().setCrossArticleNumber("");
            return processProductCrossArticleNumberSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductCrossArticleNumberSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<Brand> allBrandsList = commonProductsDAO.findAvailableBrands(supply.getProduct());

        if (allBrandsList.size() == 1) {
            supply.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductBrandSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);

        if (supply.getProduct().getBrand().getBrandName() == null) {
            supply.getProduct().setBrand(commonBrandNamesDAO.findBrandById(supply.getProduct().getBrand().getId()));
        }
        supply.getProduct().setId(commonProductsDAO.findProductId(supply.getProduct()));

        setPrivateInfos(supplyBasket, supplyBasket.getSuppliesList(), true);

        for (int i = 0; i < supplyBasket.getSuppliesList().size() - 1; i++) {

            if (supply.equalsOnlyProductWithoutNotes(supplyBasket.getSuppliesList().get(i))) {
                supplyBasket.getSuppliesList().remove(supplyBasket.getSuppliesList().size() - 1);

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
        return getNextStep(supplyStep);
    }

    private void setPrivateInfos(SupplyBasket supplyBasket, List<Supply> suppliesList, boolean onlyForLastSupplies) {
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

        Map<Integer, Integer> stockBeforeSaleMap = prestigeAllSuppliesDAO.findStockBeforeSale(productIds, supplyBasket);

        int suppliesListSize = suppliesList.size();

        for (int i = onlyForLastSupplies ? suppliesListSize - 1 : 0; i < suppliesListSize; i++) {
            Supply supply = suppliesList.get(i);
            supply.setStockBeforeSale(stockBeforeSaleMap.get(supply.getProduct().getId()));
        }
    }

    private void updateBasketTotalSum(SupplyBasket supplyBasket) {
        List<Supply> suppliesList = supplyBasket.getSuppliesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Supply supply : suppliesList) {
            totalSum = totalSum.add(supply.getPriceDollar().multiply(BigDecimal.valueOf(supply.getQuantity())));
        }
        supplyBasket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        prestigeAllSuppliesDAO.updateBasketTotalSum(supplyBasket);
    }

    private EStep processProductQuantityAndPriceSelection(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        Supply newSupply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);

        prestigeAllSuppliesDAO.saveNewPrestigeSupply(supplyBasket, newSupply);
        updateBasketTotalSum(supplyBasket);
        return getNextStep(supplyStep);
    }

    private EStep processProductSelectionDone(SupplyBasket supplyBasket, Model model, EStep supplyStep) {
        supplyBasket.getSuppliesList().add(new Supply());
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
        return getNextStep(supplyStep);
    }

    private void patchBasketInternal(SupplyBasket supplyBasket) {
        prestigeAllSuppliesDAO.saveNewPrestigeSuppliesList(supplyBasket, supplyBasket.getSuppliesListForSave());
        prestigeAllSuppliesDAO.updateQuantityAndPrices(supplyBasket.getSuppliesListForUpdate());
        prestigeSupplyBasketsDAO.updateBasketNotes(supplyBasket);
        updateBasketTotalSum(supplyBasket);
    }

    private String changeBasketStatus(SupplyBasket supplyBasket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, SupplyBasket.EStatus newStatus) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return String.format("redirect:/prestige/supplies/baskets/%d", supplyBasket.getId());
        }

        patchBasketInternal(supplyBasket); // перед обновлением статуса корзины обязательно обновить ее
        supplyBasket.setStatus(newStatus);
        prestigeSupplyBasketsDAO.updateBasketStatus(supplyBasket);

        return "redirect:/prestige/supplies/baskets";
    }

    private String changeBasketType(SupplyBasket supplyBasket, Model model) {
        EStep supplyStep = (EStep)model.getAttribute("supplyStep");
        List<Supply> suppliesList = supplyBasket.getSuppliesList();

        if (supplyStep == EStep.PRODUCT_SELECTION_DONE) {
            supplyStep = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(suppliesList.size());
            for (Supply supply : supplyBasket.getSuppliesList()) {
                excludeProductIds.add(supply.getProduct().getId());
            }
            List<Supply> fullSuppliesList = commonProductsDAO.findAllProductsWrappedInSupply(excludeProductIds);
            setPrivateInfos(supplyBasket, fullSuppliesList, false);

            suppliesList.addAll(fullSuppliesList);
        }
        else {
            supplyStep = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
            supplyBasket.setSuppliesList(supplyBasket.getSuppliesListForUpdate()); // delete all unsaved supplies
        }

        model.addAttribute("supplyStep", supplyStep);
        return "prestige/supplies/baskets/add-new-basket";
    }

    private String resetAddingNewProduct(SupplyBasket supplyBasket, Model model) {
        supplyBasket.getSuppliesList().remove(supplyBasket.getSuppliesList().size() - 1);
        model.addAttribute("supplyStep", EStep.PRODUCT_SELECTION_DONE);

        return "prestige/supplies/baskets/add-new-basket";
    }

    private String changeSearchType(SupplyBasket supplyBasket, Model model) {
        EStep supplyStep = (EStep)model.getAttribute("supplyStep");

        if (supplyStep != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            supplyStep = EStep.PRODUCT_SELECTION_WITH_FILTER;

            if (supplyBasket.getFilter() != null) {
                supplyBasket.getFilter().clear();
            }
        }
        else {
            supplyStep = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
        }
        model.addAttribute("supplyStep", supplyStep);
        return "prestige/supplies/baskets/add-new-basket";
    }

    private String markAsReceivedBasket(SupplyBasket supplyBasket, BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {
        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                supplyBasket.getStatus() == SupplyBasket.EStatus.DELETED ? "восстановлении" : "оприходовании");

        return changeBasketStatus(supplyBasket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.RECEIVED);
    }

    private String deleteSelected(SupplyBasket supplyBasket, List<Integer> selectedSuppliesIds) {

        if (selectedSuppliesIds != null && !selectedSuppliesIds.isEmpty()) {
            for (Integer supplyId : selectedSuppliesIds) {
                supplyBasket.getSuppliesList().removeIf(supply -> supply.getId().equals(supplyId));
            }

            prestigeAllSuppliesDAO.deleteSuppliesIds(selectedSuppliesIds);
            updateBasketTotalSum(supplyBasket);
        }
        return "redirect:/prestige/supplies/baskets/" + supplyBasket.getId();
    }

    private String deleteBasket(SupplyBasket supplyBasket, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(supplyBasket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.DELETED);
    }

    private String patchBasket(SupplyBasket supplyBasket, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        // обновляем кол-во и цену товаров и возвращаемся на страницу "/prestige/supplies/baskets"

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/supplies/baskets/%d", supplyBasket.getId());
        }

        patchBasketInternal(supplyBasket);

        return "redirect:/prestige/supplies/baskets";
    }

    private String saveNewBasket(SupplyBasket supplyBasket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/prestige/supplies/baskets/add");
        }

        EStep supplyStep = processAndGetNextStep(supplyBasket, model, (EStep)model.getAttribute("supplyStep"));
        model.addAttribute("supplyStep", supplyStep);

        return "prestige/supplies/baskets/add-new-basket";
    }

    private SupplyBasket findBasketById(Integer basketId) {

        try {
            SupplyBasket supplyBasket = prestigeSupplyBasketsDAO.findBasketById(basketId);
            supplyBasket.setSuppliesList(prestigeAllSuppliesDAO.findAllSuppliesByBasketId(basketId));
            setPrivateInfos(supplyBasket, supplyBasket.getSuppliesList(), false);
            return supplyBasket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/supplies/baskets/add-new-basket");
        SupplyBasket supplyBasket = findBasketById(basketId);

        if (supplyBasket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/supplies/baskets");
        }
        else {
            mav.addObject("supplyBasket", supplyBasket);
            mav.addObject("supplyStep", EStep.PRODUCT_SELECTION_DONE);
        }
        return mav;
    }
}
