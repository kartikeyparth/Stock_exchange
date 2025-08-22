package com.pk.Stock_exchange.dao.impl;

import com.pk.Stock_exchange.dao.StockDao;
import com.pk.Stock_exchange.model.Stock;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryStockDao implements StockDao {
    private final Map<String, Stock> byId = new ConcurrentHashMap<>();

    @Override
    public void saveAll(List<Stock> stocks) {
        for (Stock s : stocks) byId.put(s.getId(), s);
    }

    @Override
    public Optional<Stock> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Stock> findAll() {
        return new ArrayList<>(byId.values());
    }
}

