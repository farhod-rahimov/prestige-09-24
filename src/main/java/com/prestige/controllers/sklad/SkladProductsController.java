package com.prestige.controllers.sklad;

import com.prestige.dao.sklad.SkladAllSuppliesDAO;
import com.prestige.dao.sklad.SkladSettingsDAO;
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
@RequestMapping("/sklad/products")
@SessionAttributes({"skladProductFilter"})
public class SkladProductsController {

    private final SkladAllSuppliesDAO skladAllSuppliesDAO;
    private final SkladSettingsDAO skladSettingsDAO;

    @Autowired
    public SkladProductsController(SkladAllSuppliesDAO skladAllSuppliesDAO, SkladSettingsDAO skladSettingsDAO) {
        this.skladAllSuppliesDAO = skladAllSuppliesDAO;
        this.skladSettingsDAO = skladSettingsDAO;
    }

    @GetMapping
    public String showAllSkladProductsInStock(Model model) {
        model.addAttribute("allProductsInStock", skladAllSuppliesDAO.findAllSkladProductsInStock());
        model.addAttribute("skladProductFilter", new ProductFilter());

        return "sklad/products/products";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("skladProductFilter") ProductFilter skladProductFilter,
                                       @RequestParam("method") String method, BindingResult bindingResult,
                                       RedirectAttributes redirectAttributes, Model model) {

        switch (method) {
            case "applyFilter":
                return applyFilter(skladProductFilter, model);
            case "resetFilter":
                return resetFilter(skladProductFilter);
            default:
                throw new RuntimeException(String.format("POST method is undefined: '%s'", method));
        }
    }

    private String applyFilter(ProductFilter skladProductFilter, Model model) {
        ProductFilter productFilterCopy = new ProductFilter(skladProductFilter);
        productFilterCopy.prepareAllFields();

        model.addAttribute("allProductsInStock", skladAllSuppliesDAO.findAllSkladProductsInStockWIthFilter(productFilterCopy));

        return "sklad/products/products";
    }

    private String resetFilter(ProductFilter skladProductFilter) {
        skladProductFilter.clear();
        return String.format("redirect:/sklad/products");
    }

    @GetMapping("/{productId}/supplies")
    public ModelAndView findAllSuppliesByProductId(@PathVariable("productId") Integer productId, Model model) {
        ModelAndView mav = new ModelAndView("sklad/products/supply-list");

        List<RetailProduct> skladProductSupplyList = skladAllSuppliesDAO.findSkladProductSupplyListByProductId(productId);

        if (skladProductSupplyList.isEmpty()) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/sklad/products");
        }
        else {
            mav.addObject("skladProductSupplyList", skladProductSupplyList);
        }
        return mav;
    }
}
