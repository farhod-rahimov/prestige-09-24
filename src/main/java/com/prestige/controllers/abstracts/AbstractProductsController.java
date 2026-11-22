package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import com.prestige.dao.abstracts.AbstractAllSuppliesDAO;
import com.prestige.dao.abstracts.AbstractSettingsDAO;
import com.prestige.models.RetailProductView;
import com.prestige.models.filters.ProductStockFilter;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@AllArgsConstructor
public abstract class AbstractProductsController {

    protected final AbstractAllSuppliesDAO allSuppliesDAO;

    protected final AbstractAllSalesDAO allSalesDAO;

    protected final AbstractSettingsDAO settingsDAO;

    protected final String uriPrefix;

    private String getModelFilterName() {
        return "productStockFilter";
    }

    @GetMapping
    public String showAllProductsInStock(Model model) {
        model.addAttribute("allProductsInStock", allSuppliesDAO.findAllProductsInStock());

        if (model.getAttribute(getModelFilterName()) == null) {
            model.addAttribute(getModelFilterName(), new ProductStockFilter());
        }

        if (uriPrefix.contains("prestige")) {
            model.addAttribute("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
        }

        return String.format("%s/products/products", uriPrefix);
    }

    protected String postMethodDispatcherInternal(ProductStockFilter productStockFilter, String method, BindingResult bindingResult,
                                                  RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при применении фильтра. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/products/products", uriPrefix);
        }

        switch (method) {
            case "applyFilter":
                return applyFilter(productStockFilter, model, redirectAttributes, false);
            case "applyFilterInList":
                return applyFilter(productStockFilter, model, redirectAttributes, true);
            case "resetFilter":
                return resetFilter(productStockFilter);
            default:
                throw new RuntimeException(String.format("POST method is undefined: '%s'", method));
        }
    }

    private String applyFilter(ProductStockFilter productStockFilter, Model model, RedirectAttributes redirectAttributes, boolean showInList) {
        ProductStockFilter productStockFilterCopy = new ProductStockFilter(productStockFilter);
        productStockFilterCopy.prepareAllFields();

        List<RetailProductView> filteredProducts = allSuppliesDAO.findAllProductsInStockWIthFilter(productStockFilterCopy);
        Integer entryProductId = null;

        if (showInList) {
            model.addAttribute("allProductsInStock", allSuppliesDAO.findAllProductsInStock());

            if (productStockFilterCopy.isNotEmpty() && !filteredProducts.isEmpty()) {
                entryProductId = filteredProducts.get(0).getProduct().getId();
            }
        }
        else {
            model.addAttribute("allProductsInStock", filteredProducts);
        }

        if (uriPrefix.contains("prestige")) {
            model.addAttribute("magicValue", settingsDAO.findSettingsByName("magicValue").getValue());
            model.addAttribute("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
        }

        if (entryProductId != null) {
            redirectAttributes.addFlashAttribute(getModelFilterName(), productStockFilter);
            return String.format("redirect:/%s/products#product-%d", uriPrefix, entryProductId);
        }
        return String.format("%s/products/products", uriPrefix);
    }

    private String resetFilter(ProductStockFilter productStockFilter) {
        productStockFilter.clear();

        return String.format("redirect:/%s/products", uriPrefix);
    }

    @GetMapping("/{productId}/supplies")
    public String findAllSuppliesByProductId(@PathVariable("productId") Integer productId, Model model) {
        model.addAttribute("productSupplyList", allSuppliesDAO.findProductSupplyListByProductId(productId));

        if (uriPrefix.contains("prestige")) {
            model.addAttribute("magicValue", settingsDAO.findSettingsByName("magicValue").getValue());
            model.addAttribute("exchangeRate", settingsDAO.findSettingsByName("exchangeRate").getValue());
        }
        return String.format("%s/products/supply-list", uriPrefix);
    }
}
