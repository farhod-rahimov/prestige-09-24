package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.abstracts.AbstractSettingsDAO;
import com.prestige.models.RetailProduct;
import com.prestige.models.filters.ProductFilter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@AllArgsConstructor
public abstract class AbstractProductsController {

    protected final AbstractAllSuppliesDAO allSuppliesDAO;

    protected final AbstractAllSalesDAO allSalesDAO;

    protected final AbstractSettingsDAO settingsDAO;

    protected final String uriPrefix;

    private String getModelFilterName() {
        return uriPrefix.contains("prestige") ? "prestigeProductFilter" : "skladProductFilter";
    }

    @GetMapping
    public String showAllProductsInStock(Model model) {
        model.addAttribute("allProductsInStock", allSuppliesDAO.findAllProductsInStock());
        model.addAttribute(getModelFilterName(), new ProductFilter());

        if (uriPrefix.contains("prestige")) {
            model.addAttribute("magicValue", settingsDAO.findSettingsByName("magicValue").getValue());
            model.addAttribute("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
        }

        return String.format("%s/products/products", uriPrefix);
    }

    protected String postMethodDispatcherInternal(ProductFilter productFilter, String method, BindingResult bindingResult,
                                                  RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при применении фильтра. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/products/products", uriPrefix);
        }

        switch (method) {
            case "applyFilter":
                return applyFilter(productFilter, model);
            case "resetFilter":
                return resetFilter(productFilter);
            default:
                throw new RuntimeException(String.format("POST method is undefined: '%s'", method));
        }
    }

    private String applyFilter(ProductFilter productFilter, Model model) {
        ProductFilter productFilterCopy = new ProductFilter(productFilter);
        productFilterCopy.prepareAllFields();

        model.addAttribute("allProductsInStock", allSuppliesDAO.findAllProductsInStockWIthFilter(productFilterCopy));

        if (uriPrefix.contains("prestige")) {
            model.addAttribute("magicValue", settingsDAO.findSettingsByName("magicValue").getValue());
            model.addAttribute("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
        }

        return String.format("%s/products/products", uriPrefix);
    }

    private String resetFilter(ProductFilter productFilter) {
        productFilter.clear();

        return String.format("redirect:/%s/products", uriPrefix);
    }

    @GetMapping("/{productId}/supplies")
    public ModelAndView findAllSuppliesByProductId(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView(String.format("%s/products/supply-list", uriPrefix));

        List<RetailProduct> productSupplyList = allSuppliesDAO.findProductSupplyListByProductId(productId);

        if (productSupplyList.isEmpty()) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/products", uriPrefix));
        }
        else {
            mav.addObject("productSupplyList", productSupplyList);

            if (uriPrefix.contains("prestige")) {
                mav.addObject("magicValue", settingsDAO.findSettingsByName("magicValue").getValue());
                mav.addObject("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
            }
        }
        return mav;
    }
}
