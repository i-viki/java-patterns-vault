package dev.jayav.patterns.circuitbreaker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Circuit Breaker FSM — verifies all 6 state transitions.
 *
 * @author Jaya V
 * @version 1.0.0
 */
@DisplayName("Circuit Breaker — FSM State Machine Tests")
class CircuitBreakerTest {

    private CircuitBreakerConfig config;
    private CircuitBreaker<String> breaker;

    @BeforeEach
    void setUp() {
        config = new CircuitBreakerConfig.Builder("test-circuit")
                .failureThreshold(3)
                .successThreshold(2)
                .openTimeoutMs(500)   // short timeout for test speed
                .build();
        breaker = new CircuitBreaker<>(config);
    }

    @Nested
    @DisplayName("CLOSED state (normal operation)")
    class ClosedStateTests {

        @Test
        @DisplayName("should start in CLOSED state")
        void shouldStartClosed() {
            assertThat(breaker.getState()).isEqualTo(CircuitState.CLOSED);
        }

        @Test
        @DisplayName("should execute operation when CLOSED")
        void shouldExecuteWhenClosed() throws Exception {
            String result = breaker.execute(() -> "success");

            assertThat(result).isEqualTo("success");
            assertThat(breaker.getState()).isEqualTo(CircuitState.CLOSED);
        }

        @Test
        @DisplayName("should reset failure count on success")
        void shouldResetFailureCountOnSuccess() throws Exception {
            // 2 failures then a success
            try { breaker.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
            try { breaker.execute(() -> { throw new RuntimeException("fail"); }); } catch (Exception ignored) {}
            breaker.execute(() -> "recovered");

            assertThat(breaker.getFailureCount()).isZero();
            assertThat(breaker.getState()).isEqualTo(CircuitState.CLOSED);
        }

        @Test
        @DisplayName("should trip to OPEN after reaching failure threshold")
        void shouldTripToOpenAfterThreshold() {
            tripCircuit(3);

            assertThat(breaker.getState()).isEqualTo(CircuitState.OPEN);
        }
    }

    @Nested
    @DisplayName("OPEN state (fault isolation)")
    class OpenStateTests {

        @BeforeEach
        void tripToOpen() {
            tripCircuit(3);
        }

        @Test
        @DisplayName("should reject calls immediately when OPEN")
        void shouldRejectCallsWhenOpen() {
            assertThatExceptionOfType(CircuitOpenException.class)
                    .isThrownBy(() -> breaker.execute(() -> "should-not-execute"))
                    .withMessageContaining("test-circuit")
                    .withMessageContaining("OPEN");
        }

        @Test
        @DisplayName("should transition to HALF_OPEN after timeout")
        void shouldTransitionToHalfOpenAfterTimeout() throws Exception {
            Thread.sleep(600); // wait past the 500ms timeout

            // Next call should trigger transition; it will throw CircuitOpenException
            // (HALF_OPEN) or succeed — either way state should change
            try {
                breaker.execute(() -> "probe");
            } catch (CircuitOpenException e) {
                // Expected: transitioning
            }

            // State should now be HALF_OPEN
            assertThat(breaker.getState()).isEqualTo(CircuitState.HALF_OPEN);
        }
    }

    @Nested
    @DisplayName("HALF_OPEN state (recovery probe)")
    class HalfOpenStateTests {

        @BeforeEach
        void tripToOpenAndWait() throws InterruptedException {
            tripCircuit(3);
            Thread.sleep(600); // wait for timeout
            // Trigger transition to HALF_OPEN
            try { breaker.execute(() -> "trigger"); } catch (Exception ignored) {}
        }

        @Test
        @DisplayName("should be in HALF_OPEN state after timeout")
        void shouldBeInHalfOpenState() {
            assertThat(breaker.getState()).isEqualTo(CircuitState.HALF_OPEN);
        }

        @Test
        @DisplayName("should transition to CLOSED after consecutive successes")
        void shouldCloseAfterSuccesses() throws Exception {
            // Need successThreshold (2) consecutive successes
            breaker.execute(() -> "probe-1");
            breaker.execute(() -> "probe-2");

            assertThat(breaker.getState()).isEqualTo(CircuitState.CLOSED);
            assertThat(breaker.getFailureCount()).isZero();
        }

        @Test
        @DisplayName("should return to OPEN on probe failure")
        void shouldReturnToOpenOnProbeFailure() {
            assertThatException()
                    .isThrownBy(() -> breaker.execute(() -> {
                        throw new RuntimeException("probe failed");
                    }));

            assertThat(breaker.getState()).isEqualTo(CircuitState.OPEN);
        }
    }

    @Nested
    @DisplayName("CircuitBreakerConfig")
    class ConfigTests {

        @Test
        @DisplayName("builder should use defaults when not specified")
        void shouldUseDefaults() {
            CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.defaultConfig("default");

            assertThat(defaultConfig.getFailureThreshold()).isEqualTo(5);
            assertThat(defaultConfig.getSuccessThreshold()).isEqualTo(2);
            assertThat(defaultConfig.getOpenTimeoutMs()).isEqualTo(10_000L);
        }

        @Test
        @DisplayName("should reject invalid failure threshold")
        void shouldRejectInvalidThreshold() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CircuitBreakerConfig.Builder("test")
                            .failureThreshold(0).build());
        }

        @Test
        @DisplayName("should reject null or blank name")
        void shouldRejectBlankName() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> new CircuitBreakerConfig.Builder(""));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void tripCircuit(int times) {
        for (int i = 0; i < times; i++) {
            try {
                breaker.execute(() -> { throw new RuntimeException("simulated failure"); });
            } catch (Exception ignored) {}
        }
    }
}
