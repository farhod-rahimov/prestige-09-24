package com.prestige.controllers.abstracts;

import com.prestige.dao.abstracts.AbstractSettingsDAO;
import com.prestige.models.Settings;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

public abstract class AbstractSettingsController {

    protected final AbstractSettingsDAO settingsDAO;

    protected final String uriPrefix;

    public AbstractSettingsController(AbstractSettingsDAO settingsDAO, String uriPrefix) {
        this.settingsDAO = settingsDAO;
        this.uriPrefix = uriPrefix;
    }

    private void initializeMustHaveSettings() {

        Settings magicValue = new Settings(null, "magicValue", "20");
        if (!settingsDAO.checkIfSettingsExists(magicValue)) {
            settingsDAO.saveNewSettings(magicValue);
        }

        Settings exchangeRate = new Settings(null, "exchangeRate", "11.00");
        if (!settingsDAO.checkIfSettingsExists(exchangeRate)) {
            settingsDAO.saveNewSettings(exchangeRate);
        }
    }

    @GetMapping
    public String showAllSettings(Model model) {
        initializeMustHaveSettings();

        model.addAttribute("allSettings", settingsDAO.findAllSettings());

        return "shared/settings/settings";
    }

    @GetMapping("/add")
    public String addNewSettingGet(Model model) {

        if (model.getAttribute("settings") == null) {
            model.addAttribute("settings", new Settings());
        }
        return "shared/settings/add-new-settings";
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
            return String.format("redirect:/%s/settings/add", uriPrefix);
        }

        if (!settingsDAO.saveNewSettings(settings)) {
            String errorMessage = "Указанные Вами параметры уже существуют.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            redirectAttributes.addFlashAttribute("settings", settings);

            return String.format("redirect:/%s/settings/add", uriPrefix);
        }
        return String.format("redirect:/%s/settings", uriPrefix);
    }

    @GetMapping("/{settingsId}")
    public ModelAndView findClientById(@PathVariable("settingsId") Integer settingsId, Model model) {
        ModelAndView mav = new ModelAndView("shared/settings/edit-settings");
        Settings settings = settingsDAO.findSettingsById(settingsId);

        if (settings == null) {
            mav.setViewName("errors/404");
            mav.setStatus(HttpStatus.NOT_FOUND);
            mav.addObject("previousPage", String.format("/%s/settings", uriPrefix));
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
            return String.format("redirect:/%s/settings/", uriPrefix);
        }
        else if (settings.equals(settingsDAO.findSettingsById(settings.getId()))) {
            return String.format("redirect:/%s/settings/", uriPrefix);
        }
        else if (settingsDAO.checkIfSettingsExists(settings)) {
            String errorMessage = "Указанные Вами настройки уже существуют.";

            redirectAttributes.addFlashAttribute("error", errorMessage);
            return String.format("redirect:/%s/settings/%d", uriPrefix, settings.getId());
        }
        settingsDAO.updateSettings(settings);
        return String.format("redirect:/%s/settings", uriPrefix);
    }

    private String deleteSettings(Integer settingsId) {
        settingsDAO.deleteSettingsById(settingsId);
        return String.format("redirect:/%s/settings", uriPrefix);
    }
}
