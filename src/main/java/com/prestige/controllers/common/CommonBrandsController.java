package com.prestige.controllers.common;

import com.prestige.dao.common.CommonBrandNamesDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.models.Brand;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping({"/prestige/product-management/brands",
                 "/sklad/product-management/brands"})
public class CommonBrandsController {

    private final CommonBrandNamesDAO commonBrandNamesDAO;

    private final CommonProductsDAO commonProductsDAO;

    @GetMapping
    public String showAllBrands(Model model) {
        model.addAttribute("allBrands", commonBrandNamesDAO.findAllBrands());
        return "common/brands/brands";
    }

    @GetMapping("/add")
    public String addNewBrand(Model model) {

        if (model.getAttribute("brand") == null) {
            model.addAttribute("brand", new Brand());
        }
        return "common/brands/add-new-brand";
    }

    @GetMapping("/{brandId}")
    public ModelAndView findBrandById(@PathVariable("brandId") Integer brandId, Model model) {
        ModelAndView mav = new ModelAndView("common/brands/edit-brand");
        Brand brand = commonBrandNamesDAO.findBrandById(brandId);

        if (brand == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/product-management/brands",
                    model.getAttribute("URIBeginning")));
        }
        else {
            mav.addObject("brand", brand);
        }
        return mav;
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("brand") Brand brand, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes,
                                       Model model) {

        switch (method) {
            case "put":
                return updateBrand(brand, bindingResult, redirectAttributes, model);
            case "delete":
                return deleteBrand(brand.getId(), redirectAttributes, model);
            default:
                return saveNewBrand(brand, bindingResult, redirectAttributes, model);
        }
    }

    private String saveNewBrand(Brand brand, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового бренда. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/add", model.getAttribute("URIBeginning"));
        }

        if (!commonBrandNamesDAO.saveNewBrand(brand)) {
            String errorMessage = "Указанный Вами бренд уже существует.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("brand", brand);

            return String.format("redirect:/%s/product-management/brands/add", model.getAttribute("URIBeginning"));
        }

        return String.format("redirect:/%s/product-management/brands", model.getAttribute("URIBeginning"));
    }

    private String updateBrand(Brand brand, BindingResult bindingResult,
                               RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании бренда. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/%d", model.getAttribute("URIBeginning"));
        }
        else if (brand.equals(commonBrandNamesDAO.findBrandById(brand.getId()))) {
            return String.format("redirect:/%s/product-management/brands", model.getAttribute("URIBeginning"));
        }
        else if (commonBrandNamesDAO.checkIfBrandExists(brand)) {
            String errorMessage = "Указанный Вами бренд уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/%d", model.getAttribute("URIBeginning"));
        }
        commonBrandNamesDAO.updateBrand(brand);

        return String.format("redirect:/%s/product-management/brands", model.getAttribute("URIBeginning"));
    }

    private String deleteBrand(Integer brandId, RedirectAttributes redirectAttributes, Model model) {
        if (commonProductsDAO.checkIfBrandExists(brandId)) {
            String errorMessage = "Невозможно удалить этот бренд, т.к он используется в номенклатуре товаров.";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/product-management/brands/%d", model.getAttribute("URIBeginning"));
        }

        commonBrandNamesDAO.deleteBrandById(brandId);

        return String.format("redirect:/%s/product-management/brands", model.getAttribute("URIBeginning"));
    }
}
