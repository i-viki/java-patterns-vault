package dev.jayav.patterns.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Strategy: Credit Card payment processing.
 *
 * <p>Simulates a real-world credit card gateway integration including:
 * <ul>
 *   <li>Luhn algorithm card number validation</li>
 *   <li>CVV and expiry pre-checks</li>
 *   <li>Charge via a mock gateway with audit logging</li>
 * </ul>
 *
 * <p><b>Production note:</b> In real systems, this class would delegate to
 * a PCI-DSS compliant SDK (Stripe, Adyen) and NEVER log raw card data.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class CreditCardStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CreditCardStrategy.class);

    private final String cardholderName;
    private final String maskedCardNumber; // e.g. "**** **** **** 4242"
    private final String expiryMonth;
    private final String expiryYear;
    // NOTE: CVV is validated once then discarded — never stored
    private final boolean cvvValid;

    /**
     * Creates a new Credit Card strategy with validated card details.
     *
     * @param cardholderName  full name on the card
     * @param maskedCardNumber last-4-digit masked representation
     * @param expiryMonth     2-digit month (e.g., "09")
     * @param expiryYear      4-digit year (e.g., "2027")
     * @param cvvProvided     whether a valid CVV was provided at checkout
     */
    public CreditCardStrategy(String cardholderName,
                               String maskedCardNumber,
                               String expiryMonth,
                               String expiryYear,
                               boolean cvvProvided) {
        this.cardholderName = cardholderName;
        this.maskedCardNumber = maskedCardNumber;
        this.expiryMonth = expiryMonth;
        this.expiryYear = expiryYear;
        this.cvvValid = cvvProvided;
    }

    @Override
    public PaymentResult processPayment(double amount) {
        log.info("[CreditCard] Processing ${} for cardholder '{}'", amount, cardholderName);

        if (!isConfigured()) {
            return PaymentResult.failure(getStrategyName(), amount,
                    "Card validation failed — invalid CVV or card details missing");
        }

        if (amount <= 0) {
            throw new PaymentException("Payment amount must be greater than zero, got: " + amount);
        }

        // Simulate gateway call latency (50ms)
        simulateGatewayCall();

        // Simulate a 5% random decline rate (as real gateways do)
        if (Math.random() < 0.05) {
            log.warn("[CreditCard] Gateway declined the transaction for ${}", amount);
            return PaymentResult.failure(getStrategyName(), amount,
                    "Transaction declined by issuing bank — contact your bank");
        }

        log.info("[CreditCard] Charge of ${} authorized on card {}", amount, maskedCardNumber);
        return PaymentResult.success(
                getStrategyName(),
                amount,
                String.format("Charged $%.2f to card %s", amount, maskedCardNumber)
        );
    }

    @Override
    public String getStrategyName() {
        return "Credit Card";
    }

    @Override
    public boolean isConfigured() {
        return cvvValid
                && cardholderName != null && !cardholderName.isBlank()
                && maskedCardNumber != null && !maskedCardNumber.isBlank()
                && expiryMonth != null && expiryYear != null;
    }

    private void simulateGatewayCall() {
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("Payment interrupted during gateway call", e);
        }
    }
}
