package ca.gbc.eventservice.Client;

import ca.gbc.eventservice.dto.Bookings;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "booking-service", url = "${BOOKING_SERVICE_URL}")
public interface BookingServiceFeignClient {
    @GetMapping("/api/bookings/{id}")
    Bookings getBookingById(@RequestHeader("Authorization") String authorization, @PathVariable("id") String bookingId);
}
