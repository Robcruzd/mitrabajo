# -------- Stage 1: Build --------
FROM gradle:8.5-jdk21 AS builder

WORKDIR /app
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon || true

COPY . .
RUN ./gradlew clean build -x test --no-daemon

# -------- Stage 2: Runtime --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8085

ENTRYPOINT ["java","-jar","app.jar"]
