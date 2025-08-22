package com.pk.Stock_exchange.dto;

import com.pk.Stock_exchange.model.Trade;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class TradeResponse {
    private List<Trade> trades;
}

