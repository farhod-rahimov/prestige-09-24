package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractClientsDAO;
import com.prestige.dao.abstracts.AbstractSalesBasketsDAO;
import com.prestige.models.Client;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@AllArgsConstructor
public abstract class AbstractClientsController {

    private final AbstractClientsDAO clientsDAO;

    private final AbstractSalesBasketsDAO salesBasketsDAO;

    protected final String uriPrefix;

    @GetMapping
    public String showAllClients(Model model) {
        model.addAttribute("allClients", clientsDAO.findAllClients());
        return "shared/clients/clients";
    }

    @GetMapping("/add")
    public String addNewClientGet(Model model) {

        if (model.getAttribute("client") == null) {
            model.addAttribute("client", new Client());
        }
        return "shared/clients/add-new-client";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("client") Client client, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes) {

        switch (method) {
            case "put":
                return updateClient(client, bindingResult, redirectAttributes);
            case "delete":
                return deleteClient(client.getId(), redirectAttributes);
            default:
                return saveNewClient(client, bindingResult, redirectAttributes);
        }
    }

    private String saveNewClient(Client client, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового клиента. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/clients/add", uriPrefix);
        }

        if (!clientsDAO.saveNewClient(client)) {
            String errorMessage = "Указанный Вами клиент уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("client", client);
            return String.format("redirect:/%s/clients/add", uriPrefix);
        }
        return String.format("redirect:/%s/clients", uriPrefix);
    }

    @GetMapping("/{clientId}")
    public ModelAndView findClientById(@PathVariable("clientId") Integer clientId, Model model) {
        ModelAndView mav = new ModelAndView("shared/clients/edit-client");
        Client client = clientsDAO.findClientById(clientId);

        if (client == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/clients", uriPrefix));
        }
        else {
            mav.addObject("client", client);
        }
        return mav;
    }

    private String updateClient(Client client, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании клиента. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/clients/", uriPrefix);
        }
        else if (client.equals(clientsDAO.findClientById(client.getId()))) {
            return String.format("redirect:/%s/clients/", uriPrefix);
        }
        else if (clientsDAO.checkIfClientExists(client)) {
            String errorMessage = "Указанный Вами клиент уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/clients/", uriPrefix, client.getId());
        }
        clientsDAO.updateClient(client);
        return String.format("redirect:/%s/clients/", uriPrefix);
    }

    private String deleteClient(Integer clientId, RedirectAttributes redirectAttributes) {

        if(checkIfClientIsInUse(clientId)) {
            String errorMessage = "Невозможно удалить этого клиента, т.к у него есть корзины в продажах";
            redirectAttributes.addFlashAttribute("error", errorMessage);

            return String.format("redirect:/%s/clients/%d", uriPrefix, clientId);

        }
        clientsDAO.deleteClientById(clientId);
        return String.format("redirect:/%s/clients/", uriPrefix);
    }

    private Boolean checkIfClientIsInUse(Integer clientId) {
        return salesBasketsDAO.checkIfClientIdExists(clientId);
    }
}
