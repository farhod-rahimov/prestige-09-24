package com.prestige.utils;

import com.prestige.models.Product;
import com.prestige.models.enums.EStep;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedList;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductTabState extends AbstractTabState {

    private LinkedList<Product> productList;

    public ProductTabState (String tabId, EStep step, LinkedList<Product> productList) {
        super(tabId, step);
        this.productList = productList;
    }
}
