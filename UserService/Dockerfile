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

COPY --from=builder /home/gradle/src/build/libs/*.jar /app/user-service.jar

EXPOSE 8054

ENV SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/UserService \
    SPRING_DATASOURCE_USERNAME=admin \
    SPRING_DATASOURCE_PASSWORD=password

ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
