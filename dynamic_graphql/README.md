# GraphQL Federation Demo

A demonstration of GraphQL Federation using Kotlin, Apollo Router, and Ktor.

## Architecture

```
Client → API → Gateway(Apollo Router) → Subgraphs (MST/TRAN)
```

The project implements a federated GraphQL architecture:

1. **API** (`:api`)
   - Provides RESTful endpoints
   - Translates REST requests to GraphQL queries
   - Handles request routing and aggregation

2. **Apollo Router** (Gateway)
   - Implements GraphQL federation
   - Routes queries to appropriate subgraphs
   - Combines responses from multiple subgraphs

3. **Subgraphs**(Mock)
   - MST (Master Data Service)
     - Handles master data (customers, products)
     - Provides basic entity information
   - TRAN (Transaction Service)
     - Manages transaction data (orders, payments)
     - Handles business operations

## Project Structure

```
.
├── api/             # API service
├── subgraphs/       # GraphQL subgraphs
│   ├── common/      # Shared code between subgraphs
│   ├── mst/         # Master data service (customers, products)
│   └── tran/        # Transaction service (orders, payments)
└── gateway          # Apollo Router configuration
```

## Prerequisites

- JDK 11 or later
- Gradle 7.x or later
- [Rover CLI](https://www.apollographql.com/docs/rover/getting-started)

## Getting Started

### Local Development

1. Generate the supergraph schema:
```bash
rover supergraph compose --config dynamic_graphql/gateway/supergraph.yaml > dynamic_graphql/gateway/supergraph.graphql
```

2. Build the project:
```bash
./gradlew clean build
```

3. Start the services (in order):
```bash
# 1. Start Master Data Service
./gradlew :dynamic_graphql:subgraphs:mst:run

# 2. Start Transaction Service
./gradlew :dynamic_graphql:subgraphs:tran:run

# 3. Start Apollo Router
RUST_LOG=debug ./dynamic_graphql/gateway/router --config ./dynamic_graphql/gateway/router.yaml --supergraph ./dynamic_graphql/gateway/supergraph.graphql

# 4. Start API Gateway
./gradlew :dynamic_graphql:api:run
```


## API Examples

### Query Orders
Get all orders:
```bash
curl "http://localhost:8080/orders"
```

### Filter Orders

By amount:
```bash
curl "http://localhost:8080/orders?amount_gt=1000"
```

By customer name:
```bash
curl "http://localhost:8080/orders?customer.name_like=John"
```

By order status:
```bash
curl "http://localhost:8080/orders?status_eq=PENDING"
```

## Contributing

Feel free to submit issues and enhancement requests.
