package com.prestige.controllers.prestige;

import com.prestige.dao.prestige.PrestigeAllSuppliesDAO;
import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.models.RetailProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
@RequestMapping("/prestige/products")
public class PrestigeProductsController {

    private final PrestigeAllSuppliesDAO prestigeAllSuppliesDAO;
    private final PrestigeSettingsDAO prestigeSettingsDAO;

    @Autowired
    public PrestigeProductsController(PrestigeAllSuppliesDAO prestigeAllSuppliesDAO,
                                      PrestigeSettingsDAO prestigeSettingsDAO) {
        this.prestigeAllSuppliesDAO = prestigeAllSuppliesDAO;
        this.prestigeSettingsDAO = prestigeSettingsDAO;
    }

    @GetMapping
    public String showAllPrestigeProductsInStock(Model model) {
        model.addAttribute("allProductsInStock", prestigeAllSuppliesDAO.findAllPrestigeProductsInStock());
        model.addAttribute("magicValue", prestigeSettingsDAO.findSettingsByName("magicValue").getValue());
        model.addAttribute("exchangeRate", prestigeSettingsDAO.findSettingsByName("exchangeRate").getValue());

        return "prestige/products/products";
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
