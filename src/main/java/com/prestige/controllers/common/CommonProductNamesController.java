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

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping({"/prestige/product-management/product-names",
                 "/sklad/product-management/product-names"})
public class CommonProductNamesController extends AbstractCommonBaseController {

    private final CommonProductNamesDAO commonProductNamesDAO;

    private final CommonProductsDAO commonProductsDAO;

    @Autowired
    public CommonProductNamesController(CommonProductNamesDAO commonProductNamesDAO,
                                        CommonProductsDAO commonProductsDAO) {
        this.commonProductNamesDAO = commonProductNamesDAO;
        this.commonProductsDAO = commonProductsDAO;
    }

    @GetMapping
    public String showAllProductNames(Model model, HttpServletRequest request) {
        model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/product-names/product-names";
    }

    @GetMapping("/add")
    public String addNewProductNameGet(Model model, HttpServletRequest request) {

        if (model.getAttribute("productName") == null) {
            model.addAttribute("productName", new ProductName());
        }
        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/product-names/add-new-product-name";
    }

    @GetMapping("/{productNameId}")
    public ModelAndView findProductNameById(@PathVariable("productNameId") Integer productNameId, Model model, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("common/product-names/edit-product-name");
        ProductName productName = commonProductNamesDAO.findProductNameById(productNameId);

        if (productName == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/product-names",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad"));
        }
        else {
            mav.addObject("productName", productName);
        }
        mav.addObject("URIBeginning", getURIBeginning(request));
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("productName") ProductName productName, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {

        switch (method) {
            case "put":
                return updateProductName(productName, bindingResult, redirectAttributes, request);
            case "delete":
                return deleteProductName(productName.getId(), redirectAttributes, request);
            default:
                return saveNewProductName(productName, bindingResult, redirectAttributes, request);
        }
    }

    private String saveNewProductName(ProductName productName, BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового названия товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        if (!commonProductNamesDAO.saveNewProductName(productName)) {
            String errorMessage = "Указанное Вами название товара уже существует.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("productName", productName);

            return String.format("redirect:/%s/product-management/product-names/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        return String.format("redirect:/%s/product-management/product-names",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private String updateProductName(ProductName productName, BindingResult bindingResult,
                                     RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании названия товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", productName.getId());
        }
        else if (productName.equals(commonProductNamesDAO.findProductNameById(productName.getId()))) {
            return String.format("redirect:/%s/product-management/product-names",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }
        else if (commonProductNamesDAO.checkIfProductNameExists(productName)) {
            String errorMessage = "Указанное Вами название товара уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", productName.getId());
        }
        commonProductNamesDAO.updateProductName(productName);

        return String.format("redirect:/%s/product-management/product-names",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private String deleteProductName(Integer productNameId, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        if (commonProductsDAO.checkIfProductNameExists(productNameId)) {
            String errorMessage = "Невозможно удалить это название продукта, т.к оно используется в номенклатуре товаров.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/product-names/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", productNameId);
        }

        commonProductNamesDAO.deleteProductNameById(productNameId);

        return String.format("redirect:/%s/product-management/product-names",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }
}
