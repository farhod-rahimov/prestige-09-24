package com.prestige.dao.abstracts;

import com.prestige.dao.mappers.SalesBasketMapper;
import com.prestige.models.SalesBasket;
import com.prestige.models.filters.SalesHistoryFilter;
import com.prestige.service.Utils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractSalesBasketsDAO {

    protected final JdbcTemplate jdbcTemplate;

    protected final String salesBasketsTableName;

    protected final String allSalesTableName;

    protected final String clientsTableName;

    protected final Utils utils;

    public AbstractSalesBasketsDAO(JdbcTemplate jdbcTemplate, String salesBasketsTableName,
                                   String allSalesTableName, String clientsTableName, Utils utils) {

        this.jdbcTemplate = jdbcTemplate;
        this.salesBasketsTableName = salesBasketsTableName;
        this.allSalesTableName = allSalesTableName;
        this.clientsTableName = clientsTableName;
        this.utils = utils;
    }

    public Boolean checkIfClientIdExists(Integer clientId) {
        String sql = String.format(
                        "SELECT EXISTS (" +
                            "SELECT 1 FROM %s WHERE client_id = ?)", salesBasketsTableName);

        return jdbcTemplate.queryForObject(sql, new Object[] {clientId}, Boolean.class);
    }

    public List<SalesBasket> findAllBaskets() {
        return findAllBasketsInternal(null, null);
    }

    public List<SalesBasket> findAllSoldBaskets() {
        return findAllBasketsInternal(SalesBasket.EStatus.SOLD, null);
    }

    public List<SalesBasket> findAllSoldBasketsWithFilter(SalesHistoryFilter salesHistoryFilter) {
        return findAllBasketsInternal(SalesBasket.EStatus.SOLD, salesHistoryFilter);
    }

    public List<SalesBasket> findAllBasketsInternal(SalesBasket.EStatus status, SalesHistoryFilter salesHistoryFilter) {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM " + salesBasketsTableName + " psb " +
                        "JOIN " + clientsTableName + " pc on psb.client_id = pc.id " +
                        "WHERE 1 = 1 ";

        List<Object> bindings = new ArrayList();

        if (status != null) {
            sql +=      "AND psb.status = ? ";
            bindings.add(status.getIntValue());
        }

        if (salesHistoryFilter != null) {

            if (!salesHistoryFilter.getClientName().isEmpty()) {
                sql +=  "AND pc.name ILIKE ? ESCAPE '\\' ";
                bindings.add(salesHistoryFilter.getClientName());
            }

            if (salesHistoryFilter.getSoldDate() != null) {

                LocalDate date = salesHistoryFilter.getSoldDate();

                LocalDateTime start = date.atStartOfDay();
                LocalDateTime end = date.plusDays(1).atStartOfDay();

                sql += "AND psb.last_changed_date_time >= ? ";
                sql += "AND psb.last_changed_date_time < ? ";

                bindings.add(start);
                bindings.add(end);
            }

            if (salesHistoryFilter.getBasketId() != null) {
                sql += "AND psb.id = ? ";
                bindings.add(salesHistoryFilter.getBasketId());
            }
        }

        sql +=          "ORDER BY (psb.id)";

        if (!bindings.isEmpty()) {
            return jdbcTemplate.query(sql, bindings.toArray() , new SalesBasketMapper());
        }
        return jdbcTemplate.query(sql, new SalesBasketMapper());
    }

    public SalesBasket findBasketById(Integer basketId) {
        String sql =    "SELECT " +
                            "psb.id, psb.created_date_time, psb.last_changed_date_time, " +
                            "pc.id, pc.name, pc.phone_number, " +
                            "psb.total_sum, psb.notes, psb.status " +
                        "FROM " + salesBasketsTableName + " psb " +
                        "JOIN " + clientsTableName + " pc on psb.client_id = pc.id " +
                        "WHERE psb.id = ?";

        return jdbcTemplate.queryForObject(sql, new Object[] {basketId}, new SalesBasketMapper());
    }

    public void saveNewBasket(SalesBasket basket) {
        String sql =    "INSERT INTO " + salesBasketsTableName + " (" +

                            "created_date_time, " +
                            "last_changed_date_time, " +
                            "client_id, " +
                            "total_sum, " +
                            "notes, " +
                            "status" +
                        ") VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

        basket.setCreatedDateTime(utils.getCurrentDateAndTime());
        basket.setLastChangedDateTime(utils.getCurrentDateAndTime());
        basket.setTotalSum(new BigDecimal(0));

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setTimestamp(1, basket.getCreatedDateTime());
            ps.setTimestamp(2, basket.getLastChangedDateTime());
            ps.setInt(3, basket.getClient().getId());
            ps.setBigDecimal(4, basket.getTotalSum());
            ps.setString(5, basket.getNotes());
            ps.setInt(6, basket.getStatus().getIntValue());

            return ps;
        }, keyHolder);

        basket.setId(keyHolder.getKey().intValue());
    }

    public void updateBasketTotalSum(SalesBasket basket) {
        String sql =    "UPDATE " + salesBasketsTableName + " SET total_sum = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, basket.getTotalSum(), utils.getCurrentDateAndTime(), basket.getId());
    }

    public void updateBasketStatus(SalesBasket basket) {
        String sql =    "UPDATE " + salesBasketsTableName + " SET status = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, basket.getStatus().getIntValue(), utils.getCurrentDateAndTime(), basket.getId());
    }

    public void updateBasketNotes(SalesBasket basket) {
        String sql =    "UPDATE " + salesBasketsTableName + " SET notes = ?, last_changed_date_time = ? WHERE id = ?";

        jdbcTemplate.update(sql, basket.getNotes(), utils.getCurrentDateAndTime(), basket.getId());
    }

    public void deleteAllEmptyBaskets() {
        String sql = String.format(
                        "DELETE FROM %s psb " +
                        "WHERE NOT EXISTS ( " +
                            "SELECT 1 " +
                            "FROM %s pas " +
                            "WHERE pas.basket_id  = psb.id)", salesBasketsTableName, allSalesTableName);

        jdbcTemplate.update(sql);
    }
}
