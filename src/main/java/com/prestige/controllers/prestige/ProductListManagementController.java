package com.prestige.controllers.prestige;

import com.prestige.dao.ProductListManagementDAO;
import com.prestige.dao.Utils;
import com.prestige.models.*;
import com.prestige.models.searchFilters.RetailProductSearchFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Controller
@RequestMapping("/prestige/productListManagement")
@SessionAttributes({"newPrestigeProduct", "newBasket"}) // Указываем, что объекты  нужно сохранять в сессии
public class ProductListManagementController {

    private final ProductListManagementDAO productListManagementDAO;

    private final Settings settings;

    private final Utils utils;

    @Autowired
    public ProductListManagementController(ProductListManagementDAO productListManagementDAO, Settings settings,
                                           Utils utils) {
        this.productListManagementDAO = productListManagementDAO;
        this.settings = settings;
        this.utils = utils;
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

            newProduct.setId(productListManagementDAO.getCommonProductId(newProduct.getBrand().getId(),
                    newProduct.getProductName().getId(), newProduct.getCarModel(), newProduct.getArticleNumber()));

            if (newProduct.getNotes() == null) {
                newProduct.setNotes(productListManagementDAO.getCommonProductNotesById(newProduct.getId()));
            }
        }
    }

    @PostMapping("/addNewProduct")
    public String addNewProductPost(@ModelAttribute("newProduct") Product newProduct,
                                        BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO: показать ошибку на странице
            return addNewProductGet(model);
        }

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

        // заменяем объект newPrestigeProduct в Модели и Сессии, т.к там может быть старый объект
        model.addAttribute("newPrestigeProduct", new RetailProduct());
        session.setAttribute("newPrestigeProduct", model.getAttribute("newPrestigeProduct"));

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

    @GetMapping("/addNewPrestigeBasket")
    public String addNewPrestigeBasketGet(Model model, HttpSession session) {

        Basket newBasket = new Basket();
        RetailSaleUnit newPrestigeSaleUnit = new RetailSaleUnit();
        newBasket.getRetailSaleUnitList().add(newPrestigeSaleUnit);

        // заменяем объекты в Модели и Сессии, т.к там могут быть старые объекты
        session.setAttribute("newBasket", newBasket);
        model.addAttribute("newBasket", newBasket);
        model.addAttribute("newPrestigeSaleUnit", newPrestigeSaleUnit);
        model.addAttribute("allClientNames", productListManagementDAO.getAllClientNames());

        return "prestige/addNewPrestigeBasket"; // return /prestige/addNewPrestigeBasket.html
    }

    private void completeBasket(Basket newPrestigeBasket, Model model) {
        Client client = newPrestigeBasket.getClient();

        if (client == null) {
            return;
        }

        if (client.getName() != null && client.getPhoneNumber() == null) {
            model.addAttribute("allClientPhoneNumbers", productListManagementDAO.getAllClientPhoneNumbersByNames(client.getName()));
        }
        else if (client.getName() != null && client.getPhoneNumber() != null) {
            client.setId(productListManagementDAO.getClientIdByNameAndPhoneNumber(client.getName(), client.getPhoneNumber()));
            model.addAttribute("allProductsNames", productListManagementDAO.showAllProductNames());
        }
    }

    private void setRetailSaleUnitStockBalance(RetailSaleUnit newPrestigeSaleUnit) {
        newPrestigeSaleUnit.getRetailProduct().setStockBalance(productListManagementDAO.getTotalStockBalanceByProductId(
                    newPrestigeSaleUnit.getRetailProduct().getProduct().getId()));
    }

    private void updateRetailSaleUnit(RetailSaleUnit newPrestigeSaleUnit) {
        setRetailSaleUnitStockBalance(newPrestigeSaleUnit);
    }

    private boolean isSaleUnitAlreadyInTheBasket(List<RetailSaleUnit> newPrestigeSaleUnitList, RetailSaleUnit newPrestigeSaleUnit) {
        int dublicates = 0;

        for (int i = 0; i < newPrestigeSaleUnitList.size(); i++) {

            if (newPrestigeSaleUnitList.get(i).getRetailProduct().getProduct().getId() == newPrestigeSaleUnit.getRetailProduct().getProduct().getId()) {
                dublicates++;
            }
        }
        return dublicates > 1 ? true : false;
    }

    private RetailSaleUnit saveNewPrestigeSaleUnit(Basket newBasket, Model model, HttpSession session, String action) {
        List<RetailSaleUnit> newPrestigeSaleUnitList = newBasket.getRetailSaleUnitList();
        RetailSaleUnit newPrestigeSaleUnit = newPrestigeSaleUnitList.get(newPrestigeSaleUnitList.size() - 1);

        boolean isSaleUnitAlreadyInTheBasket = isSaleUnitAlreadyInTheBasket(newPrestigeSaleUnitList, newPrestigeSaleUnit);

        if (action.equals("save") || (isSaleUnitAlreadyInTheBasket && newPrestigeSaleUnit.getIsSaved() == false)) {
            newBasket.shouldCreateNewPrestigeSaleUnit = true;

            if (isSaleUnitAlreadyInTheBasket && newPrestigeSaleUnit.getIsSaved() == false) {
                Product product = newPrestigeSaleUnit.getRetailProduct().getProduct();
                String error = String.format("Товар |'%s' | '%s' | '%s' | '%s'| уже есть в корзине",
                        product.getProductName().getProductName(), product.getCarModel(), product.getArticleNumber(),
                        product.getBrand().getBrandName());

                model.addAttribute("error", error);

                newPrestigeSaleUnitList.remove(newPrestigeSaleUnitList.size() - 1);
                return createNewPrestigeSaleUnit(newPrestigeSaleUnitList, model, session);
            }
        }

        productListManagementDAO.saveNewPrestigeSaleUnit(newBasket, newPrestigeSaleUnit);
        updateRetailSaleUnit(newPrestigeSaleUnit);
        return createNewPrestigeSaleUnit(newPrestigeSaleUnitList, model, session);
    }

    private RetailSaleUnit createNewPrestigeSaleUnit(List<RetailSaleUnit> newPrestigeSaleUnitList, Model model, HttpSession session) {
        RetailSaleUnit newPrestigeSaleUnit = new RetailSaleUnit();
        newPrestigeSaleUnitList.add(newPrestigeSaleUnit);

        model.addAttribute("newPrestigeSaleUnit", newPrestigeSaleUnit);
        session.setAttribute("newPrestigeSaleUnit", model.getAttribute("newPrestigeSaleUnit"));

        model.addAttribute("allProductsNames", productListManagementDAO.showAllProductNames());

        return newPrestigeSaleUnit;
    }

    void setPricesForDesiredTotalSum(Basket newBasket, List<RetailSaleUnit> newPrestigeSaleUnitList, BigDecimal desiredTotalSum) {
        BigDecimal multiplier = desiredTotalSum.divide(newBasket.getTotalSum(), 10, RoundingMode.HALF_UP);

        for (int i = 0; i < newPrestigeSaleUnitList.size() - 1; i++) {
            RetailSaleUnit unit = newPrestigeSaleUnitList.get(i);
            unit.setSoldPriceSmn(unit.getSoldPriceSmn().multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
        }
    }

    void updateNewPrestigeSaleUnitList(Basket newBasket, List<RetailSaleUnit> newPrestigeSaleUnitList, BigDecimal desiredTotalSum) {

        Basket tmpBasket = new Basket();
        tmpBasket.setRetailSaleUnitList(newPrestigeSaleUnitList);
        tmpBasket.update(); // calculate hash

        boolean shouldUpdateList = false;

        if (tmpBasket.getTotalSumHash() == newBasket.getTotalSumHash() &&
                desiredTotalSum.equals(newBasket.getTotalSum()) == false) {

            // products, its prices and quantities are same. desiredTotalSum differs from basket`s totalSum
            setPricesForDesiredTotalSum(newBasket, newPrestigeSaleUnitList, desiredTotalSum);
            shouldUpdateList = true;
        }

        if ((tmpBasket.getTotalSumHash() != newBasket.getTotalSumHash()) || shouldUpdateList) {
            productListManagementDAO.updateNewPrestigeSaleUnitList(newBasket, newPrestigeSaleUnitList);

            for (int i = 0; i < newPrestigeSaleUnitList.size() - 1; i++) {
                updateRetailSaleUnit(newPrestigeSaleUnitList.get(i));
            }
        }
    }

    private void findNextStep(RetailSaleUnit newPrestigeSaleUnit, RetailProductSearchFilter productSearchFilter, Model model) {
        RetailProduct prestigeProduct = newPrestigeSaleUnit.getRetailProduct();

        if (prestigeProduct.getProduct().getProductName().getId() == null) {
            model.addAttribute("allProductsNames", productListManagementDAO.showAllProductNames(productSearchFilter));
        }
        else if (prestigeProduct.getProduct().getCarModel() == null) {
            model.addAttribute("allCarModels", productListManagementDAO.getCarModelsByProductNameId(
                    prestigeProduct.getProduct().getProductName().getId(), productSearchFilter));
        }
        else if (prestigeProduct.getProduct().getArticleNumber() == null) {
            model.addAttribute("allArticleNumbers", productListManagementDAO.getArticleNumbersByProductNameIdAndCarModel(
                    prestigeProduct.getProduct().getProductName().getId(), prestigeProduct.getProduct().getCarModel(),
                    productSearchFilter));
        }
        else if (prestigeProduct.getProduct().getBrand() == null || (prestigeProduct.getProduct().getBrand().getId() == null)) {
            model.addAttribute("allBrands", productListManagementDAO.getBrandsByProductNameIdAndCarModelAndArticleNumber(
                    prestigeProduct.getProduct().getProductName().getId(), prestigeProduct.getProduct().getCarModel(),
                    prestigeProduct.getProduct().getArticleNumber(), productSearchFilter));
        }
        else if (prestigeProduct.getPriceDollar() == null) {
            newPrestigeSaleUnit.getRetailProduct().setPriceDollar(productListManagementDAO.getWACCPriceByProductId(
                    newPrestigeSaleUnit.getRetailProduct().getProduct().getId()));

            // set default soldPriceSmn
            BigDecimal magicValue = new BigDecimal(settings.getValueByKey("magicValue"));
            BigDecimal exchangeRate = new BigDecimal(settings.getValueByKey("exchangeRate"));
            BigDecimal soldPriceSmn = newPrestigeSaleUnit.getRetailProduct().getConvertedPriceToSmn(magicValue, exchangeRate);
            newPrestigeSaleUnit.setSoldPriceSmn(soldPriceSmn);

            // set stockBalance
            setRetailSaleUnitStockBalance(newPrestigeSaleUnit);
        }
        else if (newPrestigeSaleUnit.getQuantity() != null && newPrestigeSaleUnit.getTotalSumSmn() == null) {
            // set updated stockBalance
            newPrestigeSaleUnit.getRetailProduct().setStockBalance(newPrestigeSaleUnit.getRetailProduct().getStockBalance() -
                    newPrestigeSaleUnit.getQuantity());
        }
    }

    @GetMapping("/changeBasketNewItemSelectType")
    public String changeBasketNewItemSelectType(@ModelAttribute("newBasket") Basket newBasket,
                                                @RequestParam("selectType") String selectType,
                                                HttpSession session,  Model model) {

        newBasket.setNewItemSelectType(BasketSelectType.valueOf(selectType));

        return addNewPrestigeBasketPost(newBasket, null, "next", session, model, null);
    }

    @PostMapping("/addNewPrestigeBasket")
    public String addNewPrestigeBasketPost(@ModelAttribute("newBasket") Basket newBasket, BindingResult bindingResult,
                                           @RequestParam("action") String action, HttpSession session,  Model model,
                                           @RequestParam(name = "desiredTotalSum", required = false) BigDecimal desiredTotalSum) {

        List<RetailSaleUnit> newPrestigeSaleUnitList = newBasket.getRetailSaleUnitList();
        RetailSaleUnit newPrestigeSaleUnit = newPrestigeSaleUnitList.get(newPrestigeSaleUnitList.size() - 1);
        newBasket.shouldCreateNewPrestigeSaleUnit = false; // reset every time

        completeNewProduct(newPrestigeSaleUnit.getRetailProduct().getProduct());

        if (!newBasket.isClientSelectionDone()) {
            completeBasket(newBasket, model);
        }
        else if (action.equals("reset")) {
            newPrestigeSaleUnitList.remove(newPrestigeSaleUnitList.size() - 1);
            newPrestigeSaleUnit = createNewPrestigeSaleUnit(newPrestigeSaleUnitList, model, session);
            newBasket.resetProductSearchFilter();
        }
        else if (action.equals("save") || (action.equals("add") && newPrestigeSaleUnit.getTotalSumSmn() != null)) {
            newPrestigeSaleUnit = saveNewPrestigeSaleUnit(newBasket, model, session, action);
        }
        else if (action.equals("update")) {
            updateNewPrestigeSaleUnitList(newBasket, newPrestigeSaleUnitList, desiredTotalSum);
            newBasket.shouldCreateNewPrestigeSaleUnit = true;
        }
        else {
            findNextStep(newPrestigeSaleUnit, newBasket.getProductSearchFilter(), model);
        }

        model.addAttribute("magicValue", settings.getValueByKey("magicValue"));
        model.addAttribute("exchangeRate", settings.getValueByKey("exchangeRate"));
        model.addAttribute("newPrestigeSaleUnit", newPrestigeSaleUnit);

        newBasket.update();

        return "prestige/addNewPrestigeBasket"; // return /prestige/addNewPrestigeBasket.html
    }

    @GetMapping("/deletePrestigeSaleUnit")
    public String deletePrestigeSaleUnitGet(@RequestParam("removeIndex") int removeIndex, Model model) {

        Basket newBasket = (Basket)model.getAttribute("newBasket");
        List<RetailSaleUnit> newPrestigeSaleUnitList = newBasket.getRetailSaleUnitList();
        RetailSaleUnit newPrestigeSaleUnit = newPrestigeSaleUnitList.get(newPrestigeSaleUnitList.size() - 1);

        if (removeIndex > newPrestigeSaleUnitList.size() - 1) {
            String error = String.format("Неверный порядковый номер. Товара №%d нет в текущей корзине." +
                    "Пожалуйста, нажмите 'Обновить и сохранить корзину'", removeIndex + 1);
            model.addAttribute("error", error);
        }
        else if (removeIndex == newPrestigeSaleUnitList.size() - 1) {
            newPrestigeSaleUnitList.remove(removeIndex);
            newPrestigeSaleUnitList.add(new RetailSaleUnit());
            newBasket.resetNewItemSelectType();
            newBasket.resetProductSearchFilter();
            newBasket.shouldCreateNewPrestigeSaleUnit = true;
        }
        else {
            productListManagementDAO.removePrestigeSaleUnitFromBasket(newPrestigeSaleUnitList.get(removeIndex), newBasket);
            newPrestigeSaleUnitList.remove(removeIndex);
            findNextStep(newPrestigeSaleUnit, newBasket.getProductSearchFilter(), model);
        }

        model.addAttribute("magicValue", settings.getValueByKey("magicValue"));
        model.addAttribute("exchangeRate", settings.getValueByKey("exchangeRate"));
        model.addAttribute("newPrestigeSaleUnit", newPrestigeSaleUnit);

        newBasket.update();

        return "prestige/addNewPrestigeBasket"; // return /prestige/addNewPrestigeBasket.html
    }

    @GetMapping("/showAllPrestigeBaskets")
    public String showAllPrestigeBaskets(Model model) {
//        model.addAttribute("showProductNameSelectionSection", true);
//        model.addAttribute("allProducts", productListManagementDAO.showAllProducts());
        return "prestige/showAllPrestigeBaskets"; // return /prestige/showAllPrestigeBaskets.html
    }
}
