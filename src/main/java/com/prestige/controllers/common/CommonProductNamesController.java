package com.prestige.controllers.common;

import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.models.ProductName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping({"/prestige/product-management/product-names",
                 "/sklad/product-management/product-names"})
public class CommonProductNamesController {

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonProductsDAO commonProductsDAO;

    @Autowired
    public CommonProductNamesController(CommonProductNamesDAO commonProductNamesDAO,
                                        CommonProductsDAO commonProductsDAO) {
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonProductsDAO = commonProductsDAO;
    }

    @GetMapping
    public String showAllProductNames(Model model) {
        model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
        return "common/product-names/product-names";
    }

    @GetMapping("/add")
    public String addNewProductNameGet(Model model) {

        if (model.getAttribute("productName") == null) {
            model.addAttribute("productName", new ProductName());
        }
        return "common/product-names/add-new-product-name";
    }

    @GetMapping("/{productNameId}")
    public ModelAndView findProductNameById(@PathVariable("productNameId") Integer productNameId, Model model) {
        ModelAndView mav = new ModelAndView("common/product-names/edit-product-name");
        ProductName productName = commonProductNamesDAO.findProductNameById(productNameId);

        if (productName == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/product-names",
                    model.getAttribute("URIBeginning")));
        }
        else {
            mav.addObject("productName", productName);
        }
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("productName") ProductName productName, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes, Model model) {

        switch (method) {
            case "put":
                return updateProductName(productName, bindingResult, redirectAttributes, model);
            case "delete":
                return deleteProductName(productName.getId(), redirectAttributes, model);
            default:
                return saveNewProductName(productName, bindingResult, redirectAttributes, model);
        }
    }

    private String saveNewProductName(ProductName productName, BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового названия товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/add", model.getAttribute("URIBeginning"));
        }

        if (!commonProductNamesDAO.saveNewProductName(productName)) {
            String errorMessage = "Указанное Вами название товара уже существует.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("productName", productName);

            return String.format("redirect:/%s/product-management/product-names/add", model.getAttribute("URIBeginning"));
        }

        return String.format("redirect:/%s/product-management/product-names", model.getAttribute("URIBeginning"));
    }

    private String updateProductName(ProductName productName, BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании названия товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d",model.getAttribute("URIBeginning"));
        }
        else if (productName.equals(commonProductNamesDAO.findProductNameById(productName.getId()))) {
            return String.format("redirect:/%s/product-management/product-names", model.getAttribute("URIBeginning"));
        }
        else if (commonProductNamesDAO.checkIfProductNameExists(productName)) {
            String errorMessage = "Указанное Вами название товара уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d", model.getAttribute("URIBeginning"));
        }
        commonProductNamesDAO.updateProductName(productName);

        return String.format("redirect:/%s/product-management/product-names", model.getAttribute("URIBeginning"));
    }

    private String deleteProductName(Integer productNameId, RedirectAttributes redirectAttributes, Model model) {
        if (commonProductsDAO.checkIfProductNameExists(productNameId)) {
            String errorMessage = "Невозможно удалить это название продукта, т.к оно используется в номенклатуре товаров.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d", model.getAttribute("URIBeginning"), productNameId);
        }

        commonProductNamesDAO.deleteProductNameById(productNameId);

        return String.format("redirect:/%s/product-management/product-names", model.getAttribute("URIBeginning"));
    }
}
