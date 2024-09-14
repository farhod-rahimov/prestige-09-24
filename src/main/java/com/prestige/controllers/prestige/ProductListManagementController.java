package com.prestige.controllers.prestige;

import com.prestige.dao.PrestigeSalesDAO;
import com.prestige.dao.ProductListManagementDAO;
import com.prestige.models.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

@Controller
@RequestMapping("/prestige/productListManagement")
@SessionAttributes("newPrestigeProduct") // Указываем, что объект "newPrestigeProduct" нужно сохранять в сессии
public class ProductListManagementController {

    private final ProductListManagementDAO productListManagementDAO;

    private final Settings settings;

    @Autowired
    public ProductListManagementController(ProductListManagementDAO productListManagementDAO, Settings settings) {
        this.productListManagementDAO = productListManagementDAO;
        this.settings = settings;
    }

    @GetMapping()
    public String main() {
        return "prestige/productListManagement"; // return /prestige/productListManagement.html
    }

    @GetMapping("/addNewClient")
    public String addNewClientGet(Model model) {
        model.addAttribute("newClient", new Client());
        return "prestige/addNewClient"; // return /prestige/addNewClient.html
    }

    @PostMapping("/addNewClient")
    public String addNewClientPost(@ModelAttribute("newClient") Client newClient,
                                    BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewClientGet(model);
        }

        if (!productListManagementDAO.saveNewClient(newClient)) {
            String errorMessage = String.format("Клиент с именем - %s и номером телефона - %s уже существует",
                    newClient.getName(), newClient.getPhoneNumber());
            model.addAttribute("error", errorMessage);
            return addNewClientGet(model);
        };
        return "redirect:/prestige/productListManagement/showAllClients";
    }

    @GetMapping("/showAllClients")
    public String showAllClients(Model model) {
        model.addAttribute("allClients", productListManagementDAO.showAllClients());
        return "prestige/showAllClients"; // return /prestige/showAllClients.html
    }

    @GetMapping("/addNewStore")
    public String addNewStoreGet(Model model) {
        model.addAttribute("newStore", new Store());
        return "prestige/addNewStore"; // return /prestige/addNewClient.html
    }

    @PostMapping("/addNewStore")
    public String addNewStorePost(@ModelAttribute("newStore") Store newStore,
                              BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewStoreGet(model);
        }

        if (!productListManagementDAO.saveNewStore(newStore)) {
            model.addAttribute("error", String.format("Поставщик - %s уже существует", newStore.getStoreName()));
            return addNewStoreGet(model);
        };
        return "redirect:/prestige/productListManagement/showAllStores";
    }

    @GetMapping("/showAllStores")
    public String showAllStores(Model model) {
        model.addAttribute("allStores", productListManagementDAO.showAllStores());
        return "prestige/showAllStores"; // return /prestige/showAllStores.html
    }

    @GetMapping("/addNewBrand")
    public String addNewBrandGet(Model model) {
        model.addAttribute("newBrand", new Brand());
        return "prestige/addNewBrand"; // return /prestige/addNewBrand.html
    }

    @PostMapping("/addNewBrand")
    public String addNewStorePost(@ModelAttribute("newBrand") Brand newBrand,
                                  BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewBrandGet(model);
        }

        if (!productListManagementDAO.saveNewBrand(newBrand)) {
            model.addAttribute("error", String.format("Бренд - %s уже существует", newBrand.getBrandName()));
            return addNewBrandGet(model);
        };
        return "redirect:/prestige/productListManagement/showAllBrands";
    }

    @GetMapping("/showAllBrands")
    public String showAllBrands(Model model) {
        model.addAttribute("allBrands", productListManagementDAO.showAllBrands());
        return "prestige/showAllBrands"; // return /prestige/showAllBrands.html
    }

    @GetMapping("/addNewProductName")
    public String addNewProductNameGet(Model model) {
        model.addAttribute("newProductName", new ProductName());
        return "prestige/addNewProductName"; // return /prestige/addNewProductName.html
    }

    @PostMapping("/addNewProductName")
    public String addNewProductNamePost(@ModelAttribute("newProductName") ProductName newProductName,
                                  BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewProductNameGet(model);
        }

        if (!productListManagementDAO.saveNewProductName(newProductName)) {
            model.addAttribute("error", String.format("Название товара - %s уже существует", newProductName.getProductName()));
            return addNewProductNameGet(model);
        };
        return "redirect:/prestige/productListManagement/showAllProductNames";
    }

    @GetMapping("/showAllProductNames")
    public String showAllProductNames(Model model) {
        model.addAttribute("allProductNames", productListManagementDAO.showAllProductNames());
        return "prestige/showAllProductNames"; // return /prestige/showAllProductNames.html
    }

    @GetMapping("/addNewProduct")
    public String addNewProductGet(Model model) {
        model.addAttribute("newProduct", new Product());
        model.addAttribute("allProductNames", productListManagementDAO.showAllProductNames());
        model.addAttribute("allBrands", productListManagementDAO.showAllBrands());
        return "prestige/addNewProduct"; // return /prestige/addNewProduct.html
    }

    private void completeNewProduct(Product newProduct) {
        if (newProduct.getProductName() != null && newProduct.getProductName().getId() != null &&
            newProduct.getProductName().getProductName() == null) {

            newProduct.getProductName().setProductName(productListManagementDAO.getProductNameById(newProduct.getProductName().getId()));
        }

        if (newProduct.getBrand() != null && newProduct.getBrand().getId() != null &&
            newProduct.getBrand().getBrandName() == null) {

            newProduct.getBrand().setBrandName(productListManagementDAO.getBrandNameById(newProduct.getBrand().getId()));
        }

        if (newProduct.getId() == null && newProduct.getProductName() != null &&
            newProduct.getCarModel() != null && newProduct.getArticleNumber() != null &&
            newProduct.getBrand() != null) {

            newProduct.setId(productListManagementDAO.getCommonProductsId(newProduct.getBrand().getId(),
                    newProduct.getProductName().getId(), newProduct.getCarModel(), newProduct.getArticleNumber()));
        }
    }

    @PostMapping("/addNewProduct")
    public String addNewProductPost(@ModelAttribute("newProduct") Product newProduct,
                                        BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewProductGet(model);
        }

        completeNewProduct(newProduct);

        if (!productListManagementDAO.saveNewProduct(newProduct)) {
            model.addAttribute("error", String.format("Товар - %s | %s | %s | %s уже существует",
                    newProduct.getProductName().getProductName(),
                    newProduct.getCarModel(),
                    newProduct.getArticleNumber(),
                    newProduct.getBrand().getBrandName()));
            return addNewProductGet(model);
        };
        return "redirect:/prestige/productListManagement/showAllProducts";
    }

    @GetMapping("/showAllProducts")
    public String showAllProducts(Model model) {
        model.addAttribute("showProductNameSelectionSection", true);
        model.addAttribute("allProducts", productListManagementDAO.showAllProducts());
        return "prestige/showAllProducts"; // return /prestige/showAllProducts.html
    }


    private void removeAttributesFromSession(HttpSession session) {
        List<String> attributeNames = Arrays.asList("showProductNameSelectionSection",
                "showCarModelSelectionSection", "showArticleNumberSelectionSection",
                "showBrandSelectionSection", "showPriceSection", "showQuantitySection",
                "showStoreSelectionSection");

        for (String attributeName : attributeNames) {
            session.removeAttribute(attributeName);
        }
    }

    @GetMapping("/addNewPrestigeProduct")
    public String addNewPrestigeProductGet(HttpSession session, Model model) {

        model.addAttribute("newPrestigeProduct", new RetailProduct());

        removeAttributesFromSession(session);

        model.addAttribute("allProductNames", productListManagementDAO.showAllProductNames());
        model.addAttribute("showProductNameSelectionSection", true);

        session.setAttribute("showProductNameSelectionSection", true);

        return "prestige/addNewPrestigeProduct"; // return /prestige/addNewPrestigeProduct.html
    }

    private void completeNewRetailProduct(RetailProduct retailProduct) {
        completeNewProduct(retailProduct.getProduct());

        if (retailProduct.getStore() != null && retailProduct.getStore().getId() != null &&
            retailProduct.getStore().getStoreName() == null) {

            retailProduct.getStore().setStoreName(productListManagementDAO.getStoreNameById(retailProduct.getStore().getId()));
        }
    }

    @PostMapping("/addNewPrestigeProduct")
    public String addNewPrestigeProductPost(@ModelAttribute("newPrestigeProduct") RetailProduct newPrestigeProduct,
                                    BindingResult bindingResult, Model model, HttpSession session) {

        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            String error = String.format("Введено некорректное значение - '%s' в последнем поле. " +
                    "Пожалуйста укажите верное значение снова.", fieldError.getRejectedValue());
            model.addAttribute("error", error);

            return addNewPrestigeProductGet(session, model);
        }

        if (session.getAttribute("showProductNameSelectionSection") != null) {
            model.addAttribute("showProductNameSelectionSection", true);
            completeNewRetailProduct(newPrestigeProduct);

            if (session.getAttribute("showCarModelSelectionSection") == null) {
                model.addAttribute("allCarModels",
                        productListManagementDAO.getCarModelsByProductNameId(newPrestigeProduct.getProduct().getProductName().getId()));

                model.addAttribute("showCarModelSelectionSection", true);
                session.setAttribute("showCarModelSelectionSection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showCarModelSelectionSection") != null) {
            model.addAttribute("showCarModelSelectionSection", true);

            if (session.getAttribute("showArticleNumberSelectionSection") == null) {
                model.addAttribute("allArticleNumbers",
                        productListManagementDAO.getArticleNumbersByProductNameIdAndCarModel(
                                newPrestigeProduct.getProduct().getProductName().getId(),
                                newPrestigeProduct.getProduct().getCarModel()));

                model.addAttribute("showArticleNumberSelectionSection", true);
                session.setAttribute("showArticleNumberSelectionSection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showArticleNumberSelectionSection") != null) {
            model.addAttribute("showArticleNumberSelectionSection", true);

            if (session.getAttribute("showBrandSelectionSection") == null) {
                model.addAttribute("allBrands",
                        productListManagementDAO.getBrandsByProductNameIdAndCarModelAndArticleNumber(
                                newPrestigeProduct.getProduct().getProductName().getId(),
                                newPrestigeProduct.getProduct().getCarModel(),
                                newPrestigeProduct.getProduct().getArticleNumber()));

                model.addAttribute("showBrandSelectionSection", true);
                session.setAttribute("showBrandSelectionSection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showBrandSelectionSection") != null) {
            model.addAttribute("showBrandSelectionSection", true);

            if (session.getAttribute("showPriceSection") == null) {
                model.addAttribute("showPriceSection", true);
                session.setAttribute("showPriceSection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showPriceSection") != null) {
            model.addAttribute("showPriceSection", true);

            if (session.getAttribute("showQuantitySection") == null) {
                model.addAttribute("showQuantitySection", true);
                session.setAttribute("showQuantitySection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showQuantitySection") != null) {
            model.addAttribute("showQuantitySection", true);

            if (session.getAttribute("showStoreSelectionSection") == null) {
                model.addAttribute("allStores", productListManagementDAO.showAllStores());

                model.addAttribute("showStoreSelectionSection", true);
                session.setAttribute("showStoreSelectionSection", true);
                return "prestige/addNewPrestigeProduct";
            }
        }

        if (session.getAttribute("showStoreSelectionSection") != null) {
            model.addAttribute("showStoreSelectionSection", true);
        }

        productListManagementDAO.saveNewRetailProduct(newPrestigeProduct);

        return "redirect:/prestige/productListManagement/showAllPrestigeProducts";
    }

    @GetMapping("/showAllPrestigeProducts")
    public String showAllPrestigeProducts(@RequestParam(name = "magicValue", required = false) String magicValue,
                                          @RequestParam(name = "exchangeRate", required = false) String exchangeRate,
                                          Model model) {

        if (magicValue != null && !magicValue.equals(settings.getValueByKey("magicValue"))) {
            productListManagementDAO.updateSettings("magicValue", magicValue);
            settings.setValueByKey("magicValue", magicValue);
        }
        model.addAttribute("magicValue", settings.getValueByKey("magicValue"));

        if (exchangeRate != null && !exchangeRate.equals(settings.getValueByKey("exchangeRate"))) {
            productListManagementDAO.updateSettings("exchangeRate", exchangeRate);
            settings.setValueByKey("exchangeRate", exchangeRate);
        }
        model.addAttribute("exchangeRate", settings.getValueByKey("exchangeRate"));

        model.addAttribute("allPrestigeProducts", productListManagementDAO.showAllPrestigeProducts());
        return "prestige/showAllPrestigeProducts"; // return /prestige/showAllPrestigeProducts.html
    }
}
