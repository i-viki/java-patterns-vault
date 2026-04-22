package dev.jayav.patterns.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Context class that delegates payment processing to an injected {@link PaymentStrategy}.
 *
 * <p>The context is strategy-agnostic — it does not know or care which payment
 * method is being used. This is the core of the Strategy Pattern: the algorithm
 * (how to pay) is fully encapsulated behind the {@link PaymentStrategy} interface.</p>
 *
 * <h3>Key Responsibilities:</h3>
 * <ul>
 *   <li>Hold a reference to the current strategy</li>
 *   <li>Validate the strategy is configured before delegating</li>
 *   <li>Maintain an immutable audit trail of all transactions this session</li>
 *   <li>Allow hot-swap of strategies at runtime without restart</li>
 * </ul>
 *
 * <h3>Real-world scenario:</h3>
 * <pre>{@code
 * // At checkout, user selects PayPal
 * PaymentContext ctx = new PaymentContext(paypalStrategy, orderId);
 *
 * // User changes mind and switches to card
 * ctx.setStrategy(cardStrategy);
 *
 * // Context delegates — calling code never changes
 * PaymentResult result = ctx.executePayment(199.99);
 * }</pre>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class PaymentContext {

    private static final Logger log = LoggerFactory.getLogger(PaymentContext.class);

    private PaymentStrategy strategy;
    private final String orderId;
    private final List<PaymentResult> auditTrail;

    /**
     * Creates a PaymentContext with an initial strategy and order reference.
     *
     * @param initialStrategy the strategy to use initially (must not be null)
     * @param orderId         the unique order identifier for audit purposes
     * @throws NullPointerException if initialStrategy is null
     */
    public PaymentContext(PaymentStrategy initialStrategy, String orderId) {
        this.strategy = Objects.requireNonNull(initialStrategy, "Initial strategy must not be null");
        this.orderId = orderId;
        this.auditTrail = new ArrayList<>();
        log.debug("[PaymentContext] Initialized for order '{}' with strategy '{}'",
                orderId, initialStrategy.getStrategyName());
    }

    /**
     * Hot-swaps the current payment strategy at runtime.
     *
     * <p>This demonstrates the Strategy Pattern's core power: the algorithm
     * can change without touching any other part of the codebase.</p>
     *
     * @param newStrategy the new strategy to use (must not be null)
     */
    public void setStrategy(PaymentStrategy newStrategy) {
        Objects.requireNonNull(newStrategy, "Strategy must not be null");
        log.info("[PaymentContext] Order '{}': Switching strategy from '{}' → '{}'",
                orderId, this.strategy.getStrategyName(), newStrategy.getStrategyName());
        this.strategy = newStrategy;
    }

    /**
     * Executes the payment using the current strategy.
     *
     * <p>Pre-validates the strategy configuration before delegating.
     * The result is automatically appended to the session audit trail.</p>
     *
     * @param amount the amount to charge (in USD, must be &gt; 0)
     * @return the {@link PaymentResult} from the active strategy
     * @throws PaymentException if the strategy is not configured or the amount is invalid
     */
    public PaymentResult executePayment(double amount) {
        log.info("[PaymentContext] Order '{}': Executing payment of ${} via '{}'",
                orderId, amount, strategy.getStrategyName());

        if (!strategy.isConfigured()) {
            PaymentResult failure = PaymentResult.failure(
                    strategy.getStrategyName(), amount,
                    "Strategy not properly configured for order: " + orderId);
            auditTrail.add(failure);
            return failure;
        }

        PaymentResult result = strategy.processPayment(amount);
        auditTrail.add(result);

        if (result.isSuccessful()) {
            log.info("[PaymentContext] Order '{}': Payment SUCCEEDED. TxID: {}",
                    orderId, result.transactionId());
        } else {
            log.warn("[PaymentContext] Order '{}': Payment {} — {}",
                    orderId, result.status(), result.message());
        }

        return result;
    }

    /**
     * Returns the name of the currently active payment strategy.
     *
     * @return strategy name string (e.g., "Credit Card", "PayPal")
     */
    public String getCurrentStrategy() {
        return strategy.getStrategyName();
    }

    /**
     * Returns an unmodifiable view of all payment attempts made this session.
     * Useful for building audit logs, receipts, and retry mechanisms.
     *
     * @return immutable list of {@link PaymentResult} entries
     */
    public List<PaymentResult> getAuditTrail() {
        return Collections.unmodifiableList(auditTrail);
    }
}
