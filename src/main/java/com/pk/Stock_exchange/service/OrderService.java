package com.pk.Stock_exchange.service;

import com.pk.Stock_exchange.dao.OrderDao;
import com.pk.Stock_exchange.dto.OrderBookSnapshot;
import com.pk.Stock_exchange.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderDao orderDao;
    private final MatchingEngine engine;

    public List<Trade> place(String stockId, Side side, long price, long qty) {
        Assert.isTrue(price > 0 && qty > 0, "invalid");
        Order o = Order.newOrder(stockId, side, price, qty);
        orderDao.save(o);
        return engine.place(o);
    }

    public OrderBookSnapshot snapshot(String stockId, int depth) {
        return engine.snapshot(stockId, depth);
    }
}

