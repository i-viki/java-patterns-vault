package dev.jayav.patterns.factory;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable result of a notification delivery attempt.
 *
 * @param messageId     unique identifier for this notification
 * @param channel       the channel used for delivery
 * @param recipient     the destination address
 * @param status        delivery outcome
 * @param detail        human-readable delivery detail or error description
 * @param deliveredAt   UTC timestamp of the delivery attempt
 *
 * @author Jaya V
 * @version 1.0.0
 */
public record NotificationResult(
        String messageId,
        NotificationChannel channel,
        String recipient,
        DeliveryStatus status,
        String detail,
        Instant deliveredAt
) {

    /** Factory for a successful delivery result. */
    public static NotificationResult delivered(NotificationChannel channel,
                                               String recipient,
                                               String detail) {
        return new NotificationResult(
                UUID.randomUUID().toString().substring(0, 10).toUpperCase(),
                channel, recipient, DeliveryStatus.DELIVERED, detail, Instant.now()
        );
    }

    /** Factory for a failed delivery result. */
    public static NotificationResult failed(NotificationChannel channel,
                                            String recipient,
                                            String reason) {
        return new NotificationResult(
                "ERR-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                channel, recipient, DeliveryStatus.FAILED, reason, Instant.now()
        );
    }

    /** Returns true if this notification was delivered successfully. */
    public boolean isDelivered() {
        return status == DeliveryStatus.DELIVERED;
    }
}
