package dev.jayav.patterns.factory;

import java.util.Optional;

/**
 * Static Factory Provider — creates the correct {@link NotificationService}
 * based on the requested {@link NotificationChannel}.
 *
 * <p>This is the Factory Pattern's key value: callers never instantiate
 * concrete classes directly. Configuration changes (e.g., switching from
 * Twilio to Vonage for SMS) are isolated entirely to this class.</p>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * Optional<NotificationService> svc =
 *     NotificationServiceFactory.create(NotificationChannel.EMAIL);
 *
 * svc.ifPresent(s -> s.send("user@example.com", "Welcome!"));
 * }</pre>
 *
 * <p><b>Production note:</b> In a Spring application, this would be replaced
 * by a {@code @Configuration} class injecting services via DI, but the
 * factory concept is identical.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public final class NotificationServiceFactory {

    // ── Default configuration constants (would come from config/env in production) ──
    private static final String SMTP_HOST    = "smtp.internal.corp.com";
    private static final int    SMTP_PORT    = 587;
    private static final String SENDER_EMAIL = "noreply@jayav.dev";

    private static final String SMS_GATEWAY_URL = "https://api.twilio.com/2010-04-01";
    private static final String SMS_API_KEY     = "ACXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";
    private static final String SMS_FROM_NUMBER = "+14155552671";

    private static final String FCM_SERVER_KEY = "AAAAxxxxxxxx:APA91bHxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    private static final String APP_NAME       = "PatternVault";

    /** Private constructor — utility class, not instantiable. */
    private NotificationServiceFactory() {
        throw new UnsupportedOperationException("Factory class — do not instantiate");
    }

    /**
     * Creates and returns a {@link NotificationService} for the given channel.
     *
     * @param channel the desired notification delivery channel
     * @return an {@link Optional} containing the service, or empty if channel is unknown
     */
    public static Optional<NotificationService> create(NotificationChannel channel) {
        if (channel == null) return Optional.empty();

        return switch (channel) {
            case EMAIL -> Optional.of(new EmailNotification(SMTP_HOST, SMTP_PORT, SENDER_EMAIL));
            case SMS   -> Optional.of(new SmsNotification(SMS_GATEWAY_URL, SMS_API_KEY, SMS_FROM_NUMBER));
            case PUSH  -> Optional.of(new PushNotification(FCM_SERVER_KEY, APP_NAME));
        };
    }

    /**
     * Creates a {@link NotificationService} or throws if the channel is unsupported.
     *
     * <p>Use this when you require a service and an absent channel indicates a
     * programming error (e.g., missing enum case), not a runtime condition.</p>
     *
     * @param channel the desired notification delivery channel
     * @return the configured {@link NotificationService}
     * @throws IllegalArgumentException if the channel is null or unmapped
     */
    public static NotificationService createOrThrow(NotificationChannel channel) {
        return create(channel).orElseThrow(() ->
                new IllegalArgumentException("No notification service registered for channel: " + channel));
    }
}
