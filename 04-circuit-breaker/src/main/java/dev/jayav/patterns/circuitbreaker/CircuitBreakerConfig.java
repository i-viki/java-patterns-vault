package dev.jayav.patterns.circuitbreaker;

/**
 * Immutable configuration for a {@link CircuitBreaker} instance.
 *
 * <p>Built via the Builder pattern to provide named, self-documenting
 * configuration — preferred in production over constructor argument lists.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public final class CircuitBreakerConfig {

    private final int    failureThreshold;    // failures before tripping to OPEN
    private final int    successThreshold;    // successes in HALF_OPEN to close
    private final long   openTimeoutMs;       // ms to wait in OPEN before trying HALF_OPEN
    private final String name;               // for logging/metrics

    private CircuitBreakerConfig(Builder builder) {
        this.failureThreshold = builder.failureThreshold;
        this.successThreshold = builder.successThreshold;
        this.openTimeoutMs    = builder.openTimeoutMs;
        this.name             = builder.name;
    }

    public int    getFailureThreshold() { return failureThreshold; }
    public int    getSuccessThreshold() { return successThreshold; }
    public long   getOpenTimeoutMs()    { return openTimeoutMs; }
    public String getName()             { return name; }

    /** Creates a default config suitable for most external service calls. */
    public static CircuitBreakerConfig defaultConfig(String name) {
        return new Builder(name).build();
    }

    @Override
    public String toString() {
        return String.format("CircuitBreakerConfig[name=%s, failureThreshold=%d, "
                + "successThreshold=%d, timeout=%dms]",
                name, failureThreshold, successThreshold, openTimeoutMs);
    }

    // ── Builder ───────────────────────────────────────────────────────────

    public static final class Builder {
        private final String name;
        private int  failureThreshold = 5;        // default: open after 5 failures
        private int  successThreshold = 2;        // default: close after 2 successes
        private long openTimeoutMs    = 10_000L;  // default: 10 seconds in OPEN

        public Builder(String name) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Name required");
            this.name = name;
        }

        /** Number of consecutive failures before the circuit trips to OPEN. */
        public Builder failureThreshold(int threshold) {
            if (threshold < 1) throw new IllegalArgumentException("Threshold must be >= 1");
            this.failureThreshold = threshold;
            return this;
        }

        /** Number of consecutive successes in HALF_OPEN to transition to CLOSED. */
        public Builder successThreshold(int threshold) {
            if (threshold < 1) throw new IllegalArgumentException("Threshold must be >= 1");
            this.successThreshold = threshold;
            return this;
        }

        /** Milliseconds to remain in OPEN before probing with HALF_OPEN. */
        public Builder openTimeoutMs(long timeoutMs) {
            if (timeoutMs < 0) throw new IllegalArgumentException("Timeout must be >= 0");
            this.openTimeoutMs = timeoutMs;
            return this;
        }

        public CircuitBreakerConfig build() {
            return new CircuitBreakerConfig(this);
        }
    }
}
