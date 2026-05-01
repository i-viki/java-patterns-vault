package dev.jayav.patterns.circuitbreaker;

/**
 * The three states of the Circuit Breaker Finite State Machine.
 *
 * <p>State transitions:
 * <pre>
 *      failures >= threshold
 * CLOSED ──────────────────► OPEN
 *   ▲                          │
 *   │    test call success     │ timeout elapsed
 *   │                          ▼
 *   └──────────────────── HALF_OPEN
 *        test call success
 * </pre>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public enum CircuitState {

    /**
     * Normal operation state. Requests are forwarded to the downstream service.
     * Transitions to OPEN when failure count reaches the configured threshold.
     */
    CLOSED,

    /**
     * Fault isolation state. All requests are immediately rejected with a fallback.
     * Transitions to HALF_OPEN after the configured timeout period elapses.
     */
    OPEN,

    /**
     * Recovery probe state. A single test request is allowed through.
     * Transitions to CLOSED on success, back to OPEN on failure.
     */
    HALF_OPEN
}
