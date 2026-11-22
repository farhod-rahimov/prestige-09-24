package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractProductsMarginDAO;
import com.prestige.dao.common.CommonProductsDAO;
import com.prestige.forms.ProductMarginForm;
import com.prestige.models.ProductWithMargin;
import com.prestige.models.filters.ProductMarginFilter;
import lombok.AllArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public abstract class AbstractProductMarginController {

    private final AbstractProductsMarginDAO productsMarginDAO;

    private final CommonProductsDAO commonProductsDAO;

    private final String tableName;

    private String getUriPrefix(Model model) {
        return (String)model.getAttribute("URIBeginning");
    }

    @GetMapping
    public String showAllProducts(Model model) {
        return showAllProductsInternal(model, null);
    }

    @PostMapping()
    public String postMethodDispatcher(@RequestParam("method") String method,
                                       @ModelAttribute("marginForm") ProductMarginForm marginForm,
                                       RedirectAttributes redirectAttributes, BindingResult bindingResult,
                                       Model model, HttpSession session) {

        if (bindingResult.hasErrors()) {
            String errorMessage = null;

            switch (method) {
                case "applyFilter":
                    errorMessage = "Возникла ошибка при применении фильтра. Пожалуйста, попробуйте заново."; break;
                default:
                    errorMessage = "Возникла ошибка при установлении наценки. Пожалуйста, попробуйте заново."; break;
            }
            redirectAttributes.addFlashAttribute("error", errorMessage);
            model.addAttribute("productMarginFilter", new ProductMarginFilter());

            return String.format("redirect:/%s/product-management/products-margin/", getUriPrefix(model));
        }

        switch (method) {
            case "applyFilter":
                return applyFilter(marginForm.getMarginFilter(), model);
            case "resetFilter":
                return resetFilter(marginForm.getMarginFilter(), model);
            case "saveAndClose":
                return save(marginForm, model, true);
            case "save":
                return save(marginForm, model, false);
            case "close":
                return String.format("redirect:/%s/product-management", getUriPrefix(model));
            default:
                throw new RuntimeException("Ошибка. Метод '" + method + "' не существует.");
        }
    }

    private String showAllProductsInternal(Model model, ProductMarginFilter filter) {
        List<ProductWithMargin> allProducts;

        if (filter != null) {
            ProductMarginFilter filterCopy = new ProductMarginFilter(filter);
            filterCopy.prepareAllFields();
            allProducts = (List<ProductWithMargin>)commonProductsDAO.findAllProductsWIthMarginFilter(filterCopy, tableName);
        }
        else {
            allProducts = (List<ProductWithMargin>)commonProductsDAO.findAllProductsWIthMarginFilter(null, tableName);
        }

        Map<Integer, BigDecimal> marginMap = allProducts.stream().collect(
                HashMap::new,(m, p) -> m.put(p.getId(), p.getMargin()),HashMap::putAll);

        ProductMarginForm marginForm = new ProductMarginForm(marginMap, filter == null ? new ProductMarginFilter() : filter);

        model.addAttribute("allProducts", allProducts);
        model.addAttribute("marginForm", marginForm);

        return "common/products-margin/products-margin";
    }

    private String applyFilter(ProductMarginFilter productMarginFilter, Model model) {
        return showAllProductsInternal(model, productMarginFilter);
    }

    private String resetFilter(ProductMarginFilter productMarginFilter, Model model) {
        productMarginFilter.clear();

        return String.format("redirect:/%s/product-management/products-margin", getUriPrefix(model));
    }

    private String save(ProductMarginForm marginForm, Model model, boolean shouldClose) {
        productsMarginDAO.upsertBatch(marginForm.getMarginMap());

        if (shouldClose) {
            return String.format("redirect:/%s/product-management", getUriPrefix(model));
        }
        return String.format("redirect:/%s/product-management/products-margin", getUriPrefix(model));
    }
}
