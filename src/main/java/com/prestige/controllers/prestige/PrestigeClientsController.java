package com.prestige.controllers.prestige;

import com.prestige.dao.prestige.PrestigeClientsDAO;
import com.prestige.models.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prestige/clients")
public class PrestigeClientsController {

    private final PrestigeClientsDAO prestigeClientsDAO;

    @Autowired
    public PrestigeClientsController(PrestigeClientsDAO prestigeClientsDAO) {
        this.prestigeClientsDAO = prestigeClientsDAO;
    }

    @GetMapping
    public String showAllClients(Model model) {
        model.addAttribute("allClients", prestigeClientsDAO.findAllClients());
        return "prestige/clients/clients";
    }

    @GetMapping("/add")
    public String addNewClientGet(Model model) {

        if (model.getAttribute("client") == null) {
            model.addAttribute("client", new Client());
        }
        return "prestige/clients/add-new-client";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("client") Client client, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes) {

        switch (method) {
            case "put":
                return updateClient(client, bindingResult, redirectAttributes);
            case "delete":
                return deleteClient(client.getId());
            default:
                return saveNewClient(client, bindingResult, redirectAttributes);
        }
    }

    private String saveNewClient(Client client, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового клиента. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/clients/add";
        }

        if (!prestigeClientsDAO.saveNewClient(client)) {
            String errorMessage = "Указанный Вами клиент уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("client", client);
            return "redirect:/prestige/clients/add";
        }
        return "redirect:/prestige/clients";
    }

    @GetMapping("/{clientId}")
    public ModelAndView findClientById(@PathVariable("clientId") Integer clientId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/clients/edit-client");
        Client client = prestigeClientsDAO.findClientById(clientId);

        if (client == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/clients");
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
            return "redirect:/prestige/clients/";
        }
        else if (client.equals(prestigeClientsDAO.findClientById(client.getId()))) {
            return "redirect:/prestige/clients/";
        }
        else if (prestigeClientsDAO.checkIfClientExists(client)) {
            String errorMessage = "Указанный Вами клиент уже существует.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/clients/" + client.getId();
        }
        prestigeClientsDAO.updateClient(client);
        return "redirect:/prestige/clients";
    }

    private String deleteClient(Integer clientId) {
        prestigeClientsDAO.deleteClientById(clientId);
        return "redirect:/prestige/clients";
    }
}
