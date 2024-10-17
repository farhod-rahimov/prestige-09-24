package com.prestige.dao;

import com.prestige.models.Product;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

@Component
public class Utils {

    public String capitalizeString(String string) {
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
        product.setArticleNumber(formatArticleNumber(product.getArticleNumber(), true));
    }
}
