package com.prestige.controllers.sklad;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.dao.sklad.SkladClientsDAO;
import com.prestige.dao.sklad.SkladSalesBasketsDAO;
import com.prestige.models.Brand;
import com.prestige.models.Sales;
import com.prestige.models.SalesBasket;
import com.prestige.models.enums.EStep;
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
@RequestMapping("/sklad/sales/baskets")
@SessionAttributes({"skladSalesStep", "skladSalesBasket"})
public class SkladSalesBasketsController {

    private SkladSalesBasketsDAO skladSalesBasketsDAO;

    private SkladClientsDAO skladClientsDAO;

    private SkladAllSuppliesDAO skladAllSuppliesDAO;

    private SkladAllSalesDAO skladAllSalesDAO;

    private CommonProductNamesDAO commonProductNamesDAO;

    private CommonBrandNamesDAO commonBrandNamesDAO;

    private CommonProductsDAO commonProductsDAO;

    @Autowired
    public SkladSalesBasketsController(SkladSalesBasketsDAO skladSalesBasketsDAO, SkladClientsDAO skladClientsDAO,
                                       SkladAllSuppliesDAO skladAllSuppliesDAO, SkladAllSalesDAO skladAllSalesDAO,
                                       CommonProductNamesDAO commonProductNamesDAO, CommonBrandNamesDAO commonBrandNamesDAO,
                                       CommonProductsDAO commonProductsDAO) {
        this.skladSalesBasketsDAO = skladSalesBasketsDAO;
        this.skladAllSuppliesDAO = skladAllSuppliesDAO;
        this.skladAllSalesDAO = skladAllSalesDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.skladClientsDAO = skladClientsDAO;
    }

    @GetMapping
    public String showAllBaskets(Model model) {
        model.addAttribute("allBaskets", skladSalesBasketsDAO.findAllBaskets());
        model.addAttribute("skladSalesBasket", new SalesBasket()); // mock

        return "sklad/sales/baskets/baskets";
    }

    @GetMapping("/add")
    public String addNewBasketGet(Model model) {
        model.addAttribute("skladSalesBasket", new SalesBasket());
        model.addAttribute("skladSalesStep", EStep.CLIENT_NAME_SELECTION);
        model.addAttribute("allClientNames", skladClientsDAO.findUniqueClientNames());

        return "sklad/sales/baskets/add-new-basket";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladSalesBasket") SalesBasket salesBasket,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedSalesIds", required = false) List<Integer> selectedSalesIds,
                                       BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "deleteAllEmptyBaskets":
                return deleteAllEmptyBaskets();
            case "changeBasketType":
                return changeBasketType(salesBasket, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(salesBasket, model);
            case "changeSearchType":
                return changeSearchType(salesBasket, model);
            case "markAsSold":
                return markAsSoldBasket(salesBasket, bindingResult, redirectAttributes);
            case "deleteSelected":
                return deleteSelected(salesBasket, selectedSalesIds);
            case "delete":
                return deleteBasket(salesBasket, bindingResult, redirectAttributes);
            case "close":
                return "redirect:/sklad/sales/baskets";
            case "patch":
                return patchBasket(salesBasket, bindingResult, redirectAttributes, model);
            default:
                return saveNewBasket(salesBasket, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep skladSalesStep) {

        switch (skladSalesStep) {
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
                throw new RuntimeException(String.format("Step is undefined: '%s'", skladSalesStep.getValue()));
        }
    }

    EStep processAndGetNextStep(SalesBasket salesBasket, Model model, EStep skladSalesStep) {

        switch (skladSalesStep) {
            case CLIENT_NAME_SELECTION:
                return processClientNameSelection(salesBasket, model, skladSalesStep);
            case CLIENT_PHONE_NUMBER_SELECTION:
                return processPhoneNumberSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_SELECTION_WITH_FILTER:
                return processProductSelectionWithFilter(salesBasket, model, skladSalesStep);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_CAR_MODEL_SELECTION:
                return processProductCarModelSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_ENGINE_MODEL_SELECTION:
                return processProductEngineModelSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_OEM_ARTICLE_NUMBER_SELECTION:
                return processProductOemArticleNumberSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_CROSS_ARTICLE_NUMBER_SELECTION:
                return processProductCrossArticleNumberSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_BRAND_SELECTION:
                return processProductBrandSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_QUANTITY_AND_PRICE_SELECTION:
                return processProductQuantityAndPriceSelection(salesBasket, model, skladSalesStep);
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(salesBasket, model, skladSalesStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", skladSalesStep.getValue()));
        }
    }

    private EStep processClientNameSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        List<String> allPhoneNumbersList = skladClientsDAO.findPhoneNumbersByClientName(salesBasket.getClient().getName());

        if (allPhoneNumbersList.size() == 1) {
            salesBasket.getClient().setPhoneNumber(allPhoneNumbersList.get(0));
            return processPhoneNumberSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allPhoneNumbers", allPhoneNumbersList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processPhoneNumberSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        salesBasket.getClient().setId(skladClientsDAO.findClientId(salesBasket.getClient()));
        salesBasket.setSalesList(new LinkedList<>());
        salesBasket.setStatus(SalesBasket.EStatus.PENDING);

        skladSalesBasketsDAO.saveNewBasket(salesBasket);

        return getNextStep(skladSalesStep);
    }

    private EStep processProductSelectionWithFilter(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        salesBasket.getFilter().prepareAllFields();
        model.addAttribute("allProductNames", skladAllSuppliesDAO.findAllProductNamesWithFilter(salesBasket.getFilter()));
        return getNextStep(skladSalesStep);
    }

    private EStep processProductNameSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        sales.getProduct().setProductName(commonProductNamesDAO.findProductNameById(sales.getProduct().getProductName().getId()));
        List<String> allCarModelsList = skladAllSuppliesDAO.findAvailableCarModels(sales.getProduct());

        if (allCarModelsList.size() == 1) {
            sales.getProduct().setCarModel(allCarModelsList.get(0));
            return processProductCarModelSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allCarModels", allCarModelsList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processProductCarModelSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allEngineModelsList = skladAllSuppliesDAO.findAvailableEngineModels(sales.getProduct());

        if (allEngineModelsList.size() == 1) {
            sales.getProduct().setEngineModel(allEngineModelsList.get(0));
            return processProductEngineModelSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else if (allEngineModelsList.isEmpty()) {
            sales.getProduct().setEngineModel("");
            return processProductEngineModelSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allEngineModels", allEngineModelsList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processProductEngineModelSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allOemArticleNumbersList = skladAllSuppliesDAO.findAvailableOemArticleNumbers(sales.getProduct());

        if (allOemArticleNumbersList.size() == 1) {
            sales.getProduct().setOemArticleNumber(allOemArticleNumbersList.get(0));
            return processProductOemArticleNumberSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else if (allOemArticleNumbersList.isEmpty()) {
            sales.getProduct().setOemArticleNumber("");
            return processProductOemArticleNumberSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allOemArticleNumbers", allOemArticleNumbersList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processProductOemArticleNumberSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<String> allCrossArticleNumbersList = skladAllSuppliesDAO.findAvailableCrossArticleNumbers(sales.getProduct());

        if (allCrossArticleNumbersList.size() == 1) {
            sales.getProduct().setCrossArticleNumber(allCrossArticleNumbersList.get(0));
            return processProductCrossArticleNumberSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else if (allCrossArticleNumbersList.isEmpty()) {
            sales.getProduct().setCrossArticleNumber("");
            return processProductCrossArticleNumberSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allCrossArticleNumbers", allCrossArticleNumbersList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processProductCrossArticleNumberSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales sales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);
        List<Brand> allBrandsList = skladAllSuppliesDAO.findAvailableBrands(sales.getProduct());

        if (allBrandsList.size() == 1) {
            sales.getProduct().setBrand(allBrandsList.get(0));
            return processProductBrandSelection(salesBasket, model, getNextStep(skladSalesStep));
        }
        else {
            model.addAttribute("allBrands", allBrandsList);
        }
        return getNextStep(skladSalesStep);
    }

    private EStep processProductBrandSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
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
        return getNextStep(skladSalesStep);
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

        Map<Integer, Integer> stockBeforeSaleMap = skladAllSuppliesDAO.findStockBeforeSale(productIds, salesBasket);
        Map<Integer, BigDecimal> wholeSalePriceMap = skladAllSuppliesDAO.findWholesalePrice(productIds);

        int salesListSize = salesList.size();

        for (int i = onlyForLastSales ? salesListSize - 1 : 0; i < salesListSize; i++) {
            Sales sales = salesList.get(i);

            sales.setStockBeforeSale(stockBeforeSaleMap.get(sales.getProduct().getId()));
            sales.setWholesalePrice(wholeSalePriceMap.get(sales.getProduct().getId()));
        }
    }

    private void updateBasketTotalSum(SalesBasket salesBasket) {
        List<Sales> salesList = salesBasket.getSalesListForUpdate();
        BigDecimal totalSum = new BigDecimal(0);

        for (Sales sales : salesList) {
            totalSum = totalSum.add(sales.getSoldPrice().multiply(BigDecimal.valueOf(sales.getQuantity())));
        }
        salesBasket.setTotalSum(totalSum.setScale(2, RoundingMode.HALF_UP));
        skladSalesBasketsDAO.updateBasketTotalSum(salesBasket);
    }

    private EStep processProductQuantityAndPriceSelection(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        Sales newSales = salesBasket.getSalesList().get(salesBasket.getSalesList().size() - 1);

        skladAllSalesDAO.saveNewSale(salesBasket, newSales);
        updateBasketTotalSum(salesBasket);
        return getNextStep(skladSalesStep);
    }

    private EStep processProductSelectionDone(SalesBasket salesBasket, Model model, EStep skladSalesStep) {
        salesBasket.getSalesList().add(new Sales());
        model.addAttribute("allProductNames", skladAllSuppliesDAO.findAllProductNames());

        return getNextStep(skladSalesStep);
    }

    private String changeBasketStatus(SalesBasket salesBasket, BindingResult bindingResult,
                                      RedirectAttributes redirectAttributes, String errorMsg, SalesBasket.EStatus newStatus) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error", errorMsg);
            return String.format("redirect:/sklad/sales/baskets/%d", salesBasket.getId());
        }

        patchBasketInternal(salesBasket); // перед обновлением статуса корзины обязательно обновить ее
        salesBasket.setStatus(newStatus);
        skladSalesBasketsDAO.updateBasketStatus(salesBasket);

        return "redirect:/sklad/sales/baskets";
    }

    private String deleteAllEmptyBaskets() {
        skladSalesBasketsDAO.deleteAllEmptyBaskets();

        return "redirect:/sklad/sales/baskets";
    }

    private String deleteBasket(SalesBasket salesBasket, BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        String errorMsg = "Возникла ошибка при удалении корзины. Пожалуйста, попробуйте заново.";

        return changeBasketStatus(salesBasket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.DELETED);
    }

    private String changeBasketType(SalesBasket salesBasket, Model model) {
        EStep skladSalesStep = (EStep)model.getAttribute("skladSalesStep");
        List<Sales> salesList = salesBasket.getSalesList();

        if (skladSalesStep == EStep.PRODUCT_SELECTION_DONE) {
            skladSalesStep = EStep.PRODUCT_SELECTION_WITH_FULL_LIST;

            List<Integer> excludeProductIds = new ArrayList<>(salesList.size());
            for (Sales sales : salesBasket.getSalesList()) {
                excludeProductIds.add(sales.getProduct().getId());
            }
            List<Sales> fullSalesList = skladAllSuppliesDAO.findAllPrestigeProductsWrappedInSales(excludeProductIds);
            setPrivateInfos(salesBasket, fullSalesList, false);

            salesList.addAll(fullSalesList);
        }
        else {
            skladSalesStep = EStep.PRODUCT_SELECTION_DONE;
            model.addAttribute("allProductNames", skladAllSuppliesDAO.findAllProductNames());
            salesBasket.setSalesList(salesBasket.getSalesListForUpdate()); // delete all unsaved sales
        }

        model.addAttribute("skladSalesStep", skladSalesStep);
        return "sklad/sales/baskets/add-new-basket";
    }

    private String resetAddingNewProduct(SalesBasket salesBasket, Model model) {
        salesBasket.getSalesList().remove(salesBasket.getSalesList().size() - 1);
        model.addAttribute("skladSalesStep", EStep.PRODUCT_SELECTION_DONE);

        return "sklad/sales/baskets/add-new-basket";
    }

    private String changeSearchType(SalesBasket salesBasket, Model model) {
        EStep skladSalesStep = (EStep)model.getAttribute("skladSalesStep");

        if (skladSalesStep != EStep.PRODUCT_SELECTION_WITH_FILTER) {
            skladSalesStep = EStep.PRODUCT_SELECTION_WITH_FILTER;

            if (salesBasket.getFilter() != null) {
                salesBasket.getFilter().clear();
            }
        }
        else {
            skladSalesStep = EStep.PRODUCT_NAME_SELECTION;
            model.addAttribute("allProductNames", skladAllSuppliesDAO.findAllProductNames());
        }
        model.addAttribute("skladSalesStep", skladSalesStep);
        return "sklad/sales/baskets/add-new-basket";
    }

    private String markAsSoldBasket(SalesBasket salesBasket, BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {
        String errorMsg = String.format("Возникла ошибка при %s корзины. Пожалуйста, попробуйте заново.",
                salesBasket.getStatus() == SalesBasket.EStatus.DELETED ? "восстановлении" : "продаже");

        return changeBasketStatus(salesBasket, bindingResult, redirectAttributes, errorMsg, SalesBasket.EStatus.SOLD);
    }

    private String deleteSelected(SalesBasket salesBasket, List<Integer> selectedSalesIds) {

        if (selectedSalesIds != null && !selectedSalesIds.isEmpty()) {
            for (Integer salesId : selectedSalesIds) {
                salesBasket.getSalesList().removeIf(sales -> sales.getId().equals(salesId));
            }

            skladAllSalesDAO.deleteSalesIds(selectedSalesIds);
            updateBasketTotalSum(salesBasket);
        }
        return "redirect:/sklad/sales/baskets/" + salesBasket.getId();
    }

    private void patchBasketInternal(SalesBasket salesBasket) {
        skladAllSalesDAO.saveNewSalesList(salesBasket, salesBasket.getSalesListForSave());
        skladAllSalesDAO.updateQuantityAndPrices(salesBasket.getSalesListForUpdate());
        skladSalesBasketsDAO.updateBasketNotes(salesBasket);
        updateBasketTotalSum(salesBasket);
    }

    private String patchBasket(SalesBasket salesBasket, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        // обновляем кол-во и цену товаров и возвращаемся на страницу "/sklad/sales/baskets"

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при сохранении корзины. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/sklad/sales/baskets/%d", salesBasket.getId());
        }

        patchBasketInternal(salesBasket);

        return "redirect:/sklad/sales/baskets";
    }

    private String saveNewBasket(SalesBasket salesBasket, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при добавлении товаров в корзину. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/sklad/sales/baskets/add");
        }

        EStep skladSalesStep = processAndGetNextStep(salesBasket, model, (EStep)model.getAttribute("skladSalesStep"));
        model.addAttribute("skladSalesStep", skladSalesStep);

        return "sklad/sales/baskets/add-new-basket";
    }

    private SalesBasket findBasketById(Integer basketId) {

        try {
            SalesBasket salesBasket = skladSalesBasketsDAO.findBasketById(basketId);
            salesBasket.setSalesList(skladAllSalesDAO.findAllSalesByBasketId(basketId));
            setPrivateInfos(salesBasket, salesBasket.getSalesList(), false);
            return salesBasket;
        }
        catch (DataAccessException e) {
            return null;
        }
    }

    @GetMapping("/{basketId}")
    public ModelAndView findBasketById(@PathVariable("basketId") Integer basketId, Model model) {
        ModelAndView mav = new ModelAndView("sklad/sales/baskets/add-new-basket");
        SalesBasket salesBasket = findBasketById(basketId);

        if (salesBasket == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/sklad/sales/baskets");
        }
        else {
            mav.addObject("skladSalesBasket", salesBasket);
            mav.addObject("skladSalesStep", EStep.PRODUCT_SELECTION_DONE);
        }
        return mav;
    }
}
