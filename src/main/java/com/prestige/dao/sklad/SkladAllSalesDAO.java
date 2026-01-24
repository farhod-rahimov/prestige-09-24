package com.prestige.dao.sklad;

import com.prestige.dao.abstracts.AbstractAllSalesDAO;
import com.prestige.models.SalesBasket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

@Component
public class SkladAllSalesDAO extends AbstractAllSalesDAO {

    @Autowired
    public SkladAllSalesDAO(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate, "sklad_all_sales");
    }

    @Override
    public Map<Integer, BigDecimal> findRecommendedPrice(Set<Integer> productIds, SalesBasket basket) {

        // TODO: implement in the future
        throw new UnsupportedOperationException("findRecommendedPrice is not implemented yet");
    }
}
