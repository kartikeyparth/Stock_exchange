package com.pk.Stock_exchange.dao.impl;

import com.pk.Stock_exchange.dao.TradeDao;
import com.pk.Stock_exchange.model.Trade;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryTradeDao implements TradeDao {
    private final Map<String, Deque<Trade>> tradesByStock = new ConcurrentHashMap<>();

    @Override
    public void save(Trade trade) {
        tradesByStock.computeIfAbsent(trade.getStockId(), k -> new ArrayDeque<>()).addFirst(trade);
    }

    @Override
    public List<Trade> findByStock(String stockId, int limit) {
        Deque<Trade> dq = tradesByStock.getOrDefault(stockId, new ArrayDeque<>());
        return dq.stream().limit(limit).toList();
    }
}

