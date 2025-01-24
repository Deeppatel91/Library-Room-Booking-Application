# Library Room Reservation Project

[![License](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache_Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-FF6C37?style=for-the-badge&logo=keycloak&logoColor=white)
![Resilience4J](https://img.shields.io/badge/Resilience4J-005571?style=for-the-badge&logo=resilience4j&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black)
![Mailtrap](https://img.shields.io/badge/Mailtrap-FF6C37?style=for-the-badge&logo=mailtrap&logoColor=white)

## Description

The **Library Room Reservation Project** is a robust Spring Boot-based microservices application designed to streamline library room reservations. It incorporates modern technologies and best practices to deliver a scalable, fault-tolerant, and secure system. Key features include secure authentication, event-driven communication, automated notifications, and a containerized architecture.

---

## Features

- **Keycloak OAuth 2.0 Authentication:** Secure user authentication and role-based access control (RBAC).
- **Resilience4J Circuit Breakers & Fallback Mechanisms:** Enhanced fault tolerance and system reliability.
- **Automated Email Notifications:** Booking and event confirmation emails via Mailtrap.
- **Scalable Containerized Architecture:** Dockerized microservices with polyglot persistence (PostgreSQL, MongoDB).
- **RESTful APIs:** Documented using Swagger for seamless integration.
- **Asynchronous Communication:** Kafka event streaming for efficient room booking management and staff approval workflows.

---

## Table of Contents

- [Requirements](#requirements)
- [Technologies Used](#technologies-used)
- [Setup and Deployment](#setup-and-deployment)
- [API Documentation](#api-documentation)
- [License](#license)
- [Contact](#contact)

---

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

---

## Technologies Used

- **Backend:** Spring Boot, Java
- **Databases:** PostgreSQL, MongoDB
- **Authentication:** Keycloak OAuth 2.0
- **Resilience:** Resilience4J Circuit Breakers
- **Event Streaming:** Apache Kafka
- **API Documentation:** Swagger
- **Email Notifications:** Mailtrap
- **Containerization:** Docker
- **API Testing:** Postman

---
