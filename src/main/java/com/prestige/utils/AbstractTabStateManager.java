package com.prestige.utils;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractTabStateManager {

    protected abstract String getTabStateMapName();

    protected Map<String, AbstractTabState> getTabStateMap(HttpSession session, String mapAttributeName) {
        Map<String, AbstractTabState> map = (Map<String, AbstractTabState>) session.getAttribute(mapAttributeName);

        if (map == null) {
            map = new HashMap<>();
            session.setAttribute(mapAttributeName, map);
        }
        else {
            map.entrySet().removeIf(e ->
                    e.getValue().getLastAccessedDateTime().isBefore(LocalDateTime.now().minusHours(1)));
        }
        return map;
    }

    protected AbstractTabState getTabState(HttpSession session, String mapAttributeName, String tabId) {
        AbstractTabState state =  getTabStateMap(session, mapAttributeName).get(tabId);

        if (state != null) {
            state.setLastAccessedDateTime(LocalDateTime.now());
        }
        return state;
    }

    protected void saveTabState(HttpSession session, String mapAttributeName, AbstractTabState state) {
        state.setLastAccessedDateTime(LocalDateTime.now());
        getTabStateMap(session, mapAttributeName).put(state.getTabId(), state);
    }

    protected void removeTabState(HttpSession session, String mapAttributeName, String tabId) {
        getTabStateMap(session, mapAttributeName).remove(tabId);
    }
}
