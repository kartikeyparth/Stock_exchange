package com.pk.Stock_exchange.service;

import com.pk.Stock_exchange.dao.StockDao;
import com.pk.Stock_exchange.model.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StockSearchService {
    private final StockDao stockDao;

    private static class TrieNode {
        Map<Character, TrieNode> next = new HashMap<>();
        List<Stock> bucket = new ArrayList<>();
    }

    private final TrieNode root = new TrieNode();
    private final Map<String, Stock> byId = new HashMap<>();
    private final TreeMap<String, Stock> byNameSorted = new TreeMap<>();

    public void loadAll() {
        List<Stock> all = stockDao.findAll();
        byId.clear(); byNameSorted.clear();
        root.next.clear(); root.bucket.clear();
        for (Stock s : all) {
            byId.put(s.getId(), s);
            byNameSorted.put(s.getName().toUpperCase(), s);
            insertTrie(s.getName().toUpperCase(), s);
        }
    }

    private void insertTrie(String name, Stock stock) {
        TrieNode cur = root;
        for (char c : name.toCharArray()) {
            cur = cur.next.computeIfAbsent(c, k -> new TrieNode());
            if (cur.bucket.size() < 20) cur.bucket.add(stock);
        }
    }

    public Optional<Stock> byId(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public List<Stock> prefixByName(String prefix, int limit) {
        String p = prefix.toUpperCase();
        TrieNode cur = root;
        for (char c : p.toCharArray()) {
            cur = cur.next.get(c);
            if (cur == null) return List.of();
        }
        List<Stock> res = new ArrayList<>(cur.bucket);
        if (res.size() < limit) {
            var tail = byNameSorted.tailMap(p, true);
            for (var e : tail.entrySet()) {
                if (!e.getKey().startsWith(p)) break;
                if (!res.contains(e.getValue())) res.add(e.getValue());
                if (res.size() >= limit) break;
            }
        }
        return res.stream().limit(limit).toList();
    }
}

