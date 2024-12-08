# Project Status Report - GBC_EventBooking - 21

## Table of Contents
- [Requirements](#requirements)

## Requirements

| **Requirement**             | **Description and Learnings**                                                                                                                                                                    | **Status**      |
|-----------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------|
| **Environment Setup**       | Installed essential tools and technologies: Java, Gradle, Spring Boot, Docker, Kafka, MongoDB, PostgreSQL, and Resilience4J.                                                                    | Completed       |
| **Microservices Developed** | Created five core services: RoomService, BookingService, UserService, EventService, and ApprovalService.                                                                                       | Completed       |
| **Database Integration**    | Utilized PostgreSQL for RoomService & UserService and MongoDB for BookingService, EventService, and ApprovalService.                                                                            | Completed       |
| **Authentication & Authorization** | Implemented Keycloak for secure authentication and role-based access control (RBAC).                                                                                      | Completed       |
| **Centralized API Gateway** | Configured the API Gateway (port 9000) for centralized routing and Swagger documentation aggregation for all services.                                                                           | Completed       |
| **Circuit Breaker Integration** | Implemented centralized Resilience4J Circuit Breakers in the API Gateway with health checks and configuration through application.properties. Added Circuit Breakers for BookingService ↔ RoomService and ApprovalService ↔ UserService. | Completed       |
| **Fallback Mechanisms**     | Developed fallback strategies to return messages during service downtimes, ensuring system stability.                                                                                           | Completed       |
| **Event-Driven Communication** | Integrated Kafka for asynchronous messaging: BookingService publishes events on confirmation; EventService consumes these events for booking registration. Schema Registry is used for message validation and compatibility. | Completed       |
| **API Documentation**       | Aggregated Swagger documentation at the API Gateway ([http://localhost:9000/swagger-ui](http://localhost:9000/swagger-ui)) and provided standalone Swagger docs for individual services (e.g., RoomService, BookingService). | Completed       |
| **NotificationService**     | Developed a NotificationService using Mailtrap for email delivery. Sends booking confirmation emails for booking-placed events and event confirmation emails for event-placed events.           | Completed       |
| **Dockerization**           | Containerized all services, broker, Kafka, and databases using Docker Compose for simplified deployment and scalability.                                                                         | Completed       |
| **Postman Collection**      | Exported Postman collection for testing API endpoints via the API Gateway and direct service endpoints.                                                                                         | Completed       |

## Notes
This project showcases a distributed microservice architecture using modern tools and technologies, emphasizing resilience, scalability, and reliability. All features were developed with industry best practices in mind.

