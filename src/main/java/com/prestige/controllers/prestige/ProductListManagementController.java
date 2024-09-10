package com.prestige.controllers.prestige;

import com.prestige.dao.PrestigeSalesDAO;
import com.prestige.dao.ProductListManagementDAO;
import com.prestige.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/prestige/productListManagement")
public class ProductListManagementController {

    private final ProductListManagementDAO productListManagementDAO;

    @Autowired
    public ProductListManagementController(ProductListManagementDAO productListManagementDAO) {
        this.productListManagementDAO = productListManagementDAO;
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
        newProduct.getProductName().setProductName(productListManagementDAO.getProductNameById(newProduct.getProductName().getId()));
        newProduct.getBrand().setBrandName(productListManagementDAO.getBrandNameById(newProduct.getBrand().getId()));
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
        model.addAttribute("allProducts", productListManagementDAO.showAllProducts());
        return "prestige/showAllProducts"; // return /prestige/showAllProducts.html
    }
}
