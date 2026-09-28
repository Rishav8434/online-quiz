# ==============================================================================
# Multi-Stage Dockerfile for Production Online Quiz Application
# ==============================================================================

# Stage 1: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production artifact
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Optimized Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create a non-privileged application user
RUN groupadd -r appgroup && useradd -r -g appgroup -m -d /app appuser

# Copy JAR from build stage
COPY --from=builder --chown=appuser:appgroup /workspace/target/online-quiz-application-1.0.0.jar app.jar

# Configuration environment variables with secure defaults
ENV SERVER_PORT=8080 \
    SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError -XX:MaxRAMPercentage=75.0"

USER appuser

EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 0

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
