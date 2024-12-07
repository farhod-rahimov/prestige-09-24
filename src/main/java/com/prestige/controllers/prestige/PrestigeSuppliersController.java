package com.prestige.controllers.prestige;

import com.prestige.dao.prestige.PrestigeSuppliersDAO;
import com.prestige.models.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prestige/suppliers")
public class PrestigeSuppliersController {

    private final PrestigeSuppliersDAO prestigeSuppliersDAO;

    @Autowired
    public PrestigeSuppliersController(PrestigeSuppliersDAO prestigeSuppliersDAO) {
        this.prestigeSuppliersDAO = prestigeSuppliersDAO;
    }

    @GetMapping
    public String showAllSuppliers(Model model) {
        model.addAttribute("allSuppliers", prestigeSuppliersDAO.findAllSuppliers());
        return "prestige/suppliers/suppliers";
    }

    @GetMapping("/add")
    public String addNewSupplierGet(Model model) {

        if (model.getAttribute("supplier") == null) {
            model.addAttribute("supplier", new Supplier());
        }
        return "prestige/suppliers/add-new-supplier";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("supplier") Supplier supplier, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes) {

        switch (method) {
            case "put":
                return updateSupplier(supplier, bindingResult, redirectAttributes);
            case "delete":
                return deleteSupplier(supplier.getId());
            default:
                return saveNewSupplier(supplier, bindingResult, redirectAttributes);
        }
    }

    private String saveNewSupplier(Supplier supplier, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового поставщика. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/suppliers/add";
        }

        if (!prestigeSuppliersDAO.saveNewSupplier(supplier)) {
            String errorMessage = "Указанный Вами поставщик уже существует. Убедитесь, что название магазина уникально.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("supplier", supplier);
            return "redirect:/prestige/suppliers/add";
        }
        return "redirect:/prestige/suppliers";
    }

    @GetMapping("/{supplierId}")
    public ModelAndView findSupplierById(@PathVariable("supplierId") Integer supplierId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/suppliers/edit-supplier");
        Supplier supplier = prestigeSuppliersDAO.findSupplierById(supplierId);

        if (supplier == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/suppliers");
        }
        else {
            mav.addObject("supplier", supplier);
        }
        return mav;
    }

    private String updateSupplier(Supplier supplier, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании поставщика. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/suppliers/";
        }
        else if (supplier.equals(prestigeSuppliersDAO.findSupplierById(supplier.getId()))) {
            return "redirect:/prestige/suppliers/";
        }
        else if (prestigeSuppliersDAO.checkIfSupplierExists(supplier)) {
            String errorMessage = "Указанный Вами поставщик уже существует. Убедитесь, что название магазина уникально.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/suppliers/" + supplier.getId();
        }
        prestigeSuppliersDAO.updateSupplier(supplier);
        return "redirect:/prestige/suppliers";
    }

    private String deleteSupplier(Integer supplierId) {
        prestigeSuppliersDAO.deleteSupplierById(supplierId);
        return "redirect:/prestige/suppliers";
    }
}
