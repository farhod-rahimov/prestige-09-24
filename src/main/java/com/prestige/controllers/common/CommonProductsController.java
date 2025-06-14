package com.prestige.controllers.common;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.models.EStep;
import com.prestige.models.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.List;

@Controller
@RequestMapping({"/prestige/product-management/products",
                 "/sklad/product-management/products"})
@SessionAttributes({"productStep", "productList", "product"})
public class CommonProductsController extends AbstractCommonBaseController {

    private final CommonProductsDAO commonProductsDAO;

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    private final PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;

    @Autowired
    public CommonProductsController(CommonProductsDAO commonProductsDAO,
                                    CommonProductNamesDAO commonProductNamesDAO,
                                    CommonBrandNamesDAO commonBrandNamesDAO,
                                    PrestigeAllSuppliesDAO prestigeAllSuppliesDAO) {

        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
    }

    @GetMapping
    public String showAllProducts(Model model, HttpServletRequest request) {
        model.addAttribute("allProducts", commonProductsDAO.findAllProducts());
        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/products/products";
    }

    @GetMapping("/add")
    public String addNewProductGet(Model model, HttpServletRequest request) {

        model.addAttribute("productStep", EStep.PRODUCT_SELECTION_DONE);
        model.addAttribute("productList", new LinkedList<Product>());
        model.addAttribute("product", new Product()); //mock

        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/products/add-new-product";
    }

    @GetMapping("/{productId}")
    public ModelAndView findProductById(@PathVariable("productId") Integer productId, Model model, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("common/products/edit-product");
        Product product = commonProductsDAO.findProductById(productId);

        if (product == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/products",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad"));
        }
        else {
            mav.addObject("product", product);
            mav.addObject("allProductNames", commonProductNamesDAO.findAllProductNames());
            mav.addObject("allBrands", commonBrandNamesDAO.findAllBrands());
        }
        mav.addObject("URIBeginning", getURIBeginning(request));
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("product") Product product, BindingResult bindingResult,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedProductIds", required = false) List<Integer> selectedProductIds,
                                       RedirectAttributes redirectAttributes,
                                       Model model, HttpServletRequest request) {

        switch (method) {
            case "put":
                return updateProduct(product, bindingResult, redirectAttributes, request);
            case "delete":
                return deleteProduct(product.getId(), redirectAttributes, request);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(model, request);
            case "deleteSelected":
                return deleteSelected(selectedProductIds, model, request);
            case "saveAndClose":
                if (model.getAttribute("productStep") == EStep.PRODUCT_NAME_SELECTION) {
                    saveNewProduct(bindingResult, model, redirectAttributes, request);
                }
                return String.format("redirect:/%s/product-management/products", getURIBeginning(request));
            default:
                return saveNewProduct(bindingResult, model, redirectAttributes, request);
        }
    }

    private EStep getNextStep(EStep productStep) {

        switch (productStep) {
            case PRODUCT_SELECTION_DONE:
                return EStep.PRODUCT_NAME_SELECTION;
            case PRODUCT_NAME_SELECTION:
                return EStep.PRODUCT_SELECTION_DONE;
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", productStep.getValue()));
        }
    }

    EStep processAndGetNextStep(Model model, EStep productStep) {

        LinkedList<Product> productList = (LinkedList<Product>)model.getAttribute("productList");

        switch (productStep) {
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(productList, model, productStep);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(productList, model, productStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", productStep.getValue()));
        }
    }

    private EStep processProductSelectionDone(LinkedList<Product> productList, Model model, EStep productStep) {
        productList.add(new Product());

        model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
        model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
        model.addAttribute("product", productList.getLast());

        return getNextStep(productStep);
    }

    private EStep processProductNameSelection(LinkedList<Product> productList, Model model, EStep productStep) {

        Product product = productList.getLast();
        product.setProductName(commonProductNamesDAO.findProductNameById(product.getProductName().getId()));
        product.setBrand(commonBrandNamesDAO.findBrandById(product.getBrand().getId()));

        if (!commonProductsDAO.saveNewProduct(product)) {
            model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
            model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());

            String errorMessage = "Указанный Вами товар уже существует.";
            model.addAttribute("error", errorMessage);
            return productStep;
        }
        return getNextStep(productStep);
    }

    private String resetAddingNewProduct(Model model, HttpServletRequest request) {
        ((LinkedList<Product>)model.getAttribute("productList")).removeLast();

        model.addAttribute("productStep", EStep.PRODUCT_SELECTION_DONE);
        model.addAttribute("URIBeginning", getURIBeginning(request));

        return "common/products/add-new-product";
    }

    private String deleteSelected(List<Integer> selectedProductIds, Model model, HttpServletRequest request) {
        LinkedList<Product> productList = (LinkedList<Product>)model.getAttribute("productList");

        for (Integer productId : selectedProductIds) {
            productList.removeIf(product -> product.getId().equals(productId));
        }

        commonProductsDAO.deleteProductIds(selectedProductIds);

        model.addAttribute("productStep", EStep.PRODUCT_SELECTION_DONE);
        model.addAttribute("URIBeginning", getURIBeginning(request));

        return "common/products/add-new-product";
    }

    private String saveNewProduct(BindingResult bindingResult, Model model,
                                  RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/product-management/products/add", getURIBeginning(request));
        }

        EStep productStep = processAndGetNextStep(model, (EStep)model.getAttribute("productStep"));
        model.addAttribute("productStep", productStep);
        model.addAttribute("URIBeginning", getURIBeginning(request));

        return "common/products/add-new-product";
    }

    private String updateProduct(Product product, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, HttpServletRequest request) {
        completeProduct(product);

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", product.getId());
        }
        else if (product.equals(commonProductsDAO.findProductById(product.getId()))) {
            return String.format("redirect:/%s/product-management/products",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }
        else if (commonProductsDAO.checkIfProductExists(product)) {
            String errorMessage = "Указанный Вами товар уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", product.getId());
        }
        commonProductsDAO.updateProduct(product);

        return String.format("redirect:/%s/product-management/products",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private String deleteProduct(Integer productId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // TODO: prestigeAllSuppliesDAO.checkIfProductExists or skladAllSuppliesDAO.checkIfProductExists
        if (prestigeAllSuppliesDAO.checkIfProductExists(productId)) {
            String errorMessage = String.format("Невозможно удалить этот товар, т.к он используется в поступлениях %s.",
                    isPrestigeRequest(request.getRequestURI()) ? "магазина" : "склада");

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", productId);
        }

        commonProductsDAO.deleteProductById(productId);

        return String.format("redirect:/%s/product-management/products",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private void completeProduct(Product product) {
        product.setProductName(commonProductNamesDAO.findProductNameById(product.getProductName().getId()));
        product.setBrand(commonBrandNamesDAO.findBrandById(product.getBrand().getId()));
    }
}
