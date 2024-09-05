package com.prestige.controllers.prestige;

import com.prestige.dao.PrestigeSalesDAO;
import com.prestige.dao.ProductListManagementDAO;
import com.prestige.models.Client;
import com.prestige.models.Store;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
            return addNewClientGet(model);
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
}
