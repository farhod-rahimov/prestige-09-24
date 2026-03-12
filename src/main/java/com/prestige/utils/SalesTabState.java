package com.prestige.utils;

import com.prestige.models.SalesBasket;
import com.prestige.models.enums.EStep;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SalesTabState extends AbstractTabState {

    private SalesBasket basket;

    public SalesTabState (String tabId, EStep step, SalesBasket basket) {
        super(tabId, step);
        this.basket = basket;
    }
}
