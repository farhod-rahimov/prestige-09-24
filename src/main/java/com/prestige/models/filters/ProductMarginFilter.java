package com.prestige.models.filters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductMarginFilter extends ProductFilter {

    protected BigDecimal margin;

    protected EComparison marginComparison;

    @AllArgsConstructor
    @Getter
    public enum EComparison {
        LESS_THAN("LESS_THAN"),
        LESS_OR_EQUALS_THAN("LESS_OR_EQUALS_THAN"),
        GREATER_THAN("GREATER_THAN"),
        GREATER_OR_EQUALS_THAN("GREATER_OR_EQUALS_THAN"),
        EQUALS("EQUALS"),
        NOT_SET("NOT_SET");

        private final String value;
    }

    public ProductMarginFilter(ProductMarginFilter productMarginFilter) {
        super(productMarginFilter);
        margin = productMarginFilter.getMargin();
        marginComparison = productMarginFilter.getMarginComparison();
    }

    public boolean isNotEmpty() {
        return super.isNotEmpty() || margin != null;
    }

    @Override
    public void clear() {
        super.clear();
        margin = null;
        marginComparison = null;
    }

    @Override
    public void prepareAllFields() {
        super.prepareAllFields();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        ProductMarginFilter other = (ProductMarginFilter)obj;
        return  super.equals(other) &&
                Objects.equals(margin, other.margin) &&
                Objects.equals(marginComparison, other.marginComparison);
    }

    public String getComparisonExpr(String columnName, EComparison comparison, List<Object> bindings, Object bindingValue) {
        String expr = "AND " + columnName;

        switch (comparison) {
            case LESS_THAN:
                expr += " < ? "; break;
            case LESS_OR_EQUALS_THAN:
                expr += " <= ? "; break;
            case GREATER_THAN:
                expr += " > ? "; break;
            case GREATER_OR_EQUALS_THAN:
                expr += " >= ? "; break;
            case EQUALS:
                expr += " = ? "; break;
            case NOT_SET:
                expr += " IS NULL "; break;
        }
        if (comparison != ProductMarginFilter.EComparison.NOT_SET) {
            bindings.add(bindingValue);
        }
        return expr;
    }
}
