# ──────────────────────────────────────────────
# Stage 1: Build with Maven
# ──────────────────────────────────────────────
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Maven wrapper and POM first for dependency caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw dependency:resolve -B -q

# Copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests -B -q

# ──────────────────────────────────────────────
# Stage 2: Lean runtime image
# ──────────────────────────────────────────────
FROM eclipse-temurin:25-jre
WORKDIR /app

# Install curl for Docker healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
