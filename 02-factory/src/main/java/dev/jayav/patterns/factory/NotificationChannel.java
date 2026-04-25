package dev.jayav.patterns.factory;

/**
 * Enum representing the supported notification delivery channels.
 *
 * <p>Used as the factory discriminator — the factory provider uses this
 * enum to determine which concrete {@link NotificationService} to instantiate,
 * eliminating magic strings and enabling compile-time safety.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public enum NotificationChannel {

    /** Delivery via SMTP email protocol */
    EMAIL,

    /** Delivery via SMS gateway (Twilio, Nexmo, etc.) */
    SMS,

    /** Delivery via Firebase Cloud Messaging (FCM) or APNs push */
    PUSH
}
