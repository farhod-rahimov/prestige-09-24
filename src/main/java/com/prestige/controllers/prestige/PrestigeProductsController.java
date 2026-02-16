package com.prestige.controllers.prestige;

import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.models.RetailProduct;
import com.prestige.models.filters.ProductFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/prestige/products")
@SessionAttributes({"prestigeProductFilter"})
public class PrestigeProductsController {

    private final PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;
    private final PrestigeSettingsDAO prestigeSettingsDAO;

    @Autowired
    public PrestigeProductsController(PrestigeAllSuppliesDAO prestigeAllSuppliesDAO, PrestigeSettingsDAO prestigeSettingsDAO) {
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
        this.prestigeSettingsDAO = prestigeSettingsDAO;
    }

    @GetMapping
    public String showAllPrestigeProductsInStock(Model model) {
        model.addAttribute("allProductsInStock", prestigeAllSuppliesDAO.findAllPrestigeProductsInStock());
        model.addAttribute("magicValue", prestigeSettingsDAO.findSettingsByName("magicValue").getValue());
        model.addAttribute("exchangeRate", prestigeSettingsDAO.findSettingsByName("exchangeRate").getValue());
        model.addAttribute("prestigeProductFilter", new ProductFilter());

        return "prestige/products/products";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("prestigeProductFilter") ProductFilter prestigeProductFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        switch (method) {
            case "applyFilter":
                return applyFilter(prestigeProductFilter, model);
            case "resetFilter":
                return resetFilter(prestigeProductFilter);
            default:
                throw new RuntimeException(String.format("POST method is undefined: '%s'", method));
        }
    }

    private String applyFilter(ProductFilter prestigeProductFilter, Model model) {
        ProductFilter productFilterCopy = new ProductFilter(prestigeProductFilter);
        productFilterCopy.prepareAllFields();

        model.addAttribute("allProductsInStock", prestigeAllSuppliesDAO.findAllPrestigeProductsInStockWIthFilter(productFilterCopy));
        model.addAttribute("magicValue", prestigeSettingsDAO.findSettingsByName("magicValue").getValue());
        model.addAttribute("exchangeRate", prestigeSettingsDAO.findSettingsByName("exchangeRate").getValue());

        return "prestige/products/products";
    }

    private String resetFilter(ProductFilter prestigeProductFilter) {
        prestigeProductFilter.clear();
        return String.format("redirect:/prestige/products");
    }

    @GetMapping("/{productId}/supplies")
    public ModelAndView findAllSuppliesByProductId(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/products/supply-list");

        List<RetailProduct> prestigeProductSupplyList = prestigeAllSuppliesDAO.findPrestigeProductSupplyListByProductId(productId);

        if (prestigeProductSupplyList.isEmpty()) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/products");
        }
        else {
            mav.addObject("prestigeProductSupplyList", prestigeProductSupplyList);
            mav.addObject("magicValue", prestigeSettingsDAO.findSettingsByName("magicValue").getValue());
            mav.addObject("exchangeRate", prestigeSettingsDAO.findSettingsByName("exchangeRate").getValue());
        }
        return mav;
    }
}
