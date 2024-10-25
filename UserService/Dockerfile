# --------
# Build stage
# --------
FROM gradle:8-jdk21-alpine AS builder

# Set working directory and copy project files
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

# Force Gradle to use the Docker-based JDK for compilation, instead of trying to detect system info
ENV JAVA_HOME=/opt/java/openjdk
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.java.home=$JAVA_HOME"

# Build the project without running tests
RUN gradle build -x test

# --------
# Package stage
# --------
FROM openjdk:21-jdk

# Create an application directory
RUN mkdir /app

# Copy the built JAR from the build stage to the app directory
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/user-service.jar

# Expose the application port
EXPOSE 8054

# Set environment variables for PostgreSQL
ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/UserService \
    SPRING_DATASOURCE_USERNAME=admin \
    SPRING_DATASOURCE_PASSWORD=password \
    JWT_SECRET=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJuYW1lIjoiSm9obiBVcGRhdGVkIiwiZW1haWwiOiJqb2hudXBkYXRlZEBleGFtcGxlLmNvbSIsInJvbGUiOiJzdHVkZW50IiwidXNlclR5cGUiOiJzdHVkZW50IiwiaWF0IjoxNzI5ODIwOTA1LCJleHAiOjE3Mjk5MDczMDV9.nDvKqscIiuJwRSlMFdBVlZusvqJXGGvLmqOn7zloFsk \
    JWT_EXPIRATION=86400000

# Entry point to run the application
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
