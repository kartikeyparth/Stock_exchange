package com.pk.Stock_exchange.dao;

import com.pk.Stock_exchange.model.Trade;
import java.util.List;

public interface TradeDao {
    void save(Trade trade);
    List<Trade> findByStock(String stockId, int limit);
}

