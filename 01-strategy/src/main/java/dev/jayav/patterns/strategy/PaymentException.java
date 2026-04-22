package dev.jayav.patterns.strategy;

/**
 * Unchecked exception thrown when a payment strategy encounters an unrecoverable error.
 *
 * <p>Examples: network timeout communicating with payment gateway,
 * invalid card number format, or wallet address checksum failure.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class PaymentException extends RuntimeException {

    /**
     * Constructs a new {@code PaymentException} with a descriptive message.
     *
     * @param message the detail message
     */
    public PaymentException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code PaymentException} wrapping an underlying cause.
     *
     * @param message the detail message
     * @param cause   the root cause
     */
    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }
}
