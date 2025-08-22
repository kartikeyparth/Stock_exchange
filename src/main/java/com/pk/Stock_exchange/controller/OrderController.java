package com.pk.Stock_exchange.controller;

import com.pk.Stock_exchange.dto.OrderBookSnapshot;
import com.pk.Stock_exchange.dto.OrderResponse;
import com.pk.Stock_exchange.dto.PlaceOrderRequest;
import com.pk.Stock_exchange.model.Trade;
import com.pk.Stock_exchange.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    public OrderResponse place(@Valid @RequestBody PlaceOrderRequest req) {
        List<Trade> trades = orderService.place(req.getStockId(), req.getSide(), req.getPrice(), req.getQuantity());
        return new OrderResponse("Order accepted", trades);
    }

    @GetMapping("/{stockId}/book")
    public OrderBookSnapshot book(@PathVariable String stockId, @RequestParam(defaultValue = "25") int depth) {
        return orderService.snapshot(stockId, depth);
    }
}

