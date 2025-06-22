package com.prestige.service;

import com.prestige.models.enums.EStep;
import com.prestige.models.Product;
import com.prestige.models.ProductName;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

@Component
public class Utils {

    public String capitalizeString(String string) {

        if (string.length() < 2) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() +
                string.substring(1).toLowerCase();
    }

    public String toUpperCase(String string) {
        return string.toUpperCase();
    }

    public String formatArticleNumber(String articleNumber, boolean shouldReplaceAsterisk) {

        if (shouldReplaceAsterisk) {
            articleNumber = articleNumber.replaceAll("[^a-zA-Z0-9]", ""); // удалить все символы кроме БУКВ и ЦИФР
        }
        else {
            articleNumber = articleNumber.replaceAll("[^a-zA-Z0-9*]", ""); // удалить все символы кроме БУКВ, ЦИФР и *
        }

        articleNumber = toUpperCase(articleNumber);

        if (articleNumber.length() == 10) {
            articleNumber = articleNumber.substring(0, 5) + "-" + articleNumber.substring(5); // 54651-2H0000
        }
        return articleNumber;
    }

    public void formatProductAttributes(Product product) {
        product.setCarModel(toUpperCase(product.getCarModel()));
        product.setOemArticleNumber(formatArticleNumber(product.getOemArticleNumber(), true));
        product.setCrossArticleNumber(formatArticleNumber(product.getCrossArticleNumber(), true));
    }

    public Timestamp getCurrentDateAndTime() {
        long currentTimeMillis = System.currentTimeMillis();

        return new Timestamp(currentTimeMillis - (currentTimeMillis % 1000));
    }

    private String getPattern(Integer scale) {
        StringBuilder pattern = new StringBuilder("#,###");

        if (scale > 0) {
            pattern.append(".");

            for (int i = 0; i < scale; i++) {
                pattern.append("0");
            }
        }
        return pattern.toString();
    }

    public String getConvertedPriceToSmn(BigDecimal price, BigDecimal magicValue, BigDecimal exchangeRate, Integer scale) {
        BigDecimal hundred = new BigDecimal("100");

        // (100 + magicValue) / 100) * exchangeRate
        BigDecimal convertedPriceToSmn = price.multiply(hundred.add(magicValue).divide(hundred)).multiply(exchangeRate);

        BigDecimal result = convertedPriceToSmn.setScale(scale, RoundingMode.HALF_UP);

        return getFormattedBigDecimal(result, scale);
    }

    public String getFormattedBigDecimal(BigDecimal object, Integer scale) {

        BigDecimal zero = new BigDecimal(0).setScale(scale);

        if (object.equals(zero)) {
            return zero.toString();
        }

        // Настроим DecimalFormat с пробелом в качестве разделителя тысяч
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("ru", "RU"));
        symbols.setGroupingSeparator(' ');  // Устанавливаем пробел как разделитель тысяч
        symbols.setDecimalSeparator('.');  // Устанавливаем точку как разделитель десятичных знаков

        // Форматирование результата до нужного формата (например, 2 знака после запятой)
        DecimalFormat decimalFormat = new DecimalFormat(getPattern(scale), symbols);  // Формат с разделителем тысяч и двумя знаками после запятой
        return decimalFormat.format(object);
    }

    public String getDateTimeFormatted(Timestamp dateTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMMM-yyyy HH:mm", new Locale("ru", "RU"));
        return sdf.format(dateTime);
    }

    public String getTruncatedText(String text, Integer charQuantity) {
        String ellipsis = " ...";

        if (text.length() <= charQuantity + ellipsis.length()) {
            return text;
        }
        return text.substring(0, charQuantity) + ellipsis;
    }

    public boolean shouldShowNextButton(EStep step, List<ProductName> allProductNames) {

        switch (step) {
            case PRODUCT_NAME_SELECTION:
                return allProductNames != null && allProductNames.isEmpty() ? false : true;
            case PRODUCT_SELECTION_DONE:
                return false;
            default:
                return true;
        }
    }

    public boolean clientSelectionDone(EStep step) {

        switch (step) {
            case CLIENT_NAME_SELECTION:
            case CLIENT_PHONE_NUMBER_SELECTION:
                return false;
            default:
                return true;
        }
    }

    public boolean supplierSelectionDone(EStep step) {

        switch (step) {
            case SUPPLIER_STORE_NAME_SELECTION:
            case SUPPLIER_MANAGER_NAME_SELECTION:
            case SUPPLIER_PHONE_NUMBER_SELECTION:
                return false;
            default:
                return true;
        }
    }

    public boolean shouldShowAddedItemsInTheBasket(EStep step, List<Object> list, boolean isLastItem) {
        boolean isEmptyList = list == null || list.isEmpty();

        switch (step) {
            case PRODUCT_SELECTION_DONE:
            case PRODUCT_SELECTION_WITH_FULL_LIST:
                return !isEmptyList || isLastItem;
            default:
                return !isEmptyList && !isLastItem;
        }
    }

    public boolean shouldShowCheckBox(EStep step) {

        switch (step) {
            case PRODUCT_SELECTION_DONE:
                return true;
            default:
                return false;
        }
    }

    public String getBackgroundColorByGroupId(Integer groupId) {

        switch (groupId % 5) {
            case 0: return "#E0F7FA"; // light blue
            case 1: return "#E8F5E9"; // light green
            case 2: return "#FFF9C4"; // light yellow
            case 3: return "#FCE4EC"; // light pink
            default: return "#F3E5F5"; // light purple
        }
    }
}
