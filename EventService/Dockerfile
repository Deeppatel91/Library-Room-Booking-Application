# --------
# Build stage
# --------
FROM gradle:8-jdk21-alpine AS builder

WORKDIR /home/gradle/src
COPY --chown=gradle:gradle . .

ENV JAVA_HOME=/opt/java/openjdk
ENV GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.java.home=$JAVA_HOME"

RUN ./gradlew build -x test

# --------
# Package stage
# --------
FROM openjdk:21-jdk

RUN mkdir /app

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/event-service.jar

EXPOSE 8060

ENV MONGO_DB_USERNAME=admin \
    MONGO_DB_PWD=password

ENTRYPOINT ["java", "-jar", "/app/event-service.jar"]
