package com.prestige.controllers.common;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductEquivalentsDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeAllSalesDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.sklad.SkladAllSalesDAO;
import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.models.Product;
import com.prestige.models.enums.EStep;
import com.prestige.models.filters.ProductFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/prestige/product-management/products",
                 "/sklad/product-management/products"})
@SessionAttributes({"productStep", "productList", "product", "allProducts", "productFilter"})
public class CommonProductsController {

    private final CommonProductsDAO commonProductsDAO;

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    private final CommonProductEquivalentsDAO commonProductEquivalentsDAO;

    private final PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;

    private final PrestigeAllSalesDAO prestigeAllSalesDAO;

    private final SkladAllSuppliesDAO skladAllSuppliesDAO;

    private final SkladAllSalesDAO skladAllSalesDAO;

    @Autowired
    public CommonProductsController(CommonProductsDAO commonProductsDAO,
                                    CommonProductNamesDAO commonProductNamesDAO,
                                    CommonBrandNamesDAO commonBrandNamesDAO,
                                    CommonProductEquivalentsDAO commonProductEquivalentsDAO,
                                    PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                    PrestigeAllSalesDAO prestigeAllSalesDAO,
                                    SkladAllSuppliesDAO skladAllSuppliesDAO,
                                    SkladAllSalesDAO skladAllSalesDAO) {

        this.commonProductsDAO = commonProductsDAO;
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonBrandNamesDAO = commonBrandNamesDAO;
        this.commonProductEquivalentsDAO = commonProductEquivalentsDAO;
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
        this.prestigeAllSalesDAO = prestigeAllSalesDAO;
        this.skladAllSuppliesDAO = skladAllSuppliesDAO;
        this.skladAllSalesDAO = skladAllSalesDAO;
    }

    @GetMapping
    public String showAllProducts(Model model) {
        model.addAttribute("allProducts", commonProductsDAO.findAllProducts());
        model.addAttribute("product", new Product()); //mock
        model.addAttribute("productFilter", new ProductFilter());
        return "common/products/products";
    }

    @GetMapping("/add")
    public String addNewProductGet(Model model) {

        model.addAttribute("productStep", EStep.PRODUCT_SELECTION_DONE);
        model.addAttribute("productList", new LinkedList<Product>());
        model.addAttribute("product", new Product()); //mock
        return "common/products/add-new-product";
    }

    @GetMapping("/{productId}")
    public ModelAndView findProductById(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView("common/products/edit-product");
        Product product = commonProductsDAO.findProductById(productId);

        if (product == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/products",
                    model.getAttribute("URIBeginning")));
        }
        else {
            mav.addObject("product", product);
            mav.addObject("allProductNames", commonProductNamesDAO.findAllProductNames());
            mav.addObject("allBrands", commonBrandNamesDAO.findAllBrands());
        }
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("product") Product product,
                                       @ModelAttribute("productFilter") ProductFilter productFilter,
                                       BindingResult bindingResult,
                                       @RequestParam("method") String method,
                                       @RequestParam(value = "selectedProductIds", required = false) List<Integer> selectedProductIds,
                                       @RequestParam(value = "selectedEquivalentProductIds", required = false) List<Integer> selectedEquivalentProductIds,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "put":
                return updateProduct(product, bindingResult, redirectAttributes, model);
            case "delete":
                return deleteProduct(product.getId(), redirectAttributes, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(model);
            case "applyFilter":
                return applyFilter(productFilter, model);
            case "resetFilter":
                return resetFilter(productFilter, model);
            case "markAsSame":
                return markAsSame(selectedEquivalentProductIds, redirectAttributes, model);
            case "unmarkAsSame":
                return unmarkAsSame(selectedEquivalentProductIds, model);
            case "saveAndClose":
                if (model.getAttribute("productStep") == EStep.PRODUCT_NAME_SELECTION) {
                    saveNewProduct(bindingResult, model, redirectAttributes);
                }
                return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
            default:
                return saveNewProduct(bindingResult, model, redirectAttributes);
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

    private String resetAddingNewProduct(Model model) {
        ((LinkedList<Product>)model.getAttribute("productList")).removeLast();

        model.addAttribute("productStep", EStep.PRODUCT_SELECTION_DONE);
        model.addAttribute("URIBeginning", model.getAttribute("URIBeginning"));

        return "common/products/add-new-product";
    }

    private String applyFilter(ProductFilter productFilter, Model model) {
        ProductFilter productFilterCopy = new ProductFilter(productFilter);
        productFilterCopy.prepareAllFields();

        model.addAttribute("allProducts", commonProductsDAO.findAllProductsWIthFilter(productFilterCopy));

        return "common/products/products";
    }

    private String resetFilter(ProductFilter productFilter, Model model) {
        productFilter.clear();
        return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
    }

    private String markAsSame(List<Integer> selectedEquivalentProductIds, RedirectAttributes redirectAttributes,
                              Model model) {

        if (selectedEquivalentProductIds == null || selectedEquivalentProductIds.size() < 2) {
            return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
        }

        List<Product> allProducts = (List<Product>)model.getAttribute("allProducts");

        Map<Integer, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Integer groupId = null;

        for (Integer id : selectedEquivalentProductIds) {
            Product product = productMap.get(id);

            if (product != null && product.getEquivalentProductInfo() != null &&
                    !product.getEquivalentProductInfo().getGroupId().equals(0)) {

                if (groupId == null) {
                    groupId = product.getEquivalentProductInfo().getGroupId();
                }
                else if (!product.getEquivalentProductInfo().getGroupId().equals(groupId)) {
                    String errorMessage = "Ошибка: попытка отметить товары как одинаковые, состоящие уже в других объединениях.";
                    redirectAttributes.addFlashAttribute("error", errorMessage);

                    return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
                }
            }
        }

        commonProductEquivalentsDAO.markAsSame(selectedEquivalentProductIds);

        return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
    }

    private String unmarkAsSame(List<Integer> selectedEquivalentProductIds, Model model) {

        if (selectedEquivalentProductIds != null && !selectedEquivalentProductIds.isEmpty()) {
            commonProductEquivalentsDAO.unmarkAsSame(selectedEquivalentProductIds);
        }
        return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
    }

    private String saveNewProduct(BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/product-management/products/add", model.getAttribute("URIBeginning"));
        }

        EStep productStep = processAndGetNextStep(model, (EStep)model.getAttribute("productStep"));
        model.addAttribute("productStep", productStep);
        model.addAttribute("URIBeginning", model.getAttribute("URIBeginning"));

        return "common/products/add-new-product";
    }

    private String updateProduct(Product product, BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes, Model model) {
        completeProduct(product);

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d", model.getAttribute("URIBeginning"));
        }
        else if (product.equals(commonProductsDAO.findProductById(product.getId()))) {
            return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
        }
        else if (commonProductsDAO.checkIfProductExists(product)) {
            String errorMessage = "Указанный Вами товар уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d", model.getAttribute("URIBeginning"));
        }
        commonProductsDAO.updateProduct(product);

        return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
    }

    private String deleteProduct(Integer productId, RedirectAttributes redirectAttributes, Model model) {
        String errorMessage = null;

        if (prestigeAllSuppliesDAO.checkIfProductExists(productId) || skladAllSuppliesDAO.checkIfProductExists(productId)) {
            errorMessage = String.format("Невозможно удалить этот товар, т.к он используется в поступлениях %s.",
                    model.getAttribute("URIBeginning").equals("prestige") ? "магазина" : "склада");
        }
        else if (prestigeAllSalesDAO.checkIfProductExists(productId) || skladAllSalesDAO.checkIfProductExists(productId)) {
            errorMessage = String.format("Невозможно удалить этот товар, т.к он используется в продажах %s.",
                    model.getAttribute("URIBeginning").equals("prestige") ? "магазина" : "склада");
        }
        else if (commonProductEquivalentsDAO.checkIfProductExists(productId)) {
            errorMessage = "Невозможно удалить этот товар, т.к он состоит в объединениях с другими товарами.";
        }

        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/product-management/products/%d", model.getAttribute("URIBeginning"), productId);
        }

        commonProductsDAO.deleteProductById(productId);
        return String.format("redirect:/%s/product-management/products", model.getAttribute("URIBeginning"));
    }

    private void completeProduct(Product product) {
        product.setProductName(commonProductNamesDAO.findProductNameById(product.getProductName().getId()));
        product.setBrand(commonBrandNamesDAO.findBrandById(product.getBrand().getId()));
    }
}
