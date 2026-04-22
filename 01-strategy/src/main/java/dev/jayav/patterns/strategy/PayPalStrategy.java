package dev.jayav.patterns.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Strategy: PayPal payment processing.
 *
 * <p>Simulates OAuth-based PayPal integration including:
 * <ul>
 *   <li>Access token validation (simulated OAuth 2.0 exchange)</li>
 *   <li>Balance check before charge</li>
 *   <li>PayPal Orders API v2 mock call</li>
 * </ul>
 *
 * <p><b>Production note:</b> In real systems, this integrates with
 * {@code api.paypal.com/v2/checkout/orders} and handles IPN webhooks
 * for asynchronous confirmation.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class PayPalStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(PayPalStrategy.class);
    private static final double PAYPAL_FEE_RATE = 0.029; // 2.9% standard fee
    private static final double PAYPAL_FIXED_FEE = 0.30;  // $0.30 per transaction

    private final String paypalEmail;
    private final String accessToken; // OAuth 2.0 bearer token
    private final double accountBalance;

    /**
     * Creates a PayPal strategy with an authenticated session.
     *
     * @param paypalEmail    the verified PayPal account email
     * @param accessToken    a valid OAuth 2.0 access token (never null or empty)
     * @param accountBalance simulated available PayPal balance in USD
     */
    public PayPalStrategy(String paypalEmail, String accessToken, double accountBalance) {
        this.paypalEmail = paypalEmail;
        this.accessToken = accessToken;
        this.accountBalance = accountBalance;
    }

    @Override
    public PaymentResult processPayment(double amount) {
        log.info("[PayPal] Initiating payment of ${} from account '{}'", amount, paypalEmail);

        if (!isConfigured()) {
            return PaymentResult.failure(getStrategyName(), amount,
                    "PayPal session invalid — access token missing or expired");
        }

        if (amount <= 0) {
            throw new PaymentException("Payment amount must be greater than zero, got: " + amount);
        }

        double fee = (amount * PAYPAL_FEE_RATE) + PAYPAL_FIXED_FEE;
        double totalDebited = amount + fee;

        if (accountBalance < totalDebited) {
            log.warn("[PayPal] Insufficient balance: need ${}, have ${}", totalDebited, accountBalance);
            return PaymentResult.failure(getStrategyName(), amount,
                    String.format("Insufficient PayPal balance. Required: $%.2f (incl. fee: $%.2f)",
                            totalDebited, fee));
        }

        // Simulate PayPal Orders API v2 call latency (~120ms)
        simulateApiCall(120);

        log.info("[PayPal] Charge of ${} completed. Fee: ${}, Total: ${}",
                amount, String.format("%.2f", fee), String.format("%.2f", totalDebited));

        return PaymentResult.success(
                getStrategyName(),
                amount,
                String.format("PayPal charge of $%.2f completed (fee: $%.2f) for %s",
                        amount, fee, paypalEmail)
        );
    }

    @Override
    public String getStrategyName() {
        return "PayPal";
    }

    @Override
    public boolean isConfigured() {
        return accessToken != null && !accessToken.isBlank()
                && paypalEmail != null && paypalEmail.contains("@");
    }

    private void simulateApiCall(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("PayPal API call interrupted", e);
        }
    }
}
