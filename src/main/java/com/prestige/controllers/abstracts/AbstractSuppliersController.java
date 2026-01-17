package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractSuppliersDAO;
import com.prestige.dao.abstracts.AbstractSupplyBasketsDAO;
import com.prestige.models.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class AbstractSuppliersController {

    private final AbstractSuppliersDAO suppliersDAO;

    private final AbstractSupplyBasketsDAO supplyBasketsDAO;

    protected final String uriPrefix;

    public AbstractSuppliersController(AbstractSuppliersDAO suppliersDAO, AbstractSupplyBasketsDAO supplyBasketsDAO, String uriPrefix) {
        this.suppliersDAO = suppliersDAO;
        this.supplyBasketsDAO = supplyBasketsDAO;
        this.uriPrefix = uriPrefix;
    }

    public String showAllSuppliers(Model model) {
        model.addAttribute("allSuppliers", suppliersDAO.findAllSuppliers());
        return "shared/suppliers/suppliers";
    }

    @GetMapping("/add")
    public String addNewSupplierGet(Model model) {

        if (model.getAttribute("supplier") == null) {
            model.addAttribute("supplier", new Supplier());
        }
        return "shared/suppliers/add-new-supplier";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("supplier") Supplier supplier, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes) {

        switch (method) {
            case "put":
                return updateSupplier(supplier, bindingResult, redirectAttributes);
            case "delete":
                return deleteSupplier(supplier.getId(), redirectAttributes);
            default:
                return saveNewSupplier(supplier, bindingResult, redirectAttributes);
        }
    }

    private String saveNewSupplier(Supplier supplier, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового поставщика. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/suppliers/add", uriPrefix);
        }

        if (!suppliersDAO.saveNewSupplier(supplier)) {
            String errorMessage = "Указанный Вами поставщик уже существует. Убедитесь, что название магазина уникально.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("supplier", supplier);
            return String.format("redirect:/%s/suppliers/add", uriPrefix);
        }
        return String.format("redirect:/%s/suppliers", uriPrefix);
    }

    @GetMapping("/{supplierId}")
    public ModelAndView findSupplierById(@PathVariable("supplierId") Integer supplierId, Model model) {
        ModelAndView mav = new ModelAndView("shared/suppliers/edit-supplier");
        Supplier supplier = suppliersDAO.findSupplierById(supplierId);

        if (supplier == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/suppliers", uriPrefix));
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
            return String.format("redirect:/%s/suppliers/", uriPrefix);
        }
        else if (supplier.equals(suppliersDAO.findSupplierById(supplier.getId()))) {
            return String.format("redirect:/%s/suppliers/", uriPrefix);
        }
        else if (suppliersDAO.checkIfSupplierExists(supplier)) {
            String errorMessage = "Указанный Вами поставщик уже существует. Убедитесь, что название магазина уникально.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/suppliers/%d", uriPrefix, supplier.getId());
        }
        suppliersDAO.updateSupplier(supplier);
        return String.format("redirect:/%s/suppliers", uriPrefix);
    }

    private String deleteSupplier(Integer supplierId, RedirectAttributes redirectAttributes) {

        if(checkIfSupplierIsInUse(supplierId)) {
            String errorMessage = "Невозможно удалить этого поставщика, т.к у него есть корзины в поставках";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/suppliers/%d", uriPrefix, supplierId);
        }

        suppliersDAO.deleteSupplierById(supplierId);
        return String.format("redirect:/%s/suppliers", uriPrefix);
    }

    private Boolean checkIfSupplierIsInUse(Integer supplierId) {
        return supplyBasketsDAO.checkIfSupplierIdExists(supplierId);
    }
}
