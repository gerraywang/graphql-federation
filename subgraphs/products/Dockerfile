FROM gradle:7.6.1-jdk17 as builder
WORKDIR /app
COPY . .
RUN gradle :subgraphs:products:build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/subgraphs/products/build/libs/*.jar ./app.jar
EXPOSE 4002
CMD ["java", "-jar", "app.jar"] 