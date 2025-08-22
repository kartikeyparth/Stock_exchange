package com.pk.Stock_exchange.controller;

import com.pk.Stock_exchange.dto.TradeResponse;
import com.pk.Stock_exchange.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trades")
@RequiredArgsConstructor
public class TradeController {
    private final TradeService tradeService;

    @GetMapping("/{stockId}")
    public TradeResponse recent(@PathVariable String stockId, @RequestParam(defaultValue = "50") int limit) {
        return new TradeResponse(tradeService.recent(stockId, limit));
    }
}

