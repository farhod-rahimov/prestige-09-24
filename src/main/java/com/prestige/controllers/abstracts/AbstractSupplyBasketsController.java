package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.*;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.models.*;
import com.prestige.models.enums.EStep;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractSupplyBasketsController<S extends BaseSupply> {

    private final Class<S> supplyClass;

    protected final AbstractSettingsDAO settingsDAO;

    protected final AbstractSupplyBasketsDAO supplyBasketsDAO;

    protected final AbstractSuppliersDAO suppliersDAO;

    protected final AbstractAllSuppliesDAO allSuppliesDAO;

    protected final AbstractAllSalesDAO allSalesDAO;

    protected final CommonProductsDAO commonProductsDAO;

    protected final CommonProductNamesDAO commonProductNamesDAO;

    protected final CommonBrandNamesDAO commonBrandNamesDAO;

    protected final String uriPrefix;

    private String getModelStepName() {
        return Supply.class.equals(supplyClass) ? "supplyStep" : "skladSupplyStep";
    }

    private String getModelBasketName() {
        return Supply.class.equals(supplyClass) ? "supplyBasket" : "skladSupplyBasket";
    }

    protected abstract void updateBasketTotalSum(BaseSupplyBasket supplyBasket);

    protected abstract EStep processSupplierPhoneNumberSelection(BaseSupplyBasket supplyBasket, EStep supplyStep);

    protected abstract EStep processProductQuantityAndPriceSelection(BaseSupplyBasket supplyBasket, EStep supplyStep);

    protected abstract EStep processProductSelectionDone(BaseSupplyBasket supplyBasket, Model model, EStep supplyStep);

    protected abstract void prepareDataForPatching(BaseSupplyBasket supplyBasket);

    protected String basePostMethodDispatcher(BaseSupplyBasket<S> supplyBasket, String method, List<Integer> selectedSuppliesIds,
                                              BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        switch (method) {
            case "deleteAllEmptyBaskets":
                return deleteAllEmptyBaskets();
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
                return String.format("redirect:/%s/supplies/baskets", uriPrefix);
            case "patch":
                return patchBasket(supplyBasket, bindingResult, redirectAttributes, model, false);
            case "patchAndClose":
                return patchBasket(supplyBasket, bindingResult, redirectAttributes, model, true);
            default:
                return saveNewBasket(supplyBasket, bindingResult, redirectAttributes, model);
        }
    }

    private String deleteAllEmptyBaskets() {
        supplyBasketsDAO.deleteAllEmptyBaskets();

        return String.format("redirect:/%s/supplies/baskets", uriPrefix);
    }

    private String changeBasketType(BaseSupplyBasket<S> supplyBasket, Model model) {

        //TODO: step обернуть в отдельный объект сессии CHAT GPT чтобы не было конфликтов
        String stepName = getModelStepName();

        EStep supplyStep = (EStep)model.getAttribute(stepName);
        List<S> suppliesList = supplyBasket.getSuppliesList();

        if (supplyStep == EStep.PRODUCT_SELECTION_DONE) {
            supplyStep = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(suppliesList.size());
            for (S supply : supplyBasket.getSuppliesList()) {
                excludeProductIds.add(supply.getProduct().getId());
            }

            List<S> fullSuppliesList = commonProductsDAO.findAllProductsWrappedInSupply(supplyClass, excludeProductIds);

            setPrivateInfos(supplyBasket, fullSuppliesList, false);

            suppliesList.addAll(fullSuppliesList);
        }
        else {
            supplyStep = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames(supplyBasket.getFilter()));
            supplyBasket.setSuppliesList(supplyBasket.getSuppliesListForUpdate()); // delete all unsaved supplies
        }

        model.addAttribute(stepName, supplyStep);
        return String.format("%s/supplies/baskets/add-new-basket", uriPrefix);
    }

    private void setPrivateInfos(BaseSupplyBasket<S> supplyBasket, List<S> suppliesList, boolean onlyForLastSupplies) {
        Set<Integer> productIds;
        BigDecimal defaultImportCostPercentage = null;

        if (suppliesList.isEmpty()) {
            return;
        }

        if (onlyForLastSupplies) {
            productIds = new HashSet<>();
            productIds.add(suppliesList.get(suppliesList.size() - 1).getProduct().getId());
        }
        else {
            productIds = suppliesList.stream().map(s -> s.getProduct().getId()).collect(Collectors.toCollection(HashSet::new));
        }

        Map<Integer, Integer> stockBeforeSaleMap = allSuppliesDAO.findStockBeforeSale(productIds, supplyBasket);

        for (int i = onlyForLastSupplies ? suppliesList.size() - 1 : 0; i < suppliesList.size(); i++) {
            suppliesList.get(i).setStockBeforeSale(stockBeforeSaleMap.get(suppliesList.get(i).getProduct().getId()));
        }
    }

    private String resetAddingNewProduct(BaseSupplyBasket<S> supplyBasket, Model model) {
        supplyBasket.getSuppliesList().remove(supplyBasket.getSuppliesList().size() - 1);
        model.addAttribute(getModelStepName(), EStep.PRODUCT_SELECTION_DONE);

        return String.format("%s/supplies/baskets/add-new-basket", uriPrefix);
    }

    private String changeSearchType(BaseSupplyBasket<S> supplyBasket, Model model) {
        EStep supplyStep = (EStep)model.getAttribute(getModelStepName());

        if (supplyStep != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            supplyStep = EStep.PRODUCT_SELECTION_WITH_FILTER;

            if (supplyBasket.getFilter() != null) {
                supplyBasket.getFilter().clear();
            }
        }
        else {
            supplyStep = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames(supplyBasket.getFilter()));
        }
        model.addAttribute(getModelStepName(), supplyStep);
        return String.format("%s/supplies/baskets/add-new-basket", uriPrefix);
    }

    private String markAsReceivedBasket(BaseSupplyBasket<S> supplyBasket, BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes) {

        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                supplyBasket.getStatus() == BaseSupplyBasket.EStatus.DELETED ? "восстановлении" : "оприходовании");

        return changeBasketStatus(supplyBasket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.RECEIVED);
    }

    private String changeBasketStatus(BaseSupplyBasket<S> supplyBasket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, BaseSupplyBasket.EStatus newStatus) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return String.format("redirect:/%s/supplies/baskets/%d", uriPrefix, supplyBasket.getId());
        }

        patchBasketInternal(supplyBasket); // перед обновлением статуса корзины обязательно обновить ее
        supplyBasket.setStatus(newStatus);
        supplyBasketsDAO.updateBasketStatus(supplyBasket);

        return String.format("redirect:/%s/supplies/baskets", uriPrefix);
    }

    private void patchBasketInternal(BaseSupplyBasket<S> supplyBasket) {
        patchAllSupplies(supplyBasket);
        patchBasket(supplyBasket);
    }

    private void patchAllSupplies(BaseSupplyBasket<S> supplyBasket) {
        allSuppliesDAO.saveNewSuppliesList(supplyBasket.getId(), supplyBasket.getSuppliesListForSave());

        prepareDataForPatching(supplyBasket);
        allSuppliesDAO.updateQuantityAndPrices(supplyBasket.getSuppliesListForUpdate());
    }

    private void patchBasket(BaseSupplyBasket<S> supplyBasket) {
        supplyBasketsDAO.updateBasketNotes(supplyBasket);
        updateBasketTotalSum(supplyBasket);
    }

    private String deleteSelected(BaseSupplyBasket<S> supplyBasket, List<Integer> selectedSuppliesIds) {
        if (selectedSuppliesIds != null && !selectedSuppliesIds.isEmpty()) {
            allSuppliesDAO.deleteSuppliesIds(selectedSuppliesIds);
            updateBasketTotalSum(supplyBasket);
        }
        return String.format("redirect:/%s/supplies/baskets/%d", uriPrefix, supplyBasket.getId());
    }

    private String deleteBasket(BaseSupplyBasket<S> supplyBasket, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(supplyBasket, bindingResult, redirectAttributes, errorMsg, SupplyBasket.EStatus.DELETED);
    }

    private String patchBasket(BaseSupplyBasket<S> supplyBasket, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model, boolean shouldClose) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/supplies/baskets/%d", uriPrefix, supplyBasket.getId());
        }

        patchBasketInternal(supplyBasket);

        if (shouldClose) {
            return String.format("redirect:/%s/supplies/baskets", uriPrefix);
        }
        return String.format("redirect:/%s/supplies/baskets/%d", uriPrefix, supplyBasket.getId());
    }

    private String saveNewBasket(BaseSupplyBasket<S> supplyBasket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/supplies/baskets/add", uriPrefix);
        }

        EStep supplyStep = processAndGetNextStep(supplyBasket, model, (EStep)model.getAttribute(getModelStepName()));
        model.addAttribute(getModelStepName(), supplyStep);

        return String.format("%s/supplies/baskets/add-new-basket", uriPrefix);
    }

    EStep processAndGetNextStep(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {

        switch (supplyStep) {
            case SUPPLIER_STORE_NAME_SELECTION:
                return processSupplierStoreNameSelection(supplyBasket, model, supplyStep);
            case SUPPLIER_MANAGER_NAME_SELECTION:
                return processSupplierManagerNameSelection(supplyBasket, model, supplyStep);
            case SUPPLIER_PHONE_NUMBER_SELECTION:
                return processSupplierPhoneNumberSelection(supplyBasket, supplyStep);
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
                return processProductQuantityAndPriceSelection(supplyBasket, supplyStep);
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(supplyBasket, model, supplyStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", supplyStep.getValue()));
        }
    }

    protected EStep getNextStep(EStep supplyStep) {

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

    private EStep processSupplierStoreNameSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        List<String> allSupplierNamesList = suppliersDAO.findSupplierNameByStoreName(supplyBasket.getSupplier().getStoreName());

        if (allSupplierNamesList.size() == 1) {
            supplyBasket.getSupplier().setName(allSupplierNamesList.get(0));
            return processSupplierManagerNameSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allSupplierNames", allSupplierNamesList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processSupplierManagerNameSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        Supplier supplier = supplyBasket.getSupplier();
        List<String> allSupplierPhoneNumbers = suppliersDAO.findSupplierPhoneNumbersByStoreNameAndManagerName(supplier.getStoreName(), supplier.getName());

        if (allSupplierPhoneNumbers.size() == 1) {
            supplyBasket.getSupplier().setPhoneNumber(allSupplierPhoneNumbers.get(0));
            return processSupplierPhoneNumberSelection(supplyBasket, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allSupplierPhoneNumbers", allSupplierPhoneNumbers);
        }
        return getNextStep(supplyStep);
    }

    protected void baseProcessSupplierPhoneNumberSelection(BaseSupplyBasket<S> supplyBasket) {
        supplyBasket.getSupplier().setId(suppliersDAO.findSupplierId(supplyBasket.getSupplier()));
        supplyBasket.setSuppliesList(new LinkedList<>());
        supplyBasket.setStatus(SupplyBasket.EStatus.PENDING);
    }

    private EStep processProductSelectionWithFilter(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        supplyBasket.getFilter().prepareAllFields();

        List<ProductName> allProductNames = commonProductsDAO.findAllProductNames(supplyBasket.getFilter());
        model.addAttribute("allProductNames", allProductNames);

        if (allProductNames.isEmpty()) {
            supplyBasket.getFilter().clear();
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductNameSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        supply.getProduct().setProductName(commonProductNamesDAO.findProductNameById(supply.getProduct().getProductName().getId()));
        List<String> allCarModelsList = commonProductsDAO.findCarModelsByProductNameId(supply.getProduct().getProductName().getId(), supplyBasket.getFilter());

        if (allCarModelsList.size() == 1) {
            supply.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductCarModelSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allEngineModelsList = commonProductsDAO.findAvailableEngineModels(supply.getProduct(), supplyBasket.getFilter());

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

    private EStep processProductEngineModelSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allOemArticleNumbersList = commonProductsDAO.findAvailableOemArticleNumbers(supply.getProduct(), supplyBasket.getFilter());

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

    private EStep processProductOemArticleNumberSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<String> allCrossArticleNumbersList = commonProductsDAO.findAvailableCrossArticleNumbers(supply.getProduct(), supplyBasket.getFilter());

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

    private EStep processProductCrossArticleNumberSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);
        List<Brand> allBrandsList = commonProductsDAO.findAvailableBrands(supply.getProduct(), supplyBasket.getFilter());

        if (allBrandsList.size() == 1) {
            supply.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(supplyBasket, model, getNextStep(supplyStep));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(supplyStep);
    }

    private EStep processProductBrandSelection(BaseSupplyBasket<S> supplyBasket, Model model, EStep supplyStep) {
        S supply = supplyBasket.getSuppliesList().get(supplyBasket.getSuppliesList().size() - 1);

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

    protected BaseSupplyBasket findBasketById(Integer basketId) {

        try {
            BaseSupplyBasket supplyBasket = supplyBasketsDAO.findBasketById(basketId);
            supplyBasket.setSuppliesList(allSuppliesDAO.findAllSuppliesByBasketId(basketId));
            setPrivateInfos(supplyBasket, supplyBasket.getSuppliesList(), false);
            return supplyBasket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    public ModelAndView baseFindBasketById(Integer basketId, Model model) {
        ModelAndView mav = new ModelAndView(String.format("%s/supplies/baskets/add-new-basket", uriPrefix));
        BaseSupplyBasket supplyBasket = findBasketById(basketId);

        if (supplyBasket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/supplies/baskets", uriPrefix));
        }
        else {
            mav.addObject(getModelBasketName(), supplyBasket);
            mav.addObject(getModelStepName(), EStep.PRODUCT_SELECTION_DONE);
        }
        return mav;
    }
}
