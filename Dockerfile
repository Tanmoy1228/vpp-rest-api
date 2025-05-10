# Stage 1: Build the application
FROM gradle:8.13-jdk17-alpine AS builder

WORKDIR /app

# Copy only what's needed for build caching
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle build --no-daemon || return 0

# Copy full source after caching dependencies
COPY src ./src

# Build the jar
RUN gradle bootJar --no-daemon

# Stage 2: Run the application
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the jar from builder stage
COPY --from=builder /app/build/libs/vpp-rest-api-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
