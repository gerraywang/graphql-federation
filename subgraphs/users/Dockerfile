FROM gradle:7.6.1-jdk17 as builder
WORKDIR /app
COPY . .
RUN gradle :subgraphs:users:build --no-daemon

FROM openjdk:17-slim
WORKDIR /app
COPY --from=builder /app/subgraphs/users/build/libs/*.jar ./app.jar
EXPOSE 4001
CMD ["java", "-jar", "app.jar"] 