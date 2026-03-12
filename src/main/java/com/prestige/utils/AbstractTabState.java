package com.prestige.utils;

import com.prestige.models.enums.EStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public abstract class AbstractTabState {

    protected String tabId;

    protected EStep step;

    protected LocalDateTime lastAccessedDateTime = LocalDateTime.now();

    public AbstractTabState (String tabId, EStep step) {
        this.tabId = tabId;
        this.step = step;
    }
}
