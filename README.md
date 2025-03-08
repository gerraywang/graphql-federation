# graphql-federation

run for local
# 1. 启动 users 服务
./gradlew :subgraphs:users:run

# 2. 在新终端启动 products 服务
./gradlew :subgraphs:products:run

# 3. 在新终端启动 router
./router --config router.yaml --supergraph supergraph.graphql

# 测试 users 服务
curl -X POST http://localhost:4001/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ users { id name email } }"}'

# 测试 products 服务
curl -X POST http://localhost:4002/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ products { id name price } }"}'

# 测试 router
curl -X POST http://localhost:4000/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{ products { id name price owner { id name email } } }"}'