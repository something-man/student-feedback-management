# Stage 1: Build the Spring Boot application using Maven and OpenJDK 21
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build production jar
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Minimal JRE Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create non-root system user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR
COPY --from=builder /app/target/*.jar app.jar

# Configuration environment variables with safe defaults
ENV PORT=8081
ENV SPRING_PROFILES_ACTIVE=postgres

EXPOSE 8081

USER appuser

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
