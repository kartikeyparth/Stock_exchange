package com.pk.Stock_exchange.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class Trade {
    private String id;
    private String stockId;
    private String buyOrderId;
    private String sellOrderId;
    private long price;
    private long quantity;
    private Instant executedAt;

    public static Trade of(
            String stockId, String buyId, String sellId, long price, long qty) {
        return Trade.builder()
                .id(UUID.randomUUID().toString())
                .stockId(stockId)
                .buyOrderId(buyId)
                .sellOrderId(sellId)
                .price(price)
                .quantity(qty)
                .executedAt(Instant.now())
                .build();
    }
}

