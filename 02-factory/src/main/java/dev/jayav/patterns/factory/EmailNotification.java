package dev.jayav.patterns.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Factory Product: Email notification via SMTP.
 *
 * <p>Simulates SMTP delivery with retry logic. In production this delegates to
 * JavaMail / Spring Mail / SendGrid SDK.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class EmailNotification implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotification.class);
    private static final int MAX_RETRIES = 3;

    private final String smtpHost;
    private final int smtpPort;
    private final String senderAddress;

    public EmailNotification(String smtpHost, int smtpPort, String senderAddress) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderAddress = senderAddress;
    }

    @Override
    public NotificationResult send(String recipient, String message) {
        log.info("[EMAIL] Sending to '{}' via {}:{}", recipient, smtpHost, smtpPort);

        if (!isValidEmail(recipient)) {
            log.warn("[EMAIL] Invalid email address: '{}'", recipient);
            return NotificationResult.failed(getChannel(), recipient,
                    "Invalid email address format: " + recipient);
        }

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            log.debug("[EMAIL] Attempt {}/{} for '{}'", attempt, MAX_RETRIES, recipient);
            simulateSmtpDelay();

            // Simulate 10% transient SMTP failure (overcome with retry)
            if (Math.random() < 0.10 && attempt < MAX_RETRIES) {
                log.warn("[EMAIL] SMTP transient error on attempt {}. Retrying...", attempt);
                continue;
            }

            log.info("[EMAIL] Delivered to '{}' from '{}'", recipient, senderAddress);
            return NotificationResult.delivered(
                    getChannel(), recipient,
                    String.format("Email sent from %s to %s via %s:%d",
                            senderAddress, recipient, smtpHost, smtpPort)
            );
        }

        return NotificationResult.failed(getChannel(), recipient,
                "SMTP delivery failed after " + MAX_RETRIES + " attempts");
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public boolean isAvailable() {
        return smtpHost != null && !smtpHost.isBlank()
                && smtpPort > 0
                && senderAddress != null && senderAddress.contains("@");
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }

    private void simulateSmtpDelay() {
        try { Thread.sleep(30); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
