package dev.jayav.patterns.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * Thread-safe Circuit Breaker implementation using a Finite State Machine.
 *
 * <h3>States and Transitions:</h3>
 * <pre>
 *      failures >= failureThreshold
 * CLOSED ─────────────────────────► OPEN
 *   ▲                                 │
 *   │  consecutive successes          │ openTimeoutMs elapsed
 *   │  >= successThreshold            ▼
 *   └──────────────────────────── HALF_OPEN
 * </pre>
 *
 * <h3>Thread Safety:</h3>
 * <ul>
 *   <li>{@link AtomicReference} for the state — ensures single atomic state swap</li>
 *   <li>{@link AtomicInteger} for counters — no lock contention on hot path</li>
 *   <li>{@link AtomicLong} for the open timestamp — visible across threads</li>
 * </ul>
 *
 * @param <T> the return type of the protected operation
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class CircuitBreaker<T> {

    private static final Logger log = LoggerFactory.getLogger(CircuitBreaker.class);

    private final CircuitBreakerConfig config;
    private final AtomicReference<CircuitState> state;
    private final AtomicInteger failureCount;
    private final AtomicInteger successCount;   // consecutive successes in HALF_OPEN
    private final AtomicLong    openedAt;       // epoch ms when circuit opened

    public CircuitBreaker(CircuitBreakerConfig config) {
        this.config       = config;
        this.state        = new AtomicReference<>(CircuitState.CLOSED);
        this.failureCount = new AtomicInteger(0);
        this.successCount = new AtomicInteger(0);
        this.openedAt     = new AtomicLong(0);

        log.info("[CircuitBreaker:{}] Initialized. Config: {}", config.getName(), config);
    }

    /**
     * Executes the supplied operation through the circuit breaker.
     *
     * <p>Depending on the current state:
     * <ul>
     *   <li>{@link CircuitState#CLOSED} — executes normally, tracks failures</li>
     *   <li>{@link CircuitState#OPEN}   — immediately throws {@link CircuitOpenException}</li>
     *   <li>{@link CircuitState#HALF_OPEN} — allows one probe call through</li>
     * </ul>
     *
     * @param operation the operation to execute
     * @return the result of the operation
     * @throws CircuitOpenException if the circuit is OPEN
     * @throws Exception if the operation itself fails
     */
    public T execute(Supplier<T> operation) throws Exception {
        CircuitState currentState = state.get();

        return switch (currentState) {
            case OPEN     -> handleOpenState();
            case HALF_OPEN -> executeProbe(operation);
            case CLOSED    -> executeNormal(operation);
        };
    }

    /** Returns the current state of this circuit breaker. */
    public CircuitState getState() {
        return state.get();
    }

    /** Returns the current failure count. */
    public int getFailureCount() {
        return failureCount.get();
    }

    // ── State Handlers ────────────────────────────────────────────────────────

    private T handleOpenState() {
        long elapsedMs = System.currentTimeMillis() - openedAt.get();

        if (elapsedMs >= config.getOpenTimeoutMs()) {
            // Timeout elapsed — attempt transition to HALF_OPEN
            if (state.compareAndSet(CircuitState.OPEN, CircuitState.HALF_OPEN)) {
                successCount.set(0);
                log.info("[CircuitBreaker:{}] Timeout elapsed ({}ms). Transitioning OPEN → HALF_OPEN",
                        config.getName(), elapsedMs);
            }
            // Now in HALF_OPEN — this call will be handled on next invoke
            throw new CircuitOpenException(config.getName(), CircuitState.HALF_OPEN);
        }

        log.debug("[CircuitBreaker:{}] OPEN. Rejecting call. Timeout remaining: {}ms",
                config.getName(), config.getOpenTimeoutMs() - elapsedMs);
        throw new CircuitOpenException(config.getName(), CircuitState.OPEN);
    }

    private T executeProbe(Supplier<T> operation) throws Exception {
        log.info("[CircuitBreaker:{}] HALF_OPEN — sending probe request", config.getName());
        try {
            T result = operation.get();
            onProbeSuccess();
            return result;
        } catch (Exception e) {
            onProbeFailure(e);
            throw e;
        }
    }

    private T executeNormal(Supplier<T> operation) throws Exception {
        try {
            T result = operation.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure(e);
            throw e;
        }
    }

    // ── Event Handlers ────────────────────────────────────────────────────────

    private void onSuccess() {
        failureCount.set(0); // reset on any success
    }

    private void onFailure(Exception e) {
        int failures = failureCount.incrementAndGet();
        log.warn("[CircuitBreaker:{}] Failure #{}: {}", config.getName(), failures, e.getMessage());

        if (failures >= config.getFailureThreshold()) {
            if (state.compareAndSet(CircuitState.CLOSED, CircuitState.OPEN)) {
                openedAt.set(System.currentTimeMillis());
                log.error("[CircuitBreaker:{}] TRIPPED! Transitioning CLOSED → OPEN after {} failures",
                        config.getName(), failures);
            }
        }
    }

    private void onProbeSuccess() {
        int successes = successCount.incrementAndGet();
        log.info("[CircuitBreaker:{}] Probe success #{}/{}", config.getName(),
                successes, config.getSuccessThreshold());

        if (successes >= config.getSuccessThreshold()) {
            if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.CLOSED)) {
                failureCount.set(0);
                successCount.set(0);
                log.info("[CircuitBreaker:{}] RECOVERED! Transitioning HALF_OPEN → CLOSED",
                        config.getName());
            }
        }
    }

    private void onProbeFailure(Exception e) {
        log.warn("[CircuitBreaker:{}] Probe FAILED: {}. Returning HALF_OPEN → OPEN",
                config.getName(), e.getMessage());
        if (state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN)) {
            openedAt.set(System.currentTimeMillis());
            failureCount.set(config.getFailureThreshold()); // reset to threshold
        }
    }
}
