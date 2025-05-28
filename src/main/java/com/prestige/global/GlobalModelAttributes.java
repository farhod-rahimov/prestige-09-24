package com.prestige.global;

import com.prestige.service.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

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
}
