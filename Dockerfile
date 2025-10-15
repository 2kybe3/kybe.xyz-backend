FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./

RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew build --no-daemon

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
