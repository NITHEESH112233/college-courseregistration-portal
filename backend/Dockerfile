# Stage 1: Build the application using Maven & Java 17
FROM maven:3.9-eclipse-temurin-17-alpine AS builder
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build runnable JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal, secure runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Run as non-root user for container security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy executable jar from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose default application port
EXPOSE 8080

# Optimized JVM memory allocation for containers
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

