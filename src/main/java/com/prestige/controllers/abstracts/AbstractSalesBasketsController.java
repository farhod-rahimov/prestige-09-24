package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.*;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.models.*;
import com.prestige.models.enums.EStep;
import com.prestige.utils.AbstractTabStateManager;
import com.prestige.utils.SalesTabState;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public abstract class AbstractSalesBasketsController extends AbstractTabStateManager {

    protected AbstractSalesBasketsDAO salesBasketsDAO;

    protected AbstractAllSalesDAO allSalesDAO;

    protected AbstractClientsDAO clientsDAO;

    protected AbstractAllSuppliesDAO allSuppliesDAO;

    protected AbstractSettingsDAO settingsDAO;

    protected AbstractProductsMarginDAO marginDAO;

    protected CommonProductsDAO commonProductsDAO;

    protected CommonProductNamesDAO commonProductNamesDAO;

    protected CommonBrandNamesDAO commonBrandNamesDAO;

    protected final String uriPrefix;

    private String getModelStepName() {
        return "salesStep";
    }

    private String getModelBasketName() {
        return "salesBasket";
    }

    @Override
    protected String getTabStateMapName() {
        return "salesTabStateMap";
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", salesBasketsDAO.findAllBaskets());
        model.addAttribute(getModelBasketName(), new SalesBasket()); // mock

        return String.format("%s/sales/baskets/baskets", uriPrefix);
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model, HttpSession session) {
        SalesTabState state = new SalesTabState(UUID.randomUUID().toString(), EStep.CLIENT_NAME_SELECTION, new SalesBasket());
        saveTabState(session, getTabStateMapName(), state);

        model.addAttribute("tabId", state.getTabId());
        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());
        model.addAttribute("allClientNames", clientsDAO.findUniqueClientNames());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private void patchClientNameSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        salesBasket.getClient().setName(salesBasketForm.getClient().getName());
    }

    private void patchClientPhoneNumberSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        salesBasket.getClient().setPhoneNumber(salesBasketForm.getClient().getPhoneNumber());
    }

    private void patchProductNameSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        ProductName productNameForm = salesBasketForm.getSalesList().get(lastIndex).getProduct().getProductName();
        ProductName productName = salesBasket.getSalesList().get(lastIndex).getProduct().getProductName();

        productName.setId(productNameForm.getId());
    }

    private void patchProductCarModelSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).getProduct().setCarModel(
                salesBasketForm.getSalesList().get(lastIndex).getProduct().getCarModel());
    }

    private void patchProductEngineModelSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).getProduct().setEngineModel(
                salesBasketForm.getSalesList().get(lastIndex).getProduct().getEngineModel());
    }

    private void patchProductOemArticleNumberSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).getProduct().setOemArticleNumber(
                salesBasketForm.getSalesList().get(lastIndex).getProduct().getOemArticleNumber());
    }

    private void patchProductCrossArticleNumberSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).getProduct().setCrossArticleNumber(
                salesBasketForm.getSalesList().get(lastIndex).getProduct().getCrossArticleNumber());
    }

    private void patchProductBrandSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).getProduct().getBrand().setId(
                salesBasketForm.getSalesList().get(lastIndex).getProduct().getBrand().getId());
    }

    private void patchProductQuantityAndPriceSelection(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        int lastIndex = salesBasket.getSalesList().size() - 1;
        salesBasket.getSalesList().get(lastIndex).setQuantity(salesBasketForm.getSalesList().get(lastIndex).getQuantity());
        salesBasket.getSalesList().get(lastIndex).setSoldPrice(salesBasketForm.getSalesList().get(lastIndex).getSoldPrice());
    }

    private void patchProductSelectionWithFilter(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        salesBasket.setFilter(salesBasketForm.getFilter());
    }

    private void patchDefault(SalesBasket salesBasket, SalesBasket salesBasketForm) {

        for (int i = 0; i < salesBasket.getSalesList().size(); i++) {
            salesBasket.getSalesList().get(i).setQuantity(salesBasketForm.getSalesList().get(i).getQuantity());
            salesBasket.getSalesList().get(i).setSoldPrice(salesBasketForm.getSalesList().get(i).getSoldPrice());
        }
    }

    private void patchNotes(SalesBasket salesBasket, SalesBasket salesBasketForm) {
        salesBasket.setNotes(salesBasketForm.getNotes());
    }

    private void patchSalesBasketFromForm(SalesBasket salesBasket, SalesBasket salesBasketForm, EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
                patchClientNameSelection(salesBasket, salesBasketForm); break;
            case CLIENT_PHONE_NUMBER_SELECTION:
                patchClientPhoneNumberSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_NAME_SELECTION:
                patchProductNameSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_CAR_MODEL_SELECTION:
                patchProductCarModelSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_ENGINE_MODEL_SELECTION:
                patchProductEngineModelSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                patchProductOemArticleNumberSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                patchProductCrossArticleNumberSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_BRAND_SELECTION:
                patchProductBrandSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                patchProductQuantityAndPriceSelection(salesBasket, salesBasketForm); break;
            case PRODUCT_SELECTION_WITH_FILTER:
                patchProductSelectionWithFilter(salesBasket, salesBasketForm); break;
            default:
                patchDefault(salesBasket, salesBasketForm); break;
        }
    }

    protected String postMethodDispatcherInternal(SalesBasket salesBasketForm, String method, String tabId,
                                                  List<Integer> selectedSalesIds, BindingResult bindingResult,
                                                  RedirectAttributes redirectAttributes, Model model, HttpSession session) {

        SalesTabState state = (SalesTabState)getTabState(session, getTabStateMapName(), tabId);
        SalesBasket salesBasket = null;

        if (state != null) {

            if (!method.equals("close")) {
                salesBasket = state.getBasket();

                if (!method.equals("changeSearchType") && !method.equals("changeBasketType")) {
                    patchSalesBasketFromForm(salesBasket, salesBasketForm, state.getStep());
                }
                patchNotes(salesBasket, salesBasketForm);
                model.addAttribute("tabId", tabId);
            }
        }
        else if (!method.equals("deleteAllEmptyBaskets")) { // state timed out
            return String.format("redirect:/%s/sales/baskets/", uriPrefix);
        }

        switch (method) {
            case "selectAllCheckBoxes":
                return markAllCheckBoxes(model, state, true);
            case "removeAllCheckBoxes":
                return markAllCheckBoxes(model, state, false);
            case "removeAllZeroQuantity":
                return removeAllZeroQuantity(session, state);
            case "deleteAllEmptyBaskets":
                return deleteAllEmptyBaskets();
            case "changeBasketType":
                return changeBasketType(salesBasket, state, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(salesBasket, state, model);
            case "changeSearchType":
                return changeSearchType(salesBasket, state, model);
            case "markAsSold":
                return markAsSoldBasket(salesBasket, bindingResult, redirectAttributes, state, session);
            case "deleteSelected":
                return deleteSelected(salesBasket, selectedSalesIds, state, session);
            case "delete":
                return deleteBasket(salesBasket, bindingResult, redirectAttributes, state, session);
            case "close": {
                removeTabState(session, getTabStateMapName(), tabId);
                return String.format("redirect:/%s/sales/baskets", uriPrefix);
            }
            case "patch":
                return patchBasket(salesBasket, state, bindingResult, redirectAttributes, session, false);
            case "patchAndClose":
                return patchBasket(salesBasket, state, bindingResult, redirectAttributes, session, true);
            default:
                return saveNewBasket(salesBasket, state, bindingResult, redirectAttributes, session, model);
        }
    }

    private String markAllCheckBoxes(Model model, SalesTabState state, boolean markAllAsSelected) {

        if (markAllAsSelected) {
            model.addAttribute("selectAllCheckBoxes", true);
        }

        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private String removeAllZeroQuantity(HttpSession session, SalesTabState state) {
        allSalesDAO.deleteAllZeroQuantityItemsFromBasket(state.getBasket().getId());

        removeTabState(session, getTabStateMapName(), state.getTabId());
        return String.format("redirect:/%s/sales/baskets/%d", uriPrefix, state.getBasket().getId());
    }

    private EStep getNextStep(EStep salesStep) {

        switch (salesStep) {
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
                throw new RuntimeException(String.format("Step is undefined: '%s'", salesStep.getValue()));
        }
    }

    EStep processAndGetNextStep(SalesBasket salesBasket, Model model, EStep salesStep) {

        switch (salesStep) {
            case CLIENT_NAME_SELECTION:
                return processClientNameSelection(salesBasket, model, salesStep);
            case CLIENT_PHONE_NUMBER_SELECTION:
                return processPhoneNumberSelection(salesBasket, model, salesStep);
            case PRODUCT_SELECTION_WITH_FILTER:
                return processProductSelectionWithFilter(salesBasket, model, salesStep);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(salesBasket, model, salesStep);
            case PRODUCT_CAR_MODEL_SELECTION:
                return processProductCarModelSelection(salesBasket, model, salesStep);
            case PRODUCT_ENGINE_MODEL_SELECTION:
                return processProductEngineModelSelection(salesBasket, model, salesStep);
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                return processProductOemArticleNumberSelection(salesBasket, model, salesStep);
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                return processProductCrossArticleNumberSelection(salesBasket, model, salesStep);
            case PRODUCT_BRAND_SELECTION:
                return processProductBrandSelection(salesBasket, model, salesStep);
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                return processProductQuantityAndPriceSelection(salesBasket, model, salesStep);
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(salesBasket, model, salesStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", salesStep.getValue()));
        }
    }

    private EStep processClientNameSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        List<String> allPhoneNumbersList = clientsDAO.findPhoneNumbersByClientName(salesBasket.getClient().getName());

        if (allPhoneNumbersList.size() == 1) {
            salesBasket.getClient().setPhoneNumber(allPhoneNumbersList.get(0));
            return processPhoneNumberSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allPhoneNumbers", allPhoneNumbersList);
        }
        return getNextStep(salesStep);
    }

    private EStep processPhoneNumberSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        salesBasket.getClient().setId(clientsDAO.findClientId(salesBasket.getClient()));
        salesBasket.setSalesList(new LinkedList<>());
        salesBasket.setStatus(SalesBasket.EStatus.PENDING);

        salesBasketsDAO.saveNewBasket(salesBasket);

        return getNextStep(salesStep);
    }

    private EStep processProductSelectionWithFilter(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);

        salesBasket.getFilter().prepareAllFields();
        List<ProductName> allProductNames = allSuppliesDAO.findAllProductNamesWithFilter(salesBasket.getFilter());

        if (allProductNames.size() == 1) {
            sales.getProduct().setProductName(allProductNames.get(0));
            return processProductNameSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allProductNames", allProductNames);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductNameSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);

        if (sales.getProduct().getProductName().getProductName() == null) {
            sales.getProduct().setProductName(commonProductNamesDAO.findProductNameById(sales.getProduct().getProductName().getId()));
        }
        List<String> allCarModelsList = allSuppliesDAO.findAvailableCarModels(sales.getProduct(), salesBasket.getFilter());

        if (allCarModelsList.size() == 1) {
            sales.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductCarModelSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allEngineModelsList = allSuppliesDAO.findAvailableEngineModels(sales.getProduct(), salesBasket.getFilter());

        if (allEngineModelsList.size() == 1) {
            sales.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(salesBasket, model, getNextStep(salesStep));
        }
        else if (allEngineModelsList.isEmpty()) {
            sales.getProduct().setEngineModel("");
            return processProductEngineModelSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductEngineModelSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allOemArticleNumbersList = allSuppliesDAO.findAvailableOemArticleNumbers(sales.getProduct(), salesBasket.getFilter());

        if (allOemArticleNumbersList.size() == 1) {
            sales.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(salesBasket, model, getNextStep(salesStep));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            sales.getProduct().setOemArticleNumber("");
            return processProductOemArticleNumberSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductOemArticleNumberSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allCrossArticleNumbersList = allSuppliesDAO.findAvailableCrossArticleNumbers(sales.getProduct(), salesBasket.getFilter());

        if (allCrossArticleNumbersList.size() == 1) {
            sales.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(salesBasket, model, getNextStep(salesStep));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            sales.getProduct().setCrossArticleNumber("");
            return processProductCrossArticleNumberSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductCrossArticleNumberSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<Brand> allBrandsList = allSuppliesDAO.findAvailableBrands(sales.getProduct(), salesBasket.getFilter());

        if (allBrandsList.size() == 1) {
            sales.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(salesBasket, model, getNextStep(salesStep));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(salesStep);
    }

    private EStep processProductBrandSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);

        if (sales.getProduct().getBrand().getBrandName() == null) {
            sales.getProduct().setBrand(commonBrandNamesDAO.findBrandById(sales.getProduct().getBrand().getId()));
        }
        sales.getProduct().setId(commonProductsDAO.findProductId(sales.getProduct()));

        setPrivateInfos(salesBasket, salesBasket.getSalesList(), true);

        for (int i = 0; i < salesBasket.getSalesList().size() - 1; i++) {

            if (sales.equalsOnlyProductWithoutNotesAndEquivalentProductInfo(salesBasket.getSalesList().get(i))) {
                salesBasket.getSalesList().remove(salesBasket.getSalesList().size() - 1);

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
        return getNextStep(salesStep);
    }

    private void setPrivateInfos(SalesBasket salesBasket, List<Sales> salesList, boolean onlyForLastSales) {
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

        Map<Integer, Integer> stockBeforeSaleMap = allSuppliesDAO.findStockBeforeSale(productIds, salesBasket);
        Map<Integer, BigDecimal> wholeSalePriceMap = allSuppliesDAO.findWholesalePrice(productIds);
        Map<Integer, BigDecimal> productMarginMap = marginDAO.findAllMarginsByProductIds(productIds);

        Settings exchangeRateSettings = null;
        Settings magicValueSettings = settingsDAO.findSettingsByName("magicValue");
        Map<Integer, BigDecimal> recommendedPriceMap = new HashMap<>();

        if (uriPrefix.contains("prestige")) {
            exchangeRateSettings = settingsDAO.findSettingsByName("exchangeRate");
            recommendedPriceMap = allSalesDAO.findRecommendedPrice(productIds, salesBasket);
        }

        int salesListSize = salesList.size();

        for (int i = onlyForLastSales ? salesListSize - 1 : 0; i < salesListSize; i++) {
            Sales sales = salesList.get(i);

            sales.setStockBeforeSale(stockBeforeSaleMap.get(sales.getProduct().getId()));
            sales.setWholesalePrice(wholeSalePriceMap.get(sales.getProduct().getId()));

            if (productMarginMap.get(sales.getProduct().getId()) == null) {
                sales.setMagicValue(new BigDecimal(magicValueSettings.getValue()));
            }
            else {
                sales.setMagicValue(productMarginMap.get(sales.getProduct().getId()));
            }

            sales.setCalculatedPrice(getCalculatedPrice(sales.getWholesalePrice(), sales.getMagicValue(), exchangeRateSettings));

            if (recommendedPriceMap.get(sales.getProduct().getId()) == null) {
                sales.setRecommendedPrice(sales.getCalculatedPrice());
            }
            else {
                sales.setRecommendedPrice(recommendedPriceMap.get(sales.getProduct().getId()));
            }

            if (sales.getSoldPrice() == null) {
                int scale = uriPrefix.contains("sklad") ? 2 : 0;
                sales.setSoldPrice(sales.getRecommendedPrice().setScale(scale, RoundingMode.HALF_UP));
            }
        }
    }

    private BigDecimal getCalculatedPrice(BigDecimal wholesalePrice, BigDecimal margin, Settings exchangeRateSettings) {

        BigDecimal result = wholesalePrice.multiply(new BigDecimal(100).add(margin).divide(new BigDecimal(100)))
                .setScale(2, RoundingMode.HALF_UP);

        if (exchangeRateSettings == null) {
            return result;
        }
        return result.multiply(new BigDecimal(exchangeRateSettings.getValue())).setScale(2, RoundingMode.HALF_UP);
    }

    private void updateBasketTotalSum(SalesBasket salesBasket) {
        List<Sales> salesList = salesBasket.getSalesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Sales sales : salesList) {
            totalSum = totalSum.add(sales.getSoldPrice().multiply(BigDecimal.valueOf(sales.getQuantity())));
        }
        salesBasket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        salesBasketsDAO.updateBasketTotalSum(salesBasket);
    }

    private EStep processProductQuantityAndPriceSelection(SalesBasket salesBasket, Model model, EStep salesStep) {
        Sales newSales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);

        allSalesDAO.saveNewSale(salesBasket, newSales);
        updateBasketTotalSum(salesBasket);
        return getNextStep(salesStep);
    }

    private EStep processProductSelectionDone(SalesBasket salesBasket, Model model, EStep salesStep) {
        salesBasket.setFilter(null);

        salesBasket.getSalesList().add(new Sales());
        model.addAttribute("allProductNames", allSuppliesDAO.findAllProductNames());

        return getNextStep(salesStep);
    }

    private String changeBasketStatus(SalesBasket salesBasket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, SalesBasket.EStatus newStatus,
                                      SalesTabState state, HttpSession session) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            removeTabState(session, getTabStateMapName(), state.getTabId());
            return String.format("redirect:/%s/sales/baskets/%d", uriPrefix, salesBasket.getId());
        }

        patchBasketInternal(salesBasket); // перед обновлением статуса корзины обязательно обновить ее
        salesBasket.setStatus(newStatus);
        salesBasketsDAO.updateBasketStatus(salesBasket);

        removeTabState(session, getTabStateMapName(), state.getTabId());
        return String.format("redirect:/%s/sales/baskets", uriPrefix);
    }

    private String deleteAllEmptyBaskets() {
        salesBasketsDAO.deleteAllEmptyBaskets();

        return String.format("redirect:/%s/sales/baskets", uriPrefix);
    }

    private String deleteBasket(SalesBasket salesBasket, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes, SalesTabState state, HttpSession session) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(salesBasket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.DELETED, state, session);
    }

    private String changeBasketType(SalesBasket salesBasket, SalesTabState state, Model model) {
        EStep salesStep = state.getStep();
        List<Sales> salesList = salesBasket.getSalesList();

        if (salesStep == EStep.PRODUCT_SELECTION_DONE) {
            salesStep = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(salesList.size());
            for (Sales sales : salesBasket.getSalesList()) {
                excludeProductIds.add(sales.getProduct().getId());
            }
            List<Sales> fullSalesList = allSuppliesDAO.findAllProductsWrappedInSales(excludeProductIds);
            setPrivateInfos(salesBasket, fullSalesList, false);

            salesList.addAll(fullSalesList);
        }
        else {
            salesStep = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", allSuppliesDAO.findAllProductNames());
            salesBasket.setSalesList(salesBasket.getSalesListForUpdate()); // delete all unsaved sales
        }

        state.setStep(salesStep);

        model.addAttribute(getModelStepName(), state.getStep());
        model.addAttribute(getModelBasketName(), state.getBasket());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private String resetAddingNewProduct(SalesBasket salesBasket, SalesTabState state, Model model) {
        salesBasket.getSalesList().remove(salesBasket.getSalesList().size() - 1);
        state.setStep(EStep.PRODUCT_SELECTION_DONE);

        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private String changeSearchType(SalesBasket salesBasket, SalesTabState state, Model model) {
        EStep salesStep = state.getStep();

        if (salesStep != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            salesStep = EStep.PRODUCT_SELECTION_WITH_FILTER;
        }
        else {
            salesStep = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", allSuppliesDAO.findAllProductNames());
        }
        state.setStep(salesStep);

        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private String markAsSoldBasket(SalesBasket salesBasket, BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes, SalesTabState state, HttpSession session) {
        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                salesBasket.getStatus() == SalesBasket.EStatus.DELETED ? "восстановлении" : "продаже");

        return changeBasketStatus(salesBasket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.SOLD, state, session);
    }

    private String deleteSelected(SalesBasket salesBasket, List<Integer> selectedSalesIds,
                                  SalesTabState state, HttpSession session) {

        if (selectedSalesIds != null && !selectedSalesIds.isEmpty()) {
            allSalesDAO.deleteSalesIds(selectedSalesIds);
            updateBasketTotalSum(salesBasket);
        }
        salesBasketsDAO.updateBasketNotes(salesBasket);
        removeTabState(session, getTabStateMapName(), state.getTabId());
        return String.format("redirect:/%s/sales/baskets/%d", uriPrefix, salesBasket.getId());
    }

    private void patchBasketInternal(SalesBasket salesBasket) {
        allSalesDAO.saveNewSalesList(salesBasket, salesBasket.getSalesListForSave());
        allSalesDAO.updateQuantityAndPrices(salesBasket.getSalesListForUpdate());
        salesBasketsDAO.updateBasketNotes(salesBasket);
        updateBasketTotalSum(salesBasket);
    }

    private String patchBasket(SalesBasket salesBasket, SalesTabState state, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, HttpSession session, boolean shouldClose) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            removeTabState(session, getTabStateMapName(), state.getTabId());
            return String.format("redirect:/%s/sales/baskets/%d", uriPrefix, salesBasket.getId());
        }

        patchBasketInternal(salesBasket);

        removeTabState(session, getTabStateMapName(), state.getTabId());

        if (shouldClose) {
            return String.format("redirect:/%s/sales/baskets", uriPrefix);
        }
        return String.format("redirect:/%s/sales/baskets/%d", uriPrefix, salesBasket.getId());
    }

    private String saveNewBasket(SalesBasket salesBasket, SalesTabState state, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, HttpSession session, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            removeTabState(session, getTabStateMapName(), state.getTabId());
            return String.format("redirect:/%s/sales/baskets/add", uriPrefix);
        }

        EStep salesStep = processAndGetNextStep(salesBasket, model, state.getStep());
        state.setStep(salesStep);

        model.addAttribute(getModelBasketName(), state.getBasket());
        model.addAttribute(getModelStepName(), state.getStep());

        return String.format("%s/sales/baskets/add-new-basket", uriPrefix);
    }

    private SalesBasket findBasketById(Integer basketId) {

        try {
            SalesBasket salesBasket = salesBasketsDAO.findBasketById(basketId);
            salesBasket.setSalesList(allSalesDAO.findAllSalesByBasketId(basketId));
            setPrivateInfos(salesBasket, salesBasket.getSalesList(), false);
            return salesBasket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, HttpSession session, Model model) {
        ModelAndView mav = new ModelAndView(String.format("%s/sales/baskets/add-new-basket", uriPrefix));
        SalesBasket salesBasket = findBasketById(basketId);

        if (salesBasket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/sales/baskets", uriPrefix));
        }
        else {
            SalesTabState state = new SalesTabState(UUID.randomUUID().toString(), EStep.PRODUCT_SELECTION_DONE, salesBasket);
            saveTabState(session, getTabStateMapName(), state);

            mav.addObject("tabId", state.getTabId());
            mav.addObject(getModelBasketName(), state.getBasket());
            mav.addObject(getModelStepName(), state.getStep());
        }
        return mav;
    }
}
