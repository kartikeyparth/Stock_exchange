package com.pk.Stock_exchange.dto;

import com.pk.Stock_exchange.model.Side;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PlaceOrderRequest {
    @NotBlank
    private String stockId;
    @NotNull
    private Side side;
    @Min(1)
    private long price;
    @Min(1)
    private long quantity;
}
