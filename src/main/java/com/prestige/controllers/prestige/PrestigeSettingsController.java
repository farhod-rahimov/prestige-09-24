package com.prestige.controllers.prestige;

import com.prestige.dao.prestige.PrestigeSettingsDAO;
import com.prestige.models.Settings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/prestige/settings")
public class PrestigeSettingsController {

    private final PrestigeSettingsDAO prestigeSettingsDAO;

    @Autowired
    public PrestigeSettingsController(PrestigeSettingsDAO prestigeSettingsDAO) {
        this.prestigeSettingsDAO = prestigeSettingsDAO;
    }

    private void initializeMustHaveSettings() {

        Settings magicValue = new Settings(null, "magicValue", "20");
        if (!prestigeSettingsDAO.checkIfSettingsExists(magicValue)) {
            prestigeSettingsDAO.saveNewSettings(magicValue);
        }

        Settings exchangeRate = new Settings(null, "exchangeRate", "11.00");
        if (!prestigeSettingsDAO.checkIfSettingsExists(exchangeRate)) {
            prestigeSettingsDAO.saveNewSettings(exchangeRate);
        }
    }

    @GetMapping
    public String showAllSettings(Model model) {
        initializeMustHaveSettings();

        model.addAttribute("allSettings", prestigeSettingsDAO.findAllSettings());
        return "prestige/settings/settings";
    }

    @GetMapping("/add")
    public String addNewSettingGet(Model model) {

        if (model.getAttribute("settings") == null) {
            model.addAttribute("settings", new Settings());
        }
        return "prestige/settings/add-new-settings";
    }

    @PostMapping()
    public String postMethodDispatcher(@ModelAttribute("settings") Settings settings, BindingResult bindingResult,
                                       @RequestParam("method") String method, RedirectAttributes redirectAttributes) {

        switch (method) {
            case "put":
                return updateSettings(settings, bindingResult, redirectAttributes);
            case "delete":
                return deleteSettings(settings.getId());
            default:
                return saveNewSettings(settings, bindingResult, redirectAttributes);
        }
    }

    private String saveNewSettings(Settings settings, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при создании нового параметра. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/settings/add";
        }

        if (!prestigeSettingsDAO.saveNewSettings(settings)) {
            String errorMessage = "Указанные Вами параметры уже существуют.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("settings", settings);
            return "redirect:/prestige/settings/add";
        }
        return "redirect:/prestige/settings";
    }

    @GetMapping("/{settingsId}")
    public ModelAndView findClientById(@PathVariable("settingsId") Integer settingsId, Model model) {
        ModelAndView mav = new ModelAndView("prestige/settings/edit-settings");
        Settings settings = prestigeSettingsDAO.findSettingsById(settingsId);

        if (settings == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", "/prestige/settings");
        }
        else {
            mav.addObject("settings", settings);
        }
        return mav;
    }

    private String updateSettings(Settings settings, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            String errorMessage = "Возникла ошибка при редактировании настройки. Пожалуйста, попробуйте заново.";
            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/settings/";
        }
        else if (settings.equals(prestigeSettingsDAO.findSettingsById(settings.getId()))) {
            return "redirect:/prestige/settings/";
        }
        else if (prestigeSettingsDAO.checkIfSettingsExists(settings)) {
            String errorMessage = "Указанные Вами настройки уже существуют.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return "redirect:/prestige/settings/" + settings.getId();
        }
        prestigeSettingsDAO.updateSettings(settings);
        return "redirect:/prestige/settings";
    }

    private String deleteSettings(Integer settingsId) {
        prestigeSettingsDAO.deleteSettingsById(settingsId);
        return "redirect:/prestige/settings";
    }
}
