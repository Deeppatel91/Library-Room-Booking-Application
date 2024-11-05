package ca.gbc.eventservice.Transporter;

import ca.gbc.eventservice.dto.Bookings;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

// BookingServiceFeignClient
@FeignClient(name = "BookingService", url = "${BOOKING_SERVICE_URL}")
public interface BookingServiceFeignClient {
    @GetMapping("/api/bookings/{id}")
    Bookings getBookingById(@PathVariable("id") String bookingId, @RequestHeader("Authorization") String bearerToken);
}
