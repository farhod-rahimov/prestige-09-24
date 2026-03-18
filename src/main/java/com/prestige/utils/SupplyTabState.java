package com.prestige.utils;

import com.prestige.models.BaseSupplyBasket;
import com.prestige.models.enums.EStep;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class SupplyTabState extends AbstractTabState {

    private BaseSupplyBasket basket;

    public SupplyTabState(String tabId, EStep step, BaseSupplyBasket basket) {
        super(tabId, step);
        this.basket = basket;
    }
}
