package com.prestige.controllers.common;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.models.Brand;
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
@RequestMapping({"/prestige/product-management/brands",
                 "/sklad/product-management/brands"})
public class CommonBrandsController extends AbstractCommonBaseController {

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    @Autowired
    public CommonBrandsController(CommonBrandNamesDAO commonBrandNamesDAO) {
        this.commonBrandNamesDAO = commonBrandNamesDAO;
    }

    @GetMapping
    public String showAllBrands(Model model, HttpServletRequest request) {
        model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/brands/brands";
    }

    @GetMapping("/add")
    public String addNewBrand(Model model, HttpServletRequest request) {

        if (model.getAttribute("brand") == null) {
            model.addAttribute("brand", new Brand());
        }
        model.addAttribute("URIBeginning", getURIBeginning(request));
        return "common/brands/add-new-brand";
    }

    @GetMapping("/{brandId}")
    public ModelAndView findBrandById(@PathVariable("brandId") Integer brandId, Model model, HttpServletRequest request) {
        ModelAndView mav = new ModelAndView("common/brands/edit-brand");
        Brand brand = commonBrandNamesDAO.findBrandById(brandId);

        if (brand == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/brands",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad"));
        }
        else {
            mav.addObject("brand", brand);
        }
        mav.addObject("URIBeginning", getURIBeginning(request));
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("brand") Brand brand, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       HttpServletRequest request) {

        switch (method) {
            case "put":
                return updateBrand(brand, bindingResult, redirectAttributes, request);
            case "delete":
                return deleteBrand(brand.getId(), request);
            default:
                return saveNewBrand(brand, bindingResult, redirectAttributes, request);
        }
    }

    private String saveNewBrand(Brand brand, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового бренда. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        if (!commonBrandNamesDAO.saveNewBrand(brand)) {
            String errorMessage = "Указанный Вами бренд уже существует.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("brand", brand);

            return String.format("redirect:/%s/product-management/brands/add",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }

        return String.format("redirect:/%s/product-management/brands",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private String updateBrand(Brand brand, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании бренда. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", brand.getId());
        }
        else if (brand.equals(commonBrandNamesDAO.findBrandById(brand.getId()))) {
            return String.format("redirect:/%s/product-management/brands",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
        }
        else if (commonBrandNamesDAO.checkIfBrandExists(brand)) {
            String errorMessage = "Указанный Вами бренд уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/%d",
                    isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad", brand.getId());
        }
        commonBrandNamesDAO.updateBrand(brand);

        return String.format("redirect:/%s/product-management/brands",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }

    private String deleteBrand(Integer brandId, HttpServletRequest request) {
        commonBrandNamesDAO.deleteBrandById(brandId);

        return String.format("redirect:/%s/product-management/brands",
                isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad");
    }
}
