package com.pk.Stock_exchange.dto;

import com.pk.Stock_exchange.model.Stock;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchResponse {
    private List<Stock> items;
    private int count;

    public static SearchResponse of(List<Stock> items) {
        return SearchResponse.builder()
                .items(items)
                .count(items == null ? 0 : items.size())
                .build();
    }
}

