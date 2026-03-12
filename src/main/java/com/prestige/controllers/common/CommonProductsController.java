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
import com.prestige.utils.AbstractTabStateManager;
import com.prestige.utils.ProductTabState;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping({"/prestige/product-management/products",
                 "/sklad/product-management/products"})
public class CommonProductsController extends AbstractTabStateManager {

    private final CommonProductsDAO commonProductsDAO;

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    private final CommonProductEquivalentsDAO commonProductEquivalentsDAO;

    private final PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;

    private final PrestigeAllSalesDAO prestigeAllSalesDAO;

    private final SkladAllSuppliesDAO skladAllSuppliesDAO;

    private final SkladAllSalesDAO skladAllSalesDAO;

    protected String getTabStateMapName() {
        return "productTabStateMap";
    }

    private String getUriPrefix(Model model) {
        return (String)model.getAttribute("URIBeginning");
    }

    @GetMapping
    public String showAllProducts(Model model) {
        model.addAttribute("allProducts", commonProductsDAO.findAllProducts());
        model.addAttribute("productFilter", new ProductFilter());
        return "common/products/products";
    }

    private String checkBindingResult(RedirectAttributes redirectAttributes, BindingResult bindingResult,
                                      Model model, String method) {

        String errorMessage = null;

        if (bindingResult.hasErrors()) {

            switch (method) {
                case "applyFilter":
                    errorMessage = "Возникла ошибка при применении фильтра. Пожалуйста, попробуйте заново."; break;
                case "put":
                    errorMessage = "Возникла ошибка при редактировании товара. Пожалуйста, попробуйте заново."; break;
                case "delete":
                    errorMessage = "Возникла ошибка при удалении товара. Пожалуйста, попробуйте заново."; break;
                default:
                    errorMessage = "Возникла ошибка при создании нового товара. Пожалуйста, попробуйте заново."; break;

            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            model.addAttribute("productFilter", new ProductFilter());

            return String.format("redirect:/%s/product-management/products/", getUriPrefix(model));
        }
        return null;
    }

    @PostMapping()
    public String postMethodDispatcher(@RequestParam("method") String method,
                                       @RequestParam(value = "tabId", required = false) String tabId,
                                       @ModelAttribute("product") Product product,
                                       @ModelAttribute("productFilter") ProductFilter productFilter,
                                       @RequestParam(value = "selectedEquivalentProductIds", required = false) List<Integer> selectedEquivalentProductIds,
                                       @RequestParam(value = "selectedProductIds", required = false) List<Integer> selectedProductIds,
                                       RedirectAttributes redirectAttributes, BindingResult bindingResult,
                                       Model model, HttpSession session) {

        String returnPage = checkBindingResult(redirectAttributes, bindingResult, model, method);
        if (returnPage != null) {
            return returnPage;
        }

        switch (method) {
            case "applyFilter":
                return applyFilter(productFilter, model);
            case "resetFilter":
                return resetFilter(productFilter, model);
            case "markAsSame":
                return markAsSame(selectedEquivalentProductIds, redirectAttributes, model);
            case "unmarkAsSame":
                return unmarkAsSame(selectedEquivalentProductIds, model);
            case "put":
                return updateProduct(product, redirectAttributes, model);
            case "delete":
                return deleteProduct(product.getId(), redirectAttributes, model);
            case "resetAddingNewProduct":
                return resetAddingNewProduct(model, session, redirectAttributes, tabId);
            case "deleteSelected":
                return deleteSelected(selectedProductIds, model, session, redirectAttributes, tabId);
            case "saveAndClose": {
                return saveNewAndClose(model, session, redirectAttributes, product, tabId);
            }
            default:
                return saveNewProduct(model, session, redirectAttributes, product, tabId, false);
        }
    }

    private String applyFilter(ProductFilter productFilter, Model model) {
        ProductFilter productFilterCopy = new ProductFilter(productFilter);
        productFilterCopy.prepareAllFields();

        model.addAttribute("allProducts", commonProductsDAO.findAllProductsWIthFilter(productFilterCopy));

        return "common/products/products";
    }

    private String resetFilter(ProductFilter productFilter, Model model) {
        productFilter.clear();
        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private String markAsSame(List<Integer> selectedEquivalentProductIds,
                              RedirectAttributes redirectAttributes, Model model) {

        if (selectedEquivalentProductIds == null || selectedEquivalentProductIds.size() < 2) {
            return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
        }

        List<Product> allProducts = commonProductsDAO.findAllProducts();

        Map<Integer, Product> productMap = allProducts.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        Integer groupId = null;

        Iterator<Integer> it = selectedEquivalentProductIds.iterator();

        while(it.hasNext()) {
            Integer id = it.next();
            Product product = productMap.get(id);

            if (product != null && product.getEquivalentProductInfo() != null &&
                    !product.getEquivalentProductInfo().getGroupId().equals(0)) {

                if (groupId == null) {
                    groupId = product.getEquivalentProductInfo().getGroupId();
                }
                else if (!product.getEquivalentProductInfo().getGroupId().equals(groupId)) {

                    if (!redirectAttributes.getFlashAttributes().containsKey("warning")) {
                        String warningMessage = "К сведению: некоторые товары не были отмечены как одинаковые, т. к. они состоят уже в других объединениях.";
                        redirectAttributes.addFlashAttribute("warning", warningMessage);
                    }
                    it.remove();
                }
            }
        }

        commonProductEquivalentsDAO.markAsSame(selectedEquivalentProductIds);

        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private String unmarkAsSame(List<Integer> selectedEquivalentProductIds, Model model) {

        if (selectedEquivalentProductIds != null && !selectedEquivalentProductIds.isEmpty()) {
            commonProductEquivalentsDAO.unmarkAsSame(selectedEquivalentProductIds);
        }
        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    @GetMapping("/{productId}")
    public ModelAndView findProductById(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView("common/products/edit-product");
        Product product = commonProductsDAO.findProductById(productId);

        if (product == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/products", getUriPrefix(model)));
        }
        else {
            mav.addObject("product", product);
            mav.addObject("allProductNames", commonProductNamesDAO.findAllProductNames());
            mav.addObject("allBrands", commonBrandNamesDAO.findAllBrands());
        }
        return mav;
    }

    private void completeProduct(Product product) {
        product.setProductName(commonProductNamesDAO.findProductNameById(product.getProductName().getId()));
        product.setBrand(commonBrandNamesDAO.findBrandById(product.getBrand().getId()));
    }

    private String updateProduct(Product product, RedirectAttributes redirectAttributes, Model model) {
        completeProduct(product);

        if (product.equals(commonProductsDAO.findProductById(product.getId()))) {
            return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
        }
        else if (commonProductsDAO.checkIfProductExists(product)) {
            String errorMessage = "Указанный Вами товар уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/%d", getUriPrefix(model));
        }

        commonProductsDAO.updateProduct(product);

        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private String deleteProduct(Integer productId, RedirectAttributes redirectAttributes, Model model) {
        String errorMessage = null;

        if (prestigeAllSuppliesDAO.checkIfProductExists(productId) || skladAllSuppliesDAO.checkIfProductExists(productId)) {
            errorMessage = String.format("Невозможно удалить этот товар, т.к он используется в поступлениях %s.",
                    getUriPrefix(model).equals("prestige") ? "магазина" : "склада");
        }
        else if (prestigeAllSalesDAO.checkIfProductExists(productId) || skladAllSalesDAO.checkIfProductExists(productId)) {
            errorMessage = String.format("Невозможно удалить этот товар, т.к он используется в продажах %s.",
                    getUriPrefix(model).equals("prestige") ? "магазина" : "склада");
        }
        else if (commonProductEquivalentsDAO.checkIfProductExists(productId)) {
            errorMessage = "Невозможно удалить этот товар, т.к он состоит в объединениях с другими товарами.";
        }

        if (errorMessage != null) {
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/product-management/products/%d", getUriPrefix(model), productId);
        }

        commonProductsDAO.deleteProductById(productId);
        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private void addAttributesToModel(Model model, ProductTabState state) {
        model.addAttribute("tabId", state.getTabId());
        model.addAttribute("productStep", state.getStep());
        model.addAttribute("productList", state.getProductList());
    }

    private String stateTimedOut(RedirectAttributes redirectAttributes, Model model) {
        redirectAttributes.addFlashAttribute("warning", "К сведению: сессия истекла, начните добавление товаров заново");
        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private String resetAddingNewProduct(Model model, HttpSession session, RedirectAttributes redirectAttributes, String tabId) {
        ProductTabState state = (ProductTabState)getTabState(session, getTabStateMapName(), tabId);

        if (state == null) { // state timed out
            stateTimedOut(redirectAttributes, model);
        }

        state.getProductList().removeLast();
        state.setStep(EStep.PRODUCT_SELECTION_DONE);

        addAttributesToModel(model, state);

        return "common/products/add-new-product";
    }

    private String deleteSelected(List<Integer> selectedProductIds, Model model, HttpSession session, RedirectAttributes redirectAttributes, String tabId) {
        ProductTabState state = (ProductTabState)getTabState(session, getTabStateMapName(), tabId);

        if (state == null) { // state timed out
            return stateTimedOut(redirectAttributes, model);
        }

        if (selectedProductIds != null) {
            commonProductsDAO.deleteProductByIds(selectedProductIds);

            for (Integer id : selectedProductIds) {

                for (Product product : state.getProductList()) {

                    if (product.getId().equals(id)) {
                        state.getProductList().remove(product);
                        break;
                    }
                }
            }
        }

        addAttributesToModel(model, state);

        return "common/products/add-new-product";
    }

    @GetMapping("/add")
    public String addNewProductGet(Model model, HttpSession session) {
        ProductTabState state = new ProductTabState(UUID.randomUUID().toString(), EStep.PRODUCT_SELECTION_DONE, new LinkedList<Product>());
        saveTabState(session, getTabStateMapName(), state);

        addAttributesToModel(model, state);

        return "common/products/add-new-product";
    }

    private String saveNewAndClose(Model model, HttpSession session, RedirectAttributes redirectAttributes,
                                   Product productForm, String tabId) {

        ProductTabState state = (ProductTabState)getTabState(session, getTabStateMapName(), tabId);

        if (state == null) { // state timed out
            return stateTimedOut(redirectAttributes, model);
        }
        else if (state.getStep() == EStep.PRODUCT_NAME_SELECTION) { // save last product
            saveNewProduct(model, session, redirectAttributes, productForm, tabId, true);
        }
        return String.format("redirect:/%s/product-management/products", getUriPrefix(model));
    }

    private String saveNewProduct(Model model, HttpSession session, RedirectAttributes redirectAttributes,
                                  Product productForm, String tabId, boolean justSave) {

        ProductTabState state = (ProductTabState)getTabState(session, getTabStateMapName(), tabId);

        if (state == null) { // state timed out
            return stateTimedOut(redirectAttributes, model);
        }

        if (state.getStep() == EStep.PRODUCT_NAME_SELECTION) {
            state.getProductList().removeLast();
            state.getProductList().add(productForm);
        }

        EStep productStep = processAndGetNextStep(model, state.getStep(), state.getProductList());

        if (!justSave) {
            state.setStep(productStep);

            addAttributesToModel(model, state);

            return "common/products/add-new-product";
        }
        return null;
    }

    EStep processAndGetNextStep(Model model, EStep productStep, LinkedList<Product> productList) {

        switch (productStep) {
            case PRODUCT_SELECTION_DONE:
                return processProductSelectionDone(model, productList, productStep);
            case PRODUCT_NAME_SELECTION:
                return processProductNameSelection(model, productList, productStep);
            default:
                throw new RuntimeException(String.format("Step is undefined: '%s'", productStep.getValue()));
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

    private EStep processProductSelectionDone(Model model, LinkedList<Product> productList, EStep productStep) {
        productList.add(new Product());

        model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
        model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
        model.addAttribute("product", productList.getLast());

        return getNextStep(productStep);
    }

    private EStep processProductNameSelection(Model model, LinkedList<Product> productList, EStep productStep) {

        Product product = productList.getLast();
        completeProduct(product);

        if (!commonProductsDAO.saveNewProduct(product)) {
            model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
            model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
            model.addAttribute("error", "Указанный Вами товар уже существует.");

            return productStep;
        }
        return getNextStep(productStep);
    }
}
