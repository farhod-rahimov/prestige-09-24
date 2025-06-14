package com.prestige.controllers.common;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
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

@Controller
@RequestMapping({"/prestige/product-management/products",
                 "/sklad/product-management/products"})
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

        if (model.getAttribute("product") == null) {
            model.addAttribute("product", new Product());
        }
        model.addAttribute("allProductNames", commonProductNamesDAO.findAllProductNames());
        model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
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
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {

        switch (method) {
            case "put":
                return updateProduct(product, bindingResult, redirectAttributes, request);
            case "delete":
                return deleteProduct(product.getId(), redirectAttributes, request);
            default:
                return saveNewProduct(product, bindingResult, redirectAttributes, request);
        }
    }

    private String saveNewProduct(Product product, BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового товара. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/products/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        if (!commonProductsDAO.saveNewProduct(product)) {
            String errorMessage = "Указанный Вами товар уже существует.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("product", product);

            return String.format("redirect:/%s/product-management/products/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        return String.format("redirect:/%s/product-management/products",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
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
