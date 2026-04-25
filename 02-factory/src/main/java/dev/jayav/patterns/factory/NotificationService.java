package dev.jayav.patterns.factory;

/**
 * Strategy interface for all notification delivery channels.
 *
 * <p>Each concrete notification service (Email, SMS, Push) knows its own
 * delivery mechanism. The factory creates these without exposing the
 * concrete types to the calling code.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public interface NotificationService {

    /**
     * Sends a notification to the specified recipient.
     *
     * @param recipient the destination address (email, phone number, device token, etc.)
     * @param message   the content to deliver
     * @return a {@link NotificationResult} with delivery status and metadata
     */
    NotificationResult send(String recipient, String message);

    /**
     * Returns the delivery channel this service handles.
     *
     * @return the {@link NotificationChannel} enum constant
     */
    NotificationChannel getChannel();

    /**
     * Returns whether this service is operational and configured.
     *
     * @return {@code true} if the service can accept messages
     */
    boolean isAvailable();
}
