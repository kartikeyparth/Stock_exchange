package com.pk.Stock_exchange.model;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter
@Builder
@AllArgsConstructor @NoArgsConstructor
public class Order {
    private String id;
    private String stockId;
    private Side side;
    private long price;
    private long quantity;
    private long remaining;
    private Instant createdAt;
    private OrderStatus status;

    public static Order newOrder(
            String stockId, Side side, long price, long qty) {
        return Order.builder()
                .id(UUID.randomUUID().toString())
                .stockId(stockId)
                .side(side)
                .price(price)
                .quantity(qty)
                .remaining(qty)
                .createdAt(Instant.now())
                .status(OrderStatus.PENDING)
                .build();
    }
}