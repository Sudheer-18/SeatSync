package com.example.demo.services;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.example.demo.model.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class BookingNotificationService {
    private static final Logger logger = LoggerFactory.getLogger(BookingNotificationService.class);

    private final ObjectProvider<JavaMailSender> mailSenders;
    private final RestClient restClient = RestClient.create();
    private final boolean emailEnabled;
    private final String emailFrom;
    private final boolean smsEnabled;
    private final String twilioAccountSid;
    private final String twilioAuthToken;
    private final String twilioFrom;

    public BookingNotificationService(
        ObjectProvider<JavaMailSender> mailSenders,
        @Value("${seatsync.notifications.email.enabled:false}") boolean emailEnabled,
        @Value("${seatsync.notifications.email.from:}") String emailFrom,
        @Value("${seatsync.notifications.sms.enabled:false}") boolean smsEnabled,
        @Value("${seatsync.notifications.sms.twilio-account-sid:}") String twilioAccountSid,
        @Value("${seatsync.notifications.sms.twilio-auth-token:}") String twilioAuthToken,
        @Value("${seatsync.notifications.sms.twilio-from:}") String twilioFrom
    ) {
        this.mailSenders = mailSenders;
        this.emailEnabled = emailEnabled;
        this.emailFrom = emailFrom;
        this.smsEnabled = smsEnabled;
        this.twilioAccountSid = twilioAccountSid;
        this.twilioAuthToken = twilioAuthToken;
        this.twilioFrom = twilioFrom;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void notifyCustomer(BookingStatusNotification notification) {
        sendEmail(notification);
        sendSms(notification);
    }

    private void sendEmail(BookingStatusNotification notification) {
        if (!emailEnabled) {
            logger.info("Booking {} email notification skipped: email delivery is disabled.", notification.bookingId());
            return;
        }
        JavaMailSender mailSender = mailSenders.getIfAvailable();
        if (mailSender == null || emailFrom.isBlank()) {
            logger.error("Booking {} email notification not sent: configure an SMTP sender and EMAIL_FROM.",
                notification.bookingId());
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(emailFrom);
        message.setTo(notification.email());
        message.setSubject("SeatSync booking #" + notification.bookingId() + ": " + paymentLabel(notification));
        message.setText(emailBody(notification));
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            logger.error("Booking {} email notification could not be sent.", notification.bookingId(), exception);
        }
    }

    private void sendSms(BookingStatusNotification notification) {
        if (!smsEnabled) {
            logger.info("Booking {} SMS notification skipped: SMS delivery is disabled.", notification.bookingId());
            return;
        }
        if (twilioAccountSid.isBlank() || twilioAuthToken.isBlank() || twilioFrom.isBlank()) {
            logger.error("Booking {} SMS notification not sent: configure the Twilio account SID, auth token, and sender.",
                notification.bookingId());
            return;
        }

        var form = new LinkedMultiValueMap<String, String>();
        form.add("To", notification.phone());
        form.add("From", twilioFrom);
        form.add("Body", smsBody(notification));
        String credentials = Base64.getEncoder().encodeToString(
            (twilioAccountSid + ":" + twilioAuthToken).getBytes(StandardCharsets.UTF_8));
        try {
            restClient.post()
                .uri("https://api.twilio.com/2010-04-01/Accounts/{accountSid}/Messages.json", twilioAccountSid)
                .header("Authorization", "Basic " + credentials)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        } catch (RestClientException exception) {
            logger.error("Booking {} SMS notification could not be sent.", notification.bookingId(), exception);
        }
    }

    private String emailBody(BookingStatusNotification notification) {
        String statusMessage = switch (PaymentStatus.valueOf(notification.paymentStatus())) {
            case PENDING -> "Payment is pending. Please complete payment before the payment window expires.";
            case SUCCESS -> "Your payment is confirmed. Your ticket is booked.";
            case REJECTED -> "Your payment was not verified and the booking was cancelled. Contact support if you were charged.";
            case EXPIRED -> "The payment window expired before payment was completed, so the booking is no longer active.";
            case CANCELLED -> "The payment was cancelled.";
            case REFUNDED -> "Your booking was cancelled and the payment was refunded.";
        };
        return "Hello " + notification.customerName() + ",\n\n"
            + "Booking #" + notification.bookingId() + " status: " + notification.bookingStatus() + ".\n"
            + "Payment status: " + notification.paymentStatus() + ".\n"
            + statusMessage + "\n"
            + "Amount: INR " + notification.amount().toPlainString() + "\n\n"
            + "SeatSync";
    }

    private String smsBody(BookingStatusNotification notification) {
        return "SeatSync booking #" + notification.bookingId() + ": " + paymentLabel(notification)
            + ". Booking status: " + notification.bookingStatus()
            + ". Amount INR " + notification.amount().toPlainString() + ".";
    }

    private String paymentLabel(BookingStatusNotification notification) {
        return switch (PaymentStatus.valueOf(notification.paymentStatus())) {
            case PENDING -> "payment pending";
            case SUCCESS -> "ticket confirmed";
            case REJECTED -> "payment failed";
            case EXPIRED -> "payment expired";
            case CANCELLED -> "payment cancelled";
            case REFUNDED -> "payment refunded";
        };
    }
}
