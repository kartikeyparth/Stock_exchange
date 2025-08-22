package com.pk.Stock_exchange.dto;

import com.pk.Stock_exchange.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderBookSnapshot {
    private String stockId;
    private String timestamp;
    private List<Order> buys;
    private List<Order> sells;
}

