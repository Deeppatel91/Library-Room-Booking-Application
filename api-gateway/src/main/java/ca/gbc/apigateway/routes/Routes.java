package ca.gbc.apigateway.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
@Slf4j
public class Routes {

    @Value("${services.user-url}")
    private String userServiceUrl;

    @Value("${services.event-url}")
    private String eventServiceUrl;

    @Value("${services.room-url}")
    private String roomServiceUrl;

    @Value("${services.booking-url}")
    private String bookingServiceUrl;

    @Value("${services.approval-url}")
    private String approvalServiceUrl;

    @Bean
    public RouterFunction<ServerResponse> userServiceRoutes() {
        log.info("Routing UserService requests to URL: {}", userServiceUrl);

        return GatewayRouterFunctions.route("user-service")
                .route(RequestPredicates.path("/api/users/**"), request -> {
                    log.info("Received request for UserService: {}", request.uri());
                    return HandlerFunctions.http(userServiceUrl).handle(request);
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("UserServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceRoutes() {
        log.info("Routing EventService requests to URL: {}", eventServiceUrl);

        return GatewayRouterFunctions.route("event-service")
                .route(RequestPredicates.path("/api/events/**"), request -> {
                    log.info("Received request for EventService: {}", request.uri());
                    return HandlerFunctions.http(eventServiceUrl).handle(request);
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("EventServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roomServiceRoutes() {
        log.info("Routing RoomService requests to URL: {}", roomServiceUrl);

        return GatewayRouterFunctions.route("room-service")
                .route(RequestPredicates.path("/api/rooms/**"), request -> {
                    log.info("Received request for RoomService: {}", request.uri());
                    return HandlerFunctions.http(roomServiceUrl).handle(request);
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("RoomServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceRoutes() {
        log.info("Routing BookingService requests to URL: {}", bookingServiceUrl);

        return GatewayRouterFunctions.route("booking-service")
                .route(RequestPredicates.path("/api/bookings/**"), request -> {
                    log.info("Received request for BookingService: {}", request.uri());
                    return HandlerFunctions.http(bookingServiceUrl).handle(request);
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("BookingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> approvalServiceRoutes() {
        log.info("Routing ApprovalService requests to URL: {}", approvalServiceUrl);

        return GatewayRouterFunctions.route("approval-service")
                .route(RequestPredicates.path("/api/approvals/**"), request -> {
                    log.info("Received request for ApprovalService: {}", request.uri());
                    return HandlerFunctions.http(approvalServiceUrl).handle(request);
                })
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("ApprovalServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }




    @Bean
    public RouterFunction<ServerResponse> userServiceSwaggerRoute() {
        return route("user_service_swagger")
                .route(RequestPredicates.path("/aggregate/user-service/v3/api-docs"),
                        HandlerFunctions.http("http://user-service:8054/api-docs"))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("UserServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> eventServiceSwaggerRoute() {
        return route("event_service_swagger")
                .route(RequestPredicates.path("/aggregate/event-service/v3/api-docs"),
                        HandlerFunctions.http("http://event-service:8062/api-docs"))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("EventServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> roomServiceSwaggerRoute() {
        return route("room_service_swagger")
                .route(RequestPredicates.path("/aggregate/room-service/v3/api-docs"),
                        HandlerFunctions.http("http://room-service:8090/api-docs"))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("RoomServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> bookingServiceSwaggerRoute() {
        return route("booking_service_swagger")
                .route(RequestPredicates.path("/aggregate/booking-service/v3/api-docs"),
                        HandlerFunctions.http("http://booking-service:8060/api-docs"))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("BookingServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> approvalServiceSwaggerRoute() {
        return route("approval_service_swagger")
                .route(RequestPredicates.path("/aggregate/approval-service/v3/api-docs"),
                        HandlerFunctions.http("http://approval-service:8064/api-docs"))
                .filter(setPath("/api-docs"))
                .filter(CircuitBreakerFilterFunctions
                        .circuitBreaker("ApprovalServiceCircuitBreaker", URI.create("forward:/fallbackRoute")))
                .build();
    }



    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .route(RequestPredicates.all()
                                .and(request -> !request.path().contains("/swagger-ui"))
                                .and(request -> !request.path().contains("/api-docs")),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Service is Temporarily Unavailable, please try again later...."))
                .build();
    }



}