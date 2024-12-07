package com.prestige.controllers.common;

import javax.servlet.http.HttpServletRequest;

public abstract class AbstractCommonBaseController {

    protected boolean isPrestigeRequest(String requestURI) {
        return requestURI.contains("prestige");
    }

    protected String getURIBeginning(HttpServletRequest request) {
        return isPrestigeRequest(request.getRequestURI()) ? "prestige" : "sklad";
    }
}
