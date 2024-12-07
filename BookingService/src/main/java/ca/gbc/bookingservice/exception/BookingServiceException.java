package ca.gbc.bookingservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class BookingServiceException extends ResponseStatusException {

    public BookingServiceException(HttpStatus status, String message, Throwable cause) {
        super(status, message, cause);
    }

    public static BookingServiceException unexpectedError(String message, Throwable cause) {
        return new BookingServiceException(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    public static BookingServiceException bookingNotFound(String message) {
        return new BookingServiceException(HttpStatus.NOT_FOUND, message, null);
    }

    public static BookingServiceException roomAlreadyOccupied(String message) {
        return new BookingServiceException(HttpStatus.CONFLICT, message, null);
    }

    public static BookingServiceException roomNotFound(String message) {
        return new BookingServiceException(HttpStatus.NOT_FOUND, message, null);
    }

    public static BookingServiceException userAuthenticationFailed(String message) {
        return new BookingServiceException(HttpStatus.UNAUTHORIZED, message, null);
    }

    public static BookingServiceException serviceUnavailable(String message) {
        return new BookingServiceException(HttpStatus.SERVICE_UNAVAILABLE, message, null);
    }

    public BookingServiceException withStatus(HttpStatus status) {
        return new BookingServiceException(status, this.getReason(), this.getCause());
    }

}
