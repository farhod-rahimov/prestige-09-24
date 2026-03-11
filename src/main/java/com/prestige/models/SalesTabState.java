package com.prestige.models;

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
public class SalesTabState {

    private String tabId;

    private EStep step;

    private SalesBasket basket;

    private LocalDateTime createdDateTime = LocalDateTime.now();

    public SalesTabState(String tabId, EStep step, SalesBasket basket) {
        this.tabId = tabId;
        this.step = step;
        this.basket = basket;
    }
}
