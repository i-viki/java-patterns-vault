package dev.jayav.patterns.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Provides fallback responses when the Circuit Breaker is OPEN.
 *
 * <p>Graceful degradation is the core goal of the Circuit Breaker pattern —
 * rather than cascading failures, the system returns a cached/default response.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class FallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(FallbackHandler.class);

    private final String circuitName;
    private final Supplier<String> fallbackSupplier;

    /**
     * Creates a fallback handler for the named circuit.
     *
     * @param circuitName      the name of the circuit this fallback serves
     * @param fallbackSupplier a supplier returning the degraded response
     */
    public FallbackHandler(String circuitName, Supplier<String> fallbackSupplier) {
        this.circuitName      = circuitName;
        this.fallbackSupplier = fallbackSupplier;
    }

    /**
     * Executes the protected call via the circuit breaker, returning the fallback
     * response if the circuit is open.
     *
     * @param breaker   the circuit breaker to use
     * @param operation the operation to attempt
     * @return the operation result, or the fallback if the circuit is OPEN
     */
    public String executeWithFallback(CircuitBreaker<String> breaker,
                                      Supplier<String> operation) {
        try {
            return breaker.execute(operation);
        } catch (CircuitOpenException e) {
            String fallback = fallbackSupplier.get();
            log.warn("[Fallback:{}] Circuit is {} — using fallback: {}",
                    circuitName, e.getCurrentState(), fallback);
            return fallback;
        } catch (Exception e) {
            String fallback = fallbackSupplier.get();
            log.error("[Fallback:{}] Operation failed: {}. Using fallback.",
                    circuitName, e.getMessage());
            return fallback;
        }
    }

    public String getCircuitName() { return circuitName; }
}
