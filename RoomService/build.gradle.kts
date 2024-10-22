plugins {
    java
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
}

group = "ca.gbc"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-actuator") // Spring Boot Actuator for monitoring
    implementation("org.springframework.boot:spring-boot-starter-data-jpa") // JPA for database interactions
    implementation("org.springframework.boot:spring-boot-starter-web") // Web starter for REST APIs
    implementation("org.postgresql:postgresql:42.7.4")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    testImplementation("org.springframework.boot:spring-boot-starter-test") // Spring Boot test support (JUnit, Mockito)
    testImplementation("io.rest-assured:rest-assured") // For API testing using RestAssured
    testImplementation("org.springframework.boot:spring-boot-testcontainers") // Spring Boot Testcontainers support
    testImplementation("org.testcontainers:junit-jupiter") // Testcontainers for JUnit 5
    testImplementation("org.testcontainers:postgresql") // PostgreSQL container support for integration testing
    testRuntimeOnly("org.junit.platform:junit-platform-launcher") // JUnit platform runtime launcher
}

tasks.withType<Test> {
    useJUnitPlatform()
}
