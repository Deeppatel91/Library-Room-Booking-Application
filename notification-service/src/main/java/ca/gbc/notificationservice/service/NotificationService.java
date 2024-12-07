package ca.gbc.notificationservice.service;

import ca.gbc.notificationservice.event.BookingPlacedEvent;
import ca.gbc.notificationservice.event.EventPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender javaMailSender;

    @KafkaListener(topics = "booking-placed", groupId = "notificationService")
    public void listenToBookingPlaced(BookingPlacedEvent bookingPlacedEvent) {
        log.info("Listening to 'booking-placed' topic...");

        if (bookingPlacedEvent == null) {
            log.error("Received null BookingPlacedEvent from 'booking-placed' topic.");
            return;
        }

        log.info("Processing booking confirmation for Booking ID: {}", bookingPlacedEvent.getBookingId());
        try {
            sendEmail(
                    bookingPlacedEvent.getEmail(),
                    "Your Booking Confirmation",
                    String.format(
                            """
                            Hello,
                            
                            Your booking has been successfully confirmed.
                            
                            Booking Details:
                            - Booking ID: %s
                            
                            Thank you for using our BookingService.
                            
                            Best Regards,
                            GBC Group-21
                            """,
                            bookingPlacedEvent.getBookingId()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send booking confirmation email. Booking ID: {}, Error: {}", bookingPlacedEvent.getBookingId(), e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "event-placed", groupId = "notificationService")
    public void listenToEventPlaced(EventPlacedEvent eventPlacedEvent) {
        log.info("Listening to 'event-placed' topic...");

        if (eventPlacedEvent == null) {
            log.error("Received null EventPlacedEvent from 'event-placed' topic.");
            return;
        }

        log.info("Processing event confirmation for Event ID: {}", eventPlacedEvent.getEventId());
        try {
            sendEmail(
                    eventPlacedEvent.getEmail(),
                    "Your Event Confirmation",
                    String.format(
                            """
                            Hello,
                            
                            Your event has been successfully registered.
                            
                            Event Details:
                            - Event ID: %s
                            
                            Thank you for using our EventService.
                            
                            Best Regards,
                            GBC Group-21
                            """,
                            eventPlacedEvent.getEventId()
                    )
            );
        } catch (Exception e) {
            log.error("Failed to send event confirmation email. Event ID: {}, Error: {}", eventPlacedEvent.getEventId(), e.getMessage(), e);
        }
    }

    private void sendEmail(String to, String subject, String text) {
        log.info("Preparing email. Recipient: {}, Subject: {}", to, subject);

        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom("deep.patel3@georgebrown.ca");
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);
            messageHelper.setText(text);
        };

        try {
            javaMailSender.send(messagePreparator);
            log.info("Email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Email delivery failed. Recipient: {}, Error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Error occurred while sending email.", e);
        }
    }
}
