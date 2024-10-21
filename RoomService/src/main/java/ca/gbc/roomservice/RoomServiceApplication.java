package ca.gbc.roomservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot Application for Room Service.
 * This class serves as the entry point for the Spring Boot application.
 * The @SpringBootApplication annotation enables auto-configuration, component scanning, and
 * configuration properties support, setting up the application context.
 */
@SpringBootApplication
public class RoomServiceApplication {

    /**
     * Main method to launch the application.
     * @param args Command line arguments passed to the application.
     */
    public static void main(String[] args) {
        // Launch the Spring Boot application
        SpringApplication.run(RoomServiceApplication.class, args);
    }
}
