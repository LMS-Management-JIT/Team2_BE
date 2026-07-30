# ===========================
# Build Stage
# ===========================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Gradle wrapper and config first (better layer caching)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Download dependencies (cached if no change)
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon -x test

# ===========================
# Runtime Stage
# ===========================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user (security best practice)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the jar from builder
COPY --from=builder app/build/libs/*.jar app.jar

EXPOSE 1010

# Healthcheck (optional but recommended)
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget -q --spider http://localhost:1010/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]