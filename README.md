# GraphQL Federation Demo

A demonstration of GraphQL Federation using Kotlin, Apollo Router, and Ktor.

## Project Structure

```
.
├── api/                 # API Gateway service
├── subgraphs/          # GraphQL subgraphs
│   ├── common/         # Shared code between subgraphs
│   ├── mst/           # SubGraphQL: Master data service
│   └── tran/          # SubGraphQL: Transaction service
└── router.yaml         # SuperGraphQL: Apollo Router configuration
```

## Prerequisites

- JDK 11 or later
- Gradle 7.x or later
- [Rover CLI](https://www.apollographql.com/docs/rover/getting-started)

## Getting Started

### Local Development

1. Generate the supergraph schema:
```bash
cd gateway && rover supergraph compose --config supergraph.yaml > supergraph.graphql && cd ..
```

2. Build the project:
```bash
./gradlew clean build
```

3. Start the services:
```bash
# Start Master Data Service
./gradlew :subgraphs:mst:run

# Start Transaction Service
./gradlew :subgraphs:tran:run

# Start Apollo Router
cd gateway && RUST_LOG=debug ./router --config router.yaml --supergraph supergraph.graphql && cd ..

# Start API Gateway
./gradlew :api:run
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
