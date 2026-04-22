package dev.jayav.patterns.strategy;

/**
 * Runnable demonstration of the Strategy Pattern using a payment processing scenario.
 *
 * <p>Shows three key Strategy Pattern capabilities:
 * <ol>
 *   <li>Initial strategy assignment via constructor injection</li>
 *   <li>Runtime strategy hot-swap without changing calling code</li>
 *   <li>Audit trail accumulation across all payment attempts</li>
 * </ol>
 *
 * <p><b>Run:</b> {@code mvn -pl 01-strategy exec:java -Dexec.mainClass=dev.jayav.patterns.strategy.StrategyDemo}
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class StrategyDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║         Strategy Pattern — Payment Demo          ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        String orderId = "ORD-2024-88421";

        // ── Demo 1: Credit Card ───────────────────────────────────────────────
        System.out.println("▶ Step 1: User checks out with Credit Card");
        PaymentStrategy creditCard = new CreditCardStrategy(
                "Jaya V",
                "**** **** **** 4242",
                "09", "2027",
                true
        );

        PaymentContext context = new PaymentContext(creditCard, orderId);
        PaymentResult result1 = context.executePayment(249.99);
        printResult(result1);

        // ── Demo 2: Runtime swap → PayPal ─────────────────────────────────────
        System.out.println("\n▶ Step 2: User switches to PayPal at checkout (hot-swap)");
        PaymentStrategy paypal = new PayPalStrategy(
                "jayav@example.com",
                "Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.mock",
                500.00
        );
        context.setStrategy(paypal);
        PaymentResult result2 = context.executePayment(249.99);
        printResult(result2);

        // ── Demo 3: Runtime swap → Crypto ─────────────────────────────────────
        System.out.println("\n▶ Step 3: User pays with Bitcoin (Crypto)");
        PaymentStrategy crypto = new CryptoStrategy(
                "1A1zP1eP5QGefi2DMPTfTL5SLmv7Divf Na",  // mock BTC address (spaces stripped)
                "BTC",
                0.01  // 0.01 BTC ≈ $685 at simulated rate
        );
        // Fix: remove spaces from wallet address
        PaymentStrategy btc = new CryptoStrategy(
                "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
                "BTC",
                0.01
        );
        context.setStrategy(btc);
        PaymentResult result3 = context.executePayment(249.99);
        printResult(result3);

        // ── Audit Trail ───────────────────────────────────────────────────────
        System.out.println("\n══ Audit Trail for Order " + orderId + " ══");
        context.getAuditTrail().forEach(r ->
                System.out.printf("  [%s] %-12s $%7.2f  %s%n",
                        r.status(), r.strategyName(), r.amount(), r.transactionId())
        );
        System.out.println();
    }

    private static void printResult(PaymentResult result) {
        String icon = switch (result.status()) {
            case SUCCESS -> "✓";
            case FAILED  -> "✗";
            case PENDING -> "⏳";
        };
        System.out.printf("  %s [%s] %s%n", icon, result.status(), result.message());
    }
}
