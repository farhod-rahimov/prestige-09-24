package com.prestige.forms;

import com.prestige.models.filters.ProductMarginFilter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductMarginForm {

    private Map<Integer, BigDecimal> marginMap = new HashMap<>();

    private ProductMarginFilter marginFilter;
}
