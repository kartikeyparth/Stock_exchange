package com.pk.Stock_exchange.dao.impl;

import com.pk.Stock_exchange.dao.OrderDao;
import com.pk.Stock_exchange.model.Order;
import org.springframework.stereotype.Repository;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryOrderDao implements OrderDao {
    private final Map<String, Order> orders = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        orders.put(order.getId(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(orders.get(id));
    }

    @Override
    public List<Order> findOpenByStock(String stockId) {
        return orders.values().stream()
                .filter(o -> o.getStockId().equals(stockId) && o.getRemaining() > 0)
                .toList();
    }
}

