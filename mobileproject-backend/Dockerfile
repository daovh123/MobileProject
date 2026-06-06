# syntax=docker/dockerfile:1

# ── Stage 1: Build ────────────────────────────────────────────────────────────
# Uses Maven with Eclipse Temurin JDK 21 to compile and package the application.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only the POM first to leverage Docker layer caching for dependency resolution.
# If pom.xml hasn't changed, the expensive `dependency:go-offline` step is cached.
COPY pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

# Copy source code and build the executable JAR (tests skipped in Docker build).
COPY src src
RUN mvn -q -DskipTests package

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
# Lightweight JRE-only image – no Maven or source code in the final image.
FROM eclipse-temurin:21-jre
WORKDIR /app

# Render (and most PaaS) inject a dynamic port via $PORT at container start.
# Spring is configured to read it: server.port=${PORT:8080}
ENV PORT=8080
EXPOSE 8080

# Copy the fat JAR produced in the build stage.
COPY --from=build /app/target/*.jar /app/app.jar

# Start the Spring Boot application.
CMD ["java", "-jar", "/app/app.jar"]
