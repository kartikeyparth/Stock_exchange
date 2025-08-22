package com.pk.Stock_exchange.dao;

import com.pk.Stock_exchange.model.Stock;
import java.util.*;

public interface StockDao {
    void saveAll(List<Stock> stocks);
    Optional<Stock> findById(String id);
    List<Stock> findAll();
}

