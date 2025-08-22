Below is the structure of the Project
├─ StockExchangeApplication.java
├─ controller
│  ├─ OrderController.java
│  └─ StockController.java
   └─ TradeController.java
├─ service
│  ├─ OrderService.java
│  ├─ StockSearchService.java
│  ├─ MatchingEngine.java
│  └─ TradeService.java
├─ dao
│  ├─ OrderDao.java
│  ├─ TradeDao.java
│  ├─ StockDao.java
│  └─ impl
│     ├─ InMemoryOrderDao.java
│     ├─ InMemoryTradeDao.java
│     └─ InMemoryStockDao.java
├─ model
│  ├─ Order.java
│  ├─ Trade.java
│  ├─ Stock.java
│  ├─ Side.java
│  └─ OrderStatus.java
└─ dto
   ├─ PlaceOrderRequest.java
   ├─ OrderResponse.java
   ├─ TradeResponse.java
   └─ SearchResponse.java
   ├─ OrderRequestSnapshot.java


i made controller, service ,dao, dto layers to make the project

StockExchangeApplication.java

This is just the entry point. The main function starts the Spring Boot app.

controller

OrderController.java

This is the door for anything related to orders. Two main things it does:
POST /api/orders takes a JSON body (PlaceOrderRequest) and sends it to the service. It doesn’t do business logic; it just validates inputs and forwards them.
GET /api/orders/{stockId}/book gives me a snapshot of the order book for a symbol (best bids/asks up to a depth). This is mainly for debugging and showing the state of the market.

I keep controllers thin on purpose—no business rules here, just for requests/responses.

StockController.java

This is for stocks themselves, not trades. It exposes:
POST /api/stocks/_reload to load the in-memory index (I’ll explain that in the search service).
GET /api/stocks/{id} to fetch one stock by ID.
GET /api/stocks/search?prefix=... to search by name prefix (quick type-ahead style search).

So if I want to know whether a stock exists before placing an order, or I want a little search box experience, this is where I look.

TradeController.java

This one exposes executed trades. Typical endpoint:
GET /api/trades/{stockId}?limit=50 shows the most recent trades for a given stock.
It doesn’t generate trades—that’s the matching engine’s job. This just fetches them so I can see what filled.









service

OrderService.java

When a request to place an order arrives:
validate the payload (price, quantity, side, stock exists).
create an Order object and persist it via OrderDao.
push the order into MatchingEngine to see what fills immediately.
update the order’s remaining and status (PENDING / PARTIALLY_FILLED / FILLED).
Any trades produced by the engine get persisted via TradeService/TradeDao.
build an OrderResponse that tells the client what happened.

This service also produces the order book snapshot for /book, by asking the engine/dao for best bids/asks and formatting it.

Data structures used: whatever the matching engine exposes (priority queues for bids/asks), plus DAOs which are maps/lists in memory.


StockSearchService.java

This is my fast stock search. It builds a tiny in-memory search index so I’m not scanning line by line:
keep a Map<String, Stock> byId for O(1) lookup by ticker.
keep a TreeMap<String, Stock> byNameSorted so I can walk a sorted range of names.
also build a simple Trie (prefix tree) where each node holds a small bucket (up to ~20 matches). This makes prefix lookups quick and bounded.

Workflow:

loadAll() pulls all stocks from StockDao, clears the indexes, and rebuilds the trie and maps.
byId(id) returns a single stock from byId.
prefixByName(prefix, limit) walks the trie to grab a tiny pre-collected bucket, and if I still need more, I scan byNameSorted.tailMap(prefix...) until the prefix no longer matches or I hit the limit.
This lets me handle large-ish datasets without crying. Fast enough for the assignment and easy to reason about.


MatchingEngine.java

Here I maintain two priority queues per stock:

Bids (buy orders): a max-heap sorted by price descending; tie-break with earlier createdAt.
Asks (sell orders): a min-heap sorted by price ascending; tie-break with earlier createdAt.

When a new buy comes in:

I compare it against the best ask. If buy.price >= ask.price, I match them for min(buy.remaining, ask.remaining). That creates a Trade (with the ask’s price, standard continuous market behavior).
Decrement both remaining quantities and update statuses. If any hits zero, it pops off the queue. I keep going until either the buy is fully done or there’s no ask cheap enough.
If there’s leftover remaining on the buy, I park it into the bid book.
When a new sell comes in, same logic but mirrored against best bid.

At the end, I return the list of trades generated for this incoming order. The service persists those. The engine is pure logic + data structures; it doesn’t talk HTTP or databases.

Data structures used here:

Map<String, OrderBook> keyed by stock ID.
Each OrderBook has two PriorityQueue<Order>s (bids, asks) with custom comparators (price, then FIFO).
This gives me price-time priority naturally, which is how real exchanges operate.

TradeService.java

Very lean: a small facade over TradeDao.
recent(stockId, limit) gives last N trades for a symbol.
save(trade) persists a new trade.
It keeps the controllers and other services from poking the DAO directly.





model 

Order.java

Represents one live order in the book or an order that was fully matched.
Fields I maintain:
id (uuid), stockId, side (BUY / SELL),
price, quantity, remaining,
createdAt (for FIFO inside same price),
status (PENDING, PARTIALLY_FILLED, FILLED).

I usually construct new ones via a helper (e.g., Order.newOrder()) so id/createdAt/remaining/status are set consistently. Lombok reduces boilerplate for getters/setters/builders.

Trade.java

Represents one executed match between a buyer and a seller:
id, stockId,
buyOrderId, sellOrderId,
price, quantity,
executedAt.
I generate these inside the matching engine whenever a fill happens.

Stock.java

Just basic info about a tradable symbol:
id (ticker),
name.
Side.java

Enum with two values: BUY, SELL.
Using an enum avoids typos like "selll" in logic.

OrderStatus.java

Enum to track lifecycle of an order:
PENDING (sitting in the book),
PARTIALLY_FILLED (some quantity matched),
FILLED (done, off the book).




dao (Data Access Objects)

I kept persistence in-memory to keep things fast and easy to test. If I swap to a real DB later, I’ll just replace the impl classes; interfaces stay the same.

OrderDao.java

Contract for storing and querying orders. Typical methods:
save(Order)
findById(String id)
openBids(stockId), openAsks(stockId) or a structured getOrderBook(stockId)
maybe update(Order) as status/remaining changes

The idea is to abstract away storage details so services don’t care whether this is a map, a DB, or a file.

TradeDao.java

For storing and reading trades:
save(Trade)
recentByStock(String stockId, int limit)
maybe findAllByStock(String stockId) if I need more than a limit.

StockDao.java

Contract for stock master data:
findAll() (used by the search service’s loadAll())
findById(String id)
maybe a simple search(String q, int limit) if I ever want DAO-level searching too.

impl/InMemoryOrderDao.java

In-memory implementation for orders:

kept a Map<String, Order> for quick direct lookups by ID.
kept per-stock bid/ask priority queues that mirror what the matching engine expects.
Saves/updates trickle into those structures so the book is always consistent.

impl/InMemoryTradeDao.java

Trades are just appended to a List<Trade>, and I keep a per-stock index (like a Map<String, Deque<Trade>>) to return the most recent ones quickly. Super simple.

impl/InMemoryStockDao.java

Holds a bunch of stocks in memory:

Map<String, Stock> byId
List<Stock> or Collection<Stock> for findAll()
This feeds the search service which builds the trie and sorted map on top.









dto (Data Transfer Objects)

PlaceOrderRequest.java

The JSON I accept from clients when they place an order:
stockId, side, price, quantity.
I use Jakarta Validation annotations like @NotBlank, @NotNull, @Min so nonsense inputs get rejected before they touch any logic.

OrderResponse.java

What I return right after placing an order:
message like “Order accepted”
trades list for any immediate matches that happened (so the client sees fills instantly)

TradeResponse.java

Just a wrapper around a list of trades when you ask for recent trades on a stock. Keeps the response consistent and easy to expand later.

SearchResponse.java

A small wrapper for stock search results:
items: List<Stock>
count: int
If I ever paginate or add metadata, this DTO is where it goes.

OrderRequestSnapshot.java

This is the “state dump” object I return when someone asks for a snapshot of the order book (the name’s a bit different in various places; think of it as “order book snapshot”). It typically contains:

stockId,
a timestamp or stringified time,
top bids and top asks trimmed to the requested depth (e.g., 25),
each side is a list of Order or a compact struct with price + quantity.
I use this to show the market state without exposing the internals of the priority queues.





How the whole thing flows 

A client calls POST /api/orders with a buy/sell.

OrderService validates, creates the Order, pushes it into MatchingEngine.

The engine keeps bids and asks in priority queues per stock. It matches using price–time priority until either the incoming order is done or there’s no compatible price left.

Trades pop out; TradeService stores them. The order’s remaining and status are updated.

The response returns with a friendly message and any trades that happened.

Meanwhile, StockSearchService can load a million-ish stocks into a small Trie + TreeMap + Map index.

StockController lets me reload, find by ID, or search by prefix.

TradeController lets me pull recent trades to see what actually filled.

OrderController can show me a book snapshot so I can peek at top bids/asks.
