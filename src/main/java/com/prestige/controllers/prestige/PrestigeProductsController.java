package com.prestige.controllers.prestige;

import com.prestige.dao.Utils;
import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeProductsDAO;
import com.prestige.dao.prestige.PrestigeSuppliersDAO;
import com.prestige.models.ProductName;
import com.prestige.models.RetailProduct;
import com.prestige.models.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/prestige/products")
@SessionAttributes({"step", "retailProduct"})
public class PrestigeProductsController {

    private final PrestigeProductsDAO prestigeProductsDAO;

    private final CommonProductsDAO commonProductsDAO;

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    private final PrestigeSuppliersDAO prestigeSuppliersDAO;

    private final Utils utils;

    @AllArgsConstructor
    @Getter
    private enum EStep {
        PRODUCT_NAME_SELECTION("PRODUCT_NAME_SELECTION"),
        CAR_MODEL_SELECTION("CAR_MODEL_SELECTION"),
        ENGINE_MODEL_SELECTION("ENGINE_MODEL_SELECTION"),
        OEM_ARTICLE_NUMBER_SELECTION("OEM_ARTICLE_NUMBER_SELECTION"),
        CROSS_ARTICLE_NUMBER_SELECTION("CROSS_ARTICLE_NUMBER_SELECTION"),
        BRAND_SELECTION("BRAND_SELECTION"),
        SUPPLIER_SELECTION("SUPPLIER_SELECTION"),
        QUANTITY_AND_PRICE_INPUT("QUANTITY_AND_PRICE_INPUT"),
        DATE_TIME_SELECTION("DATE_TIME_SELECTION"),
        FINISH("FINISH");

        private final String value;
    }

    @Autowired
    public PrestigeProductsController(PrestigeProductsDAO prestigeProductsDAO,
                                      CommonProductsDAO commonProductsDAO,
                                      CommonProductNamesDAO commonProductNamesDAO,
                                      CommonBrandNamesDAO commonBrandNamesDAO,
                                      PrestigeSuppliersDAO prestigeSuppliersDAO,
                                      Utils utils) {

        this.prestigeProductsDAO = prestigeProductsDAO;
        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
        this.prestigeSuppliersDAO = prestigeSuppliersDAO;
        this.utils = utils;
    }

    @GetMapping
    public String showAllProductsView(Model model) {
        model.addAttribute("allProductsView", prestigeProductsDAO.findAllProductsView());
        return "prestige/products/products";
    }

    @GetMapping("/add")
    public String addNewProductGet(HttpSession session, Model model) {

        if (model.getAttribute("retailProduct") == null) {
            model.addAttribute("retailProduct", new RetailProduct());
        }
        model.addAttribute("step", EStep.PRODUCT_NAME_SELECTION);
        model.addAttribute("allProductNames", commonProductsDAO.findAllProductNames());
        return "prestige/products/add-new-product";
    }

    @GetMapping("/{productId}/supplies")
    public ModelAndView findAllSuppliesByProductId(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/products/supply-list");

        List<RetailProduct> prestigeProductSupplyList = prestigeProductsDAO.findPrestigeProductSupplyListByProductId(productId);

        if (prestigeProductSupplyList.isEmpty()) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/products");
        }
        else {
            mav.addObject("prestigeProductSupplyList", prestigeProductSupplyList);
        }
        return mav;
    }

    @GetMapping("/{productId}/supplies/{supplyId}")
    public ModelAndView findSupplyBySupplyId(@PathVariable("productId") Integer productId,
                                             @PathVariable("supplyId") Integer supplyId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/products/supply-edit");

        RetailProduct retailProduct = prestigeProductsDAO.findPrestigeProductSupplyByProductIdAndSupplyId(productId, supplyId);

        if (retailProduct == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/products");
        }
        else {
            mav.addObject("retailProduct", retailProduct);
        }
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("retailProduct") RetailProduct retailProduct, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "put":
                return updateRetailProduct(retailProduct, bindingResult, redirectAttributes);
            case "delete":
                return deleteRetailProduct(retailProduct.getId());
            default:
                return saveNewRetailProduct(retailProduct, bindingResult, redirectAttributes, model);
        }
    }

    private EStep getNextStep(EStep step) {

        switch (step) {
            case PRODUCT_NAME_SELECTION:
                return EStep.CAR_MODEL_SELECTION;
            case CAR_MODEL_SELECTION:
                return EStep.ENGINE_MODEL_SELECTION;
            case ENGINE_MODEL_SELECTION:
                return EStep.OEM_ARTICLE_NUMBER_SELECTION;
            case OEM_ARTICLE_NUMBER_SELECTION:
                return EStep.CROSS_ARTICLE_NUMBER_SELECTION;
            case CROSS_ARTICLE_NUMBER_SELECTION:
                return EStep.BRAND_SELECTION;
            case BRAND_SELECTION:
                return EStep.SUPPLIER_SELECTION;
            case SUPPLIER_SELECTION:
                return EStep.QUANTITY_AND_PRICE_INPUT;
            case QUANTITY_AND_PRICE_INPUT:
                return EStep.DATE_TIME_SELECTION;
            case DATE_TIME_SELECTION:
                return EStep.FINISH;
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    private EStep processAndGetNextStep(RetailProduct retailProduct, Model model, EStep step) {

        switch (step) {
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(retailProduct, model, step);
            case CAR_MODEL_SELECTION:
                return processCarModelSelection(retailProduct, model, step);
            case ENGINE_MODEL_SELECTION:
                return processEngineModelSelection(retailProduct, model, step);
            case OEM_ARTICLE_NUMBER_SELECTION:
                return processOemArticleNumberSelection(retailProduct, model, step);
            case CROSS_ARTICLE_NUMBER_SELECTION:
                return processCrossArticleNumberSelection(retailProduct, model, step);
            case BRAND_SELECTION:
                return processBrandSelection(retailProduct, model, step);
            case SUPPLIER_SELECTION:
                return processSupplierSelection(retailProduct, model, step);
            case QUANTITY_AND_PRICE_INPUT:
                return processQuantityAndPriceInput(retailProduct, model, step);
            case DATE_TIME_SELECTION:
                return processDateTimeSelection(retailProduct, model, step);
            case FINISH:
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", step.getValue()));
        }
    }

    private EStep processProductNameSelection(RetailProduct retailProduct, Model model, EStep step) {
        ProductName productName = retailProduct.getProduct().getProductName();
        productName.setProductName(commonProductNamesDAO.findProductNameById(productName.getId()).getProductName());

        model.addAttribute("availableCarModels", commonProductsDAO.findCarModelsByProductNameId(
                retailProduct.getProduct().getProductName().getId()));

        return getNextStep(step);
    }

    private EStep processCarModelSelection(RetailProduct retailProduct, Model model, EStep step) {
        List<String> availableEngineModels = commonProductsDAO.findAvailableEngineModels(retailProduct.getProduct());

        if (availableEngineModels.isEmpty()) {
            retailProduct.getProduct().setEngineModel("");
            return processEngineModelSelection(retailProduct, model, getNextStep(step));
        }
        model.addAttribute("availableEngineModels", availableEngineModels);
        return getNextStep(step);
    }

    private EStep processEngineModelSelection(RetailProduct retailProduct, Model model, EStep step) {
        List<String> availableOemArticleNumbers = commonProductsDAO.findAvailableOemArticleNumbers(retailProduct.getProduct());

        if (availableOemArticleNumbers.isEmpty()) {
            retailProduct.getProduct().setOemArticleNumber("");
            return processOemArticleNumberSelection(retailProduct, model, getNextStep(step));
        }
        model.addAttribute("availableOemArticleNumbers", availableOemArticleNumbers);
        return getNextStep(step);
    }

    private EStep processOemArticleNumberSelection(RetailProduct retailProduct, Model model, EStep step) {
        List<String> availableCrossArticleNumbers = commonProductsDAO.findAvailableCrossArticleNumbers(retailProduct.getProduct());

        if (availableCrossArticleNumbers.isEmpty()) {
            retailProduct.getProduct().setCrossArticleNumber("");
            return processCrossArticleNumberSelection(retailProduct, model, getNextStep(step));
        }
        model.addAttribute("availableCrossArticleNumbers", availableCrossArticleNumbers);
        return getNextStep(step);
    }

    private EStep processCrossArticleNumberSelection(RetailProduct retailProduct, Model model, EStep step) {
        List<String> availableBrands = commonProductsDAO.findAvailableBrands(retailProduct.getProduct());

        if (availableBrands.isEmpty()) {
            // TODO: log the message "Brand should never be null. Now it is null"
        }
        model.addAttribute("availableBrands", availableBrands);
        return getNextStep(step);
    }

    private EStep processBrandSelection(RetailProduct retailProduct, Model model, EStep step) {
        retailProduct.getProduct().setBrand(commonBrandNamesDAO.findBrandByBrandName(retailProduct.getProduct().getBrand().getBrandName()));

        List<Supplier> allSuppliers = prestigeSuppliersDAO.findAllSuppliers();
        if (allSuppliers.isEmpty()) {
            Supplier newSupplier = new Supplier(null, "", "", "СКЛАД");

            prestigeSuppliersDAO.saveNewSupplier(newSupplier);
            allSuppliers.add(newSupplier);
        }
        model.addAttribute("allSuppliers", allSuppliers);
        return getNextStep(step);
    }

    private EStep processSupplierSelection(RetailProduct retailProduct, Model model, EStep step) {
        retailProduct.setSupplier(prestigeSuppliersDAO.findSupplierById(retailProduct.getSupplier().getId()));
        return getNextStep(step);
    }

    private EStep processQuantityAndPriceInput(RetailProduct retailProduct, Model model,  EStep step) {
        retailProduct.setDeliveryDateTime(utils.getCurrentDateAndTime());
        return getNextStep(step);
    }

    private EStep processDateTimeSelection(RetailProduct retailProduct, Model model, EStep step) {
        return processFinish(retailProduct, model, getNextStep(step));
    }

    private EStep processFinish(RetailProduct retailProduct, Model model, EStep step) {
        retailProduct.getProduct().setId(commonProductsDAO.findProductId(retailProduct.getProduct()));
        prestigeProductsDAO.saveNewPrestigeProduct(retailProduct);
        return step;
    }

    private String saveNewRetailProduct(RetailProduct retailProduct, BindingResult bindingResult,
                                        RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при оприходовании нового товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/prestige/products/add");
        }

        EStep step = processAndGetNextStep(retailProduct, model, (EStep)model.getAttribute("step"));
        model.addAttribute("step", step);

        return step == EStep.FINISH ? "redirect:/prestige/products" : "prestige/products/add-new-product";
    }

    private String updateRetailProduct(RetailProduct retailProduct, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании оприходованного товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/prestige/products/%d/supplies/%d", retailProduct.getProduct().getId(),
                    retailProduct.getId());
        }
        else if (retailProduct.equals(prestigeProductsDAO.findPrestigeProductSupplyBySupplyId(retailProduct.getId()))) {
            return String.format("redirect:/prestige/products/%d/supplies", retailProduct.getProduct().getId());
        }
        prestigeProductsDAO.updatePrestigeProduct(retailProduct);

        return String.format("redirect:/prestige/products/%d/supplies", retailProduct.getProduct().getId());
    }

    private String deleteRetailProduct(Integer prestigeProductId) {
        prestigeProductsDAO.deletePrestigeProductById(prestigeProductId);

        return String.format("redirect:/prestige/products");
    }
}
