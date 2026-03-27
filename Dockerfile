# ---- Stage 1: Build ----
FROM maven:3.9.14-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first (layer caching — only re-downloads deps if pom changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# ---- Stage 2: Run ----
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the built jar from Stage 1
COPY --from=builder /app/target/*.jar app.jar

# Expose app port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]