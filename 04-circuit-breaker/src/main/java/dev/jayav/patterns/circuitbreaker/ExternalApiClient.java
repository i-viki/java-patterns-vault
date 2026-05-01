package dev.jayav.patterns.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 * Simulates a flaky external API (e.g., a third-party inventory or pricing service).
 *
 * <p>The failure rate is configurable to simulate different load scenarios:
 * <ul>
 *   <li>0.0 — always succeeds (stable service)</li>
 *   <li>0.5 — 50% failure rate (degraded service)</li>
 *   <li>1.0 — always fails (down service)</li>
 * </ul>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class ExternalApiClient {

    private static final Logger log = LoggerFactory.getLogger(ExternalApiClient.class);

    private final String serviceUrl;
    private double failureRate;
    private final Random random = new Random();

    public ExternalApiClient(String serviceUrl, double failureRate) {
        this.serviceUrl  = serviceUrl;
        this.failureRate = failureRate;
    }

    /**
     * Simulates an API call that may fail based on the configured failure rate.
     *
     * @param requestId a unique identifier for this request (for logging)
     * @return a mock API response string
     * @throws RuntimeException if the simulated call fails
     */
    public String call(String requestId) {
        log.debug("[ExternalAPI] Calling {} for request '{}'", serviceUrl, requestId);

        // Simulate network latency (10-50ms)
        simulateLatency(10 + random.nextInt(40));

        if (random.nextDouble() < failureRate) {
            throw new RuntimeException(
                    "External API error for request '" + requestId + "': connection timeout");
        }

        return String.format("{\"requestId\":\"%s\",\"status\":\"ok\",\"data\":\"mock-response\"}",
                requestId);
    }

    /** Adjusts the failure rate at runtime (useful for demo scenarios). */
    public void setFailureRate(double rate) {
        this.failureRate = rate;
        log.info("[ExternalAPI] Failure rate updated to {}%", (int)(rate * 100));
    }

    public double getFailureRate() { return failureRate; }

    private void simulateLatency(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
