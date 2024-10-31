# --------
# Build stage
# --------
FROM gradle:8-jdk21-alpine AS builder

# Set working directory and copy project files
WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

# Force Gradle to use the Docker-based JDK for compilation
ENV JAVA_HOME=/opt/java/openjdk
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.java.home=$JAVA_HOME"

# Build the project without running tests
RUN ./gradlew build -x test

# --------
# Package stage
# --------
FROM openjdk:21-jdk

# Create an application directory
RUN mkdir /app

# Copy the built JAR from the build stage to the app directory
COPY --from=builder /home/gradle/src/build/libs/*.jar /app/booking-service.jar

# Expose the application port
EXPOSE 8060

ENV MONGO_DB_USERNAME=admin \
    MONGO_DB_PWD=password

# Entry point to run the application
ENTRYPOINT ["java", "-jar", "/app/booking-service.jar"]
