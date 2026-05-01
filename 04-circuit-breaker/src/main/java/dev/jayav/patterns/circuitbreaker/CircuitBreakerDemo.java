package dev.jayav.patterns.circuitbreaker;

/**
 * Demonstrates the Circuit Breaker pattern with all three state transitions.
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class CircuitBreakerDemo {

    public static void main(String[] args) throws InterruptedException {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║     Circuit Breaker Pattern — FSM Demo           ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // Config: trip after 3 failures, recover with 2 successes, 2s timeout
        CircuitBreakerConfig config = new CircuitBreakerConfig.Builder("inventory-service")
                .failureThreshold(3)
                .successThreshold(2)
                .openTimeoutMs(2000)
                .build();

        CircuitBreaker<String> breaker = new CircuitBreaker<>(config);
        ExternalApiClient api = new ExternalApiClient("https://inventory.internal/api", 1.0); // always fail
        FallbackHandler fallback = new FallbackHandler("inventory-service",
                () -> "{\"status\":\"degraded\",\"inventory\":\"unavailable — using cache\"}");

        // ── Phase 1: Force failures → trip to OPEN ────────────────────────
        System.out.println("▶ Phase 1: Sending requests to a DOWN service (failure rate 100%)");
        for (int i = 1; i <= 5; i++) {
            final int reqId = i;
            String result = fallback.executeWithFallback(breaker, () -> api.call("req-" + reqId));
            System.out.printf("  [%d] State: %-9s | Result: %s%n",
                    i, breaker.getState(), truncate(result, 60));
        }

        // ── Phase 2: Circuit is OPEN — all requests blocked ───────────────
        System.out.println("\n▶ Phase 2: Circuit is OPEN — requests blocked, fallback served");
        for (int i = 1; i <= 3; i++) {
            final int reqId = i;
            String result = fallback.executeWithFallback(breaker, () -> api.call("blocked-" + reqId));
            System.out.printf("  [%d] State: %-9s | Result: %s%n",
                    i, breaker.getState(), truncate(result, 60));
        }

        // ── Phase 3: Wait for timeout → HALF_OPEN → CLOSED ───────────────
        System.out.println("\n▶ Phase 3: Waiting 2.5s for timeout → HALF_OPEN recovery...");
        Thread.sleep(2500);
        api.setFailureRate(0.0); // service recovered

        System.out.println("  Service recovered (failure rate: 0%). Sending probe requests:");
        for (int i = 1; i <= 4; i++) {
            final int reqId = i;
            String result = fallback.executeWithFallback(breaker, () -> api.call("probe-" + reqId));
            System.out.printf("  [%d] State: %-9s | Result: %s%n",
                    i, breaker.getState(), truncate(result, 60));
        }

        System.out.printf("%n✓ Final state: %s (failures: %d)%n",
                breaker.getState(), breaker.getFailureCount());
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "null";
        return s.length() <= maxLen ? s : s.substring(0, maxLen) + "...";
    }
}
