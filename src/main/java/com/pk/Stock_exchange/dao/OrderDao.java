package com.pk.Stock_exchange.dao;

import com.pk.Stock_exchange.model.Order;
import java.util.*;

public interface OrderDao {
    void save(Order order);
    Optional<Order> findById(String id);
    List<Order> findOpenByStock(String stockId);
}

