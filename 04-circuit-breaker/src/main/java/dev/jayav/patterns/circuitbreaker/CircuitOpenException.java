package dev.jayav.patterns.circuitbreaker;

/**
 * Thrown when a request is rejected because the circuit is in {@link CircuitState#OPEN}
 * or transitioning through {@link CircuitState#HALF_OPEN}.
 *
 * <p>Callers should catch this exception and use a fallback strategy rather
 * than propagating it to end users.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class CircuitOpenException extends RuntimeException {

    private final String circuitName;
    private final CircuitState currentState;

    public CircuitOpenException(String circuitName, CircuitState currentState) {
        super(String.format("Circuit '%s' is %s — request rejected", circuitName, currentState));
        this.circuitName  = circuitName;
        this.currentState = currentState;
    }

    public String getCircuitName()       { return circuitName; }
    public CircuitState getCurrentState() { return currentState; }
}
