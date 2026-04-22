package dev.jayav.patterns.strategy;

import java.time.Instant;
import java.util.UUID;

/**
 * Immutable value object representing the outcome of a payment operation.
 *
 * <p>Uses a Java 16+ {@code record} to ensure thread-safety and prevent
 * accidental mutation of audit-critical payment data.</p>
 *
 * @param transactionId  unique identifier for this payment transaction
 * @param strategyName   name of the strategy that processed the payment
 * @param amount         the amount that was processed (in USD)
 * @param status         outcome of the payment operation
 * @param message        human-readable description or error message
 * @param processedAt    UTC timestamp of when the payment was processed
 *
 * @author Jaya V
 * @version 1.0.0
 */
public record PaymentResult(
        String transactionId,
        String strategyName,
        double amount,
        PaymentStatus status,
        String message,
        Instant processedAt
) {

    /**
     * Factory method for creating a successful payment result.
     *
     * @param strategyName the strategy that handled the payment
     * @param amount       the processed amount
     * @param message      success detail message
     * @return a new {@code PaymentResult} with status {@link PaymentStatus#SUCCESS}
     */
    public static PaymentResult success(String strategyName, double amount, String message) {
        return new PaymentResult(
                UUID.randomUUID().toString().substring(0, 12).toUpperCase(),
                strategyName,
                amount,
                PaymentStatus.SUCCESS,
                message,
                Instant.now()
        );
    }

    /**
     * Factory method for creating a failed payment result.
     *
     * @param strategyName the strategy that attempted the payment
     * @param amount       the amount that failed to process
     * @param reason       the reason for failure
     * @return a new {@code PaymentResult} with status {@link PaymentStatus#FAILED}
     */
    public static PaymentResult failure(String strategyName, double amount, String reason) {
        return new PaymentResult(
                "FAILED-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                strategyName,
                amount,
                PaymentStatus.FAILED,
                reason,
                Instant.now()
        );
    }

    /**
     * Indicates whether this result represents a successful payment.
     *
     * @return {@code true} if {@link #status()} is {@link PaymentStatus#SUCCESS}
     */
    public boolean isSuccessful() {
        return status == PaymentStatus.SUCCESS;
    }
}
