package com.prestige.dao.prestige;


import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import com.prestige.models.SalesBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class PrestigeAllSalesDAO extends AbstractAllSalesDAO {

    @Autowired
    public PrestigeAllSalesDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "prestige_all_sales");
    }

    public Map<Integer, BigDecimal> findRecommendedPrice(Set<Integer> productIds, SalesBasket basket) {
        String inClause = productIds.stream().map(id -> "?").collect(Collectors.joining(", "));

        String sql =    "WITH recommended_price_table AS (" +
                            "SELECT " +
                                "pas.common_product_id AS id, " +
                                "SUM(COALESCE(pas.quantity, 0) * COALESCE(pas.sold_price, 0)) / " +
                                    "NULLIF(SUM(pas.quantity), 0) AS recommended_price " + // в случае если pas.quantity = 0, результат будет null
                            "FROM prestige_all_sales pas " +
                            "JOIN prestige_sales_baskets psb ON psb.id = pas.basket_id " +
                            "WHERE " +
                                "pas.common_product_id IN (" + inClause + ") AND " +
                                "psb.created_date_time >= ( " +
                                    "SELECT created_date_time - INTERVAL '2 years' " +
                                    "FROM prestige_sales_baskets " +
                                    "WHERE id = ? " +
                                ") AND " +
                                "psb.created_date_time < (" +
                                    "SELECT created_date_time " +
                                    "FROM prestige_sales_baskets " +
                                    "WHERE id = ? " +
                                ") AND " +
                                "psb.status = " + SalesBasket.EStatus.SOLD.getIntValue() + " " +
                            "GROUP BY (pas.common_product_id)) " +

                        "SELECT " +
                            "pas.common_product_id, " +
                            "COALESCE(rpt.recommended_price, 0) " +
                        "FROM prestige_all_sales pas " +
                        "LEFT JOIN recommended_price_table rpt ON rpt.id = pas.common_product_id";

        List<Object> bindings = new ArrayList();
        bindings.addAll(productIds);
        bindings.add(basket.getId());
        bindings.add(basket.getId());

        return jdbcTemplate.query(sql, bindings.toArray(), rs -> {
            Map<Integer, BigDecimal> resultMap = new HashMap<>();

            while (rs.next()) {
                int productId = rs.getInt(1);
                BigDecimal recommendedPrice = rs.getBigDecimal(2);

                resultMap.put(productId, recommendedPrice != null ? recommendedPrice.setScale(2, RoundingMode.HALF_UP) : null);
            }

            for (Integer id : productIds) {
                resultMap.putIfAbsent(id, null);
            }
            return resultMap;
        });
    }
}
