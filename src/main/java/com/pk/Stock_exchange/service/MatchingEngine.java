package com.pk.Stock_exchange.service;

import com.pk.Stock_exchange.model.*;
import com.pk.Stock_exchange.dao.TradeDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
@RequiredArgsConstructor
public class MatchingEngine {

    private static class OrderBook {
        final PriorityQueue<Order> buys;
        final PriorityQueue<Order> sells;
        final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
        OrderBook() {
            buys = new PriorityQueue<>((a,b) -> {
                int p = Long.compare(b.getPrice(), a.getPrice());
                if (p != 0) return p;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
            sells = new PriorityQueue<>((a,b) -> {
                int p = Long.compare(a.getPrice(), b.getPrice());
                if (p != 0) return p;
                return a.getCreatedAt().compareTo(b.getCreatedAt());
            });
        }
    }

    private final Map<String, OrderBook> books = new ConcurrentHashMap<>();
    private final TradeDao tradeDao;

    private OrderBook book(String stockId) {
        return books.computeIfAbsent(stockId, k -> new OrderBook());
    }

    public List<Trade> place(Order incoming) {
        OrderBook ob = book(incoming.getStockId());
        ob.lock.writeLock().lock();
        try {
            List<Trade> trades = new ArrayList<>();
            if (incoming.getSide() == Side.BUY) {
                matchBuy(incoming, ob, trades);
                if (incoming.getRemaining() > 0) ob.buys.add(incoming);
            } else {
                matchSell(incoming, ob, trades);
                if (incoming.getRemaining() > 0) ob.sells.add(incoming);
            }
            updateStatus(incoming);
            return trades;
        } finally {
            ob.lock.writeLock().unlock();
        }
    }

    private void matchBuy(Order buy, OrderBook ob, List<Trade> out) {
        while (buy.getRemaining() > 0 && !ob.sells.isEmpty()) {
            Order ask = ob.sells.peek();
            if (ask.getPrice() > buy.getPrice()) break;
            long q = Math.min(buy.getRemaining(), ask.getRemaining());
            long px = ask.getPrice();
            ask.setRemaining(ask.getRemaining() - q);
            buy.setRemaining(buy.getRemaining() - q);
            updateStatus(ask);
            updateStatus(buy);
            Trade t = Trade.of(buy.getStockId(), buy.getId(), ask.getId(), px, q);
            tradeDao.save(t);
            out.add(t);
            if (ask.getRemaining() == 0) ob.sells.poll();
        }
    }

    private void matchSell(Order sell, OrderBook ob, List<Trade> out) {
        while (sell.getRemaining() > 0 && !ob.buys.isEmpty()) {
            Order bid = ob.buys.peek();
            if (bid.getPrice() < sell.getPrice()) break;
            long q = Math.min(sell.getRemaining(), bid.getRemaining());
            long px = bid.getPrice();
            bid.setRemaining(bid.getRemaining() - q);
            sell.setRemaining(sell.getRemaining() - q);
            updateStatus(bid);
            updateStatus(sell);
            Trade t = Trade.of(sell.getStockId(), bid.getId(), sell.getId(), px, q);
            tradeDao.save(t);
            out.add(t);
            if (bid.getRemaining() == 0) ob.buys.poll();
        }
    }

    private void updateStatus(Order o) {
        if (o.getRemaining() == 0) o.setStatus(OrderStatus.FILLED);
        else if (o.getRemaining() == o.getQuantity()) o.setStatus(OrderStatus.PENDING);
        else o.setStatus(OrderStatus.PARTIALLY_FILLED);
    }

    public com.pk.Stock_exchange.dto.OrderBookSnapshot snapshot(String stockId, int depth) {
        OrderBook ob = book(stockId);
        ob.lock.readLock().lock();
        try {
            List<Order> buys = new ArrayList<>(ob.buys);
            List<Order> sells = new ArrayList<>(ob.sells);
            buys.sort(ob.buys.comparator());
            sells.sort(ob.sells.comparator());
            return new com.pk.Stock_exchange.dto.OrderBookSnapshot(
                    stockId,
                    Instant.now().toString(),
                    buys.stream().limit(depth).toList(),
                    sells.stream().limit(depth).toList()
            );
        } finally {
            ob.lock.readLock().unlock();
        }
    }
}

