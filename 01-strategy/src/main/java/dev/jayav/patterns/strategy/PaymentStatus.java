package dev.jayav.patterns.strategy;

/**
 * Enum representing all possible outcomes of a payment processing operation.
 *
 * @author Jaya V
 * @version 1.0.0
 */
public enum PaymentStatus {

    /** Payment was processed and funds were captured successfully. */
    SUCCESS,

    /** Payment attempt failed due to gateway error, insufficient funds, etc. */
    FAILED,

    /** Payment is awaiting external confirmation (e.g. blockchain confirmation). */
    PENDING
}
