package com.prestige.global;

import com.prestige.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalModelAttributes {

    private final Utils utils;

    @Autowired
    public GlobalModelAttributes(Utils utils) {
        this.utils = utils;
    }

    @ModelAttribute("utils")
    public Utils addUtilsToModel() {
        return utils;
    }

    @ModelAttribute
    public void addURIBeginning(Model model, HttpServletRequest request) {
        model.addAttribute("URIBeginning", request.getRequestURI().contains("prestige") ? "prestige" : "sklad");
    }
}
