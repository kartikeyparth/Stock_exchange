package com.pk.Stock_exchange.service;

import com.pk.Stock_exchange.dao.TradeDao;
import com.pk.Stock_exchange.model.Trade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeService {
    private final TradeDao tradeDao;
    public List<Trade> recent(String stockId, int limit) {
        return tradeDao.findByStock(stockId, limit);
    }
}

