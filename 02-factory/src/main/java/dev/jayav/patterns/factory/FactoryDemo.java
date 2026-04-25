package dev.jayav.patterns.factory;

/**
 * Runnable demonstration of the Factory Pattern using multi-channel notifications.
 *
 * <p>Shows config-driven service creation where the calling code never touches
 * concrete notification classes — only the {@link NotificationChannel} enum
 * and the {@link NotificationService} interface.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class FactoryDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║      Factory Pattern — Notification Demo         ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // ── Demo: All channels via factory (no concrete class instantiation) ──
        String[][] scenarios = {
            { "EMAIL", "user@example.com",                           "Your order #ORD-88421 has shipped!" },
            { "SMS",   "+14155552671",                               "OTP: 847291. Valid for 5 mins." },
            { "PUSH",  "dGhpcyBpcyBhIGZha2UgZGV2aWNlIHRva2VuITEy",  "Flash Sale! 40% off ends in 1 hour." }
        };

        for (String[] scenario : scenarios) {
            NotificationChannel channel = NotificationChannel.valueOf(scenario[0]);
            String recipient = scenario[1];
            String message   = scenario[2];

            System.out.printf("▶ Sending via %-5s channel%n", channel);

            NotificationServiceFactory.create(channel).ifPresent(service -> {
                System.out.printf("  Factory created: %s (available: %s)%n",
                        service.getClass().getSimpleName(), service.isAvailable());

                NotificationResult result = service.send(recipient, message);
                System.out.printf("  [%s] %s%n%n", result.status(), result.detail());
            });
        }

        // ── Demo: createOrThrow for mandatory channels ─────────────────────
        System.out.println("▶ createOrThrow — mandatory channel resolution:");
        NotificationService emailService = NotificationServiceFactory.createOrThrow(NotificationChannel.EMAIL);
        System.out.printf("  Resolved: %s%n%n", emailService.getClass().getSimpleName());

        // ── Demo: graceful empty Optional for null channel ─────────────────
        System.out.println("▶ Optional.empty() returned for null channel:");
        boolean isEmpty = NotificationServiceFactory.create(null).isEmpty();
        System.out.printf("  Optional is empty: %s%n", isEmpty);
    }
}
