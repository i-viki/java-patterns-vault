package dev.jayav.patterns.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Concrete Factory Product: Push notification via FCM (Firebase Cloud Messaging) mock.
 *
 * <p>Builds a structured FCM-style JSON payload and simulates delivery to
 * a device token. Supports notification metadata (title, body, icon) and
 * optional data payload for deep linking.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class PushNotification implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(PushNotification.class);

    private final String fcmServerKey;
    private final String appName;

    public PushNotification(String fcmServerKey, String appName) {
        this.fcmServerKey = fcmServerKey;
        this.appName = appName;
    }

    @Override
    public NotificationResult send(String deviceToken, String message) {
        log.info("[PUSH] Sending notification to device token: {}...", maskToken(deviceToken));

        if (!isValidDeviceToken(deviceToken)) {
            return NotificationResult.failed(getChannel(), deviceToken,
                    "Invalid FCM device token format");
        }

        // Build FCM-style payload
        Map<String, Object> payload = buildFcmPayload(deviceToken, message);
        log.debug("[PUSH] FCM payload: {}", payload);

        simulateFcmDelivery();

        log.info("[PUSH] Push notification delivered to app '{}', token: {}...",
                appName, maskToken(deviceToken));

        return NotificationResult.delivered(
                getChannel(), deviceToken,
                String.format("Push delivered to '%s' app on device %s (FCM)",
                        appName, maskToken(deviceToken))
        );
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.PUSH;
    }

    @Override
    public boolean isAvailable() {
        return fcmServerKey != null && fcmServerKey.length() > 10
                && appName != null && !appName.isBlank();
    }

    private Map<String, Object> buildFcmPayload(String token, String message) {
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", appName + " Notification");
        notification.put("body", message);
        notification.put("icon", "ic_notification");

        Map<String, Object> payload = new HashMap<>();
        payload.put("to", token);
        payload.put("notification", notification);
        payload.put("priority", "high");
        return payload;
    }

    private boolean isValidDeviceToken(String token) {
        return token != null && token.length() >= 32 && !token.isBlank();
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "***";
        return token.substring(0, 8) + "...";
    }

    private void simulateFcmDelivery() {
        try { Thread.sleep(60); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
