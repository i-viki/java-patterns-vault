package dev.jayav.patterns.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Factory Product: SMS notification via gateway (Twilio/Nexmo mock).
 *
 * <p>Includes E.164 phone number validation and per-minute rate limiting
 * to protect against SMS flooding — a real-world production concern.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class SmsNotification implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(SmsNotification.class);
    private static final int MAX_MESSAGE_LENGTH = 160;
    private static final int RATE_LIMIT_PER_MINUTE = 100;

    private final String gatewayUrl;
    private final String apiKey;
    private final String senderNumber;
    private int sentThisMinute;

    public SmsNotification(String gatewayUrl, String apiKey, String senderNumber) {
        this.gatewayUrl  = gatewayUrl;
        this.apiKey      = apiKey;
        this.senderNumber = senderNumber;
        this.sentThisMinute = 0;
    }

    @Override
    public NotificationResult send(String recipient, String message) {
        log.info("[SMS] Sending to '{}'", recipient);

        if (!isValidPhoneNumber(recipient)) {
            return NotificationResult.failed(getChannel(), recipient,
                    "Invalid phone number. Must be E.164 format e.g. +14155552671");
        }

        if (sentThisMinute >= RATE_LIMIT_PER_MINUTE) {
            log.warn("[SMS] Rate limit reached ({}/min). Message queued.", RATE_LIMIT_PER_MINUTE);
            return NotificationResult.failed(getChannel(), recipient,
                    "Rate limit exceeded: " + RATE_LIMIT_PER_MINUTE + " SMS/min. Try again shortly.");
        }

        String truncated = message.length() > MAX_MESSAGE_LENGTH
                ? message.substring(0, MAX_MESSAGE_LENGTH) : message;

        if (truncated.length() < message.length()) {
            log.warn("[SMS] Message truncated to {} characters", MAX_MESSAGE_LENGTH);
        }

        simulateGatewayCall();
        sentThisMinute++;

        log.info("[SMS] Message delivered to '{}' via {}", recipient, gatewayUrl);
        return NotificationResult.delivered(
                getChannel(), recipient,
                String.format("SMS sent to %s from %s (chars: %d)",
                        recipient, senderNumber, truncated.length())
        );
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public boolean isAvailable() {
        return gatewayUrl != null && !gatewayUrl.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && senderNumber != null && senderNumber.startsWith("+");
    }

    /** Resets rate limit counter (call at start of each new minute window). */
    public void resetRateLimit() {
        this.sentThisMinute = 0;
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.startsWith("+") && phone.length() >= 10;
    }

    private void simulateGatewayCall() {
        try { Thread.sleep(40); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
