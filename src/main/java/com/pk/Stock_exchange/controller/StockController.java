package com.pk.Stock_exchange.controller;

import com.pk.Stock_exchange.model.Stock;
import com.pk.Stock_exchange.service.StockSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockSearchService stockSearchService;

    @PostMapping("/_reload")
    public ResponseEntity<Void> reload() {
        stockSearchService.loadAll();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Stock> getById(@PathVariable String id) {
        return stockSearchService.byId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Stock> search(@RequestParam String prefix,@RequestParam(defaultValue = "50") int limit) {
        return stockSearchService.prefixByName(prefix, limit);
    }
}
