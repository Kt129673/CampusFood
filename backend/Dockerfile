# ===========================
# CampusFood Backend - Multi-Stage Dockerfile
# ===========================

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app
USER appuser

# Expose port (Elastic Beanstalk uses 5000 by default)
EXPOSE 5000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget -qO- http://localhost:5000/actuator/health || exit 1

# Run with production profile
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "-Xmx512m", "-Xms256m", "app.jar"]
