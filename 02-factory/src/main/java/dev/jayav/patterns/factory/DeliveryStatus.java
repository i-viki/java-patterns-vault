package dev.jayav.patterns.factory;

/**
 * Possible outcomes of a notification delivery attempt.
 *
 * @author Jaya V
 * @version 1.0.0
 */
public enum DeliveryStatus {
    /** Notification was accepted and delivered by the channel provider. */
    DELIVERED,
    /** Notification failed to deliver (gateway error, invalid recipient, etc.). */
    FAILED,
    /** Notification queued for delivery — awaiting provider confirmation. */
    QUEUED
}
