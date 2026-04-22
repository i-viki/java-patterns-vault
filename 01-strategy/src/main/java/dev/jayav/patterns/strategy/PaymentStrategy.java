package dev.jayav.patterns.strategy;

/**
 * Strategy interface defining the contract for all payment processing algorithms.
 *
 * <p>Each concrete strategy encapsulates a distinct payment method (Credit Card,
 * PayPal, Crypto, etc.) and can be swapped at runtime via {@link PaymentContext}
 * without any change to the calling code — demonstrating the Open/Closed Principle.</p>
 *
 * <p><b>Production relevance:</b> Used in e-commerce platforms where payment
 * providers change based on user location, subscription tier, or A/B tests.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public interface PaymentStrategy {

    /**
     * Processes a payment of the given amount using this strategy's mechanism.
     *
     * @param amount the payment amount in USD (must be &gt; 0)
     * @return a {@link PaymentResult} containing status, transaction ID, and metadata
     * @throws PaymentException if the payment cannot be processed
     */
    PaymentResult processPayment(double amount);

    /**
     * Returns the human-readable name of this payment strategy.
     * Used for logging and audit trail generation.
     *
     * @return the strategy name (e.g., "Credit Card", "PayPal")
     */
    String getStrategyName();

    /**
     * Validates that this strategy is correctly configured before processing.
     * Called by {@link PaymentContext} as a pre-condition check.
     *
     * @return {@code true} if the strategy is ready to process payments
     */
    boolean isConfigured();
}
