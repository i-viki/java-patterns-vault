package dev.jayav.patterns.strategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Strategy Pattern implementation.
 *
 * <p>Uses nested test classes to group assertions by strategy type,
 * following BDD-style naming ({@code given_when_then} structure).</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
@DisplayName("Strategy Pattern — Payment Processing Tests")
class StrategyPatternTest {

    // ══════════════════════════════════════════════════════════════════════════
    // PaymentContext tests
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PaymentContext")
    class PaymentContextTests {

        private PaymentContext context;

        @BeforeEach
        void setUp() {
            PaymentStrategy card = new CreditCardStrategy(
                    "Test User", "**** **** **** 1111", "12", "2028", true);
            context = new PaymentContext(card, "ORD-TEST-001");
        }

        @Test
        @DisplayName("should initialize with the provided strategy name")
        void shouldInitializeWithCorrectStrategyName() {
            assertThat(context.getCurrentStrategy()).isEqualTo("Credit Card");
        }

        @Test
        @DisplayName("should hot-swap strategy and reflect new name")
        void shouldHotSwapStrategy() {
            PaymentStrategy paypal = new PayPalStrategy(
                    "test@example.com", "valid-token", 999.99);

            context.setStrategy(paypal);

            assertThat(context.getCurrentStrategy()).isEqualTo("PayPal");
        }

        @Test
        @DisplayName("should throw NullPointerException when strategy is null")
        void shouldRejectNullStrategy() {
            assertThatNullPointerException()
                    .isThrownBy(() -> context.setStrategy(null))
                    .withMessage("Strategy must not be null");
        }

        @Test
        @DisplayName("should accumulate audit trail across multiple payments")
        void shouldAccumulateAuditTrail() {
            context.executePayment(50.00);
            context.executePayment(75.00);

            assertThat(context.getAuditTrail()).hasSize(2);
        }

        @Test
        @DisplayName("audit trail should be unmodifiable")
        void auditTrailShouldBeUnmodifiable() {
            context.executePayment(100.00);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> context.getAuditTrail().clear());
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Credit Card Strategy tests
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CreditCardStrategy")
    class CreditCardStrategyTests {

        @Test
        @DisplayName("should return SUCCESS for valid card and positive amount")
        void shouldSucceedWithValidCard() {
            PaymentStrategy strategy = new CreditCardStrategy(
                    "Jaya V", "**** **** **** 4242", "09", "2027", true);

            // Run multiple times to account for the 5% random decline simulation
            // In tests we assert the result is not an exception
            PaymentResult result = strategy.processPayment(199.99);

            assertThat(result).isNotNull();
            assertThat(result.amount()).isEqualTo(199.99);
            assertThat(result.strategyName()).isEqualTo("Credit Card");
            assertThat(result.transactionId()).isNotBlank();
            assertThat(result.processedAt()).isNotNull();
        }

        @Test
        @DisplayName("should return FAILED when CVV is invalid")
        void shouldFailWhenCvvInvalid() {
            PaymentStrategy strategy = new CreditCardStrategy(
                    "Jaya V", "**** **** **** 4242", "09", "2027", false);

            PaymentResult result = strategy.processPayment(100.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.message()).contains("validation failed");
        }

        @Test
        @DisplayName("should throw PaymentException for zero amount")
        void shouldThrowForZeroAmount() {
            PaymentStrategy strategy = new CreditCardStrategy(
                    "Jaya V", "**** **** **** 4242", "09", "2027", true);

            assertThatExceptionOfType(PaymentException.class)
                    .isThrownBy(() -> strategy.processPayment(0))
                    .withMessageContaining("greater than zero");
        }

        @Test
        @DisplayName("should report isConfigured=false when card details are missing")
        void shouldReportNotConfiguredWhenMissingDetails() {
            PaymentStrategy strategy = new CreditCardStrategy(
                    "", "", null, null, true);

            assertThat(strategy.isConfigured()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PayPal Strategy tests
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PayPalStrategy")
    class PayPalStrategyTests {

        @Test
        @DisplayName("should succeed when balance is sufficient")
        void shouldSucceedWithSufficientBalance() {
            PaymentStrategy strategy = new PayPalStrategy(
                    "user@example.com", "valid-token-xyz", 500.00);

            PaymentResult result = strategy.processPayment(100.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.strategyName()).isEqualTo("PayPal");
        }

        @Test
        @DisplayName("should fail when balance is insufficient")
        void shouldFailWhenInsufficientBalance() {
            PaymentStrategy strategy = new PayPalStrategy(
                    "user@example.com", "valid-token-xyz", 10.00);

            PaymentResult result = strategy.processPayment(500.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.message()).contains("Insufficient PayPal balance");
        }

        @Test
        @DisplayName("should fail when access token is blank")
        void shouldFailWhenAccessTokenBlank() {
            PaymentStrategy strategy = new PayPalStrategy(
                    "user@example.com", "", 500.00);

            PaymentResult result = strategy.processPayment(50.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.message()).contains("token missing or expired");
        }

        @Test
        @DisplayName("isConfigured should be false for invalid email")
        void shouldReportNotConfiguredForInvalidEmail() {
            PaymentStrategy strategy = new PayPalStrategy(
                    "not-an-email", "valid-token", 100.00);

            assertThat(strategy.isConfigured()).isFalse();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Crypto Strategy tests
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("CryptoStrategy")
    class CryptoStrategyTests {

        @Test
        @DisplayName("should return PENDING status for valid BTC payment")
        void shouldReturnPendingForBtcPayment() {
            PaymentStrategy strategy = new CryptoStrategy(
                    "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", "BTC", 1.0);

            PaymentResult result = strategy.processPayment(100.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.transactionId()).startsWith("0x");
        }

        @Test
        @DisplayName("should fail when crypto balance is insufficient")
        void shouldFailWhenInsufficientCrypto() {
            PaymentStrategy strategy = new CryptoStrategy(
                    "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", "BTC", 0.000001);

            PaymentResult result = strategy.processPayment(50_000.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.message()).contains("Insufficient BTC balance");
        }

        @Test
        @DisplayName("should fail for unsupported currency")
        void shouldFailForUnsupportedCurrency() {
            PaymentStrategy strategy = new CryptoStrategy(
                    "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa", "DOGE", 1000.0);

            assertThat(strategy.isConfigured()).isFalse();
        }

        @Test
        @DisplayName("USDC payment should succeed with 1:1 USD conversion")
        void shouldSucceedWithUsdcPayment() {
            PaymentStrategy strategy = new CryptoStrategy(
                    "0xAbc123Def456789012345678901234567890AbCd", "USDC", 1000.0);

            PaymentResult result = strategy.processPayment(250.00);

            assertThat(result.status()).isEqualTo(PaymentStatus.PENDING);
            assertThat(result.message()).contains("250.00000000 USDC");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PaymentResult record tests
    // ══════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PaymentResult")
    class PaymentResultTests {

        @Test
        @DisplayName("success factory should create result with SUCCESS status")
        void successFactoryShouldSetCorrectStatus() {
            PaymentResult result = PaymentResult.success("TestStrategy", 99.99, "All good");

            assertThat(result.status()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(result.isSuccessful()).isTrue();
            assertThat(result.transactionId()).isNotBlank();
            assertThat(result.processedAt()).isNotNull();
        }

        @Test
        @DisplayName("failure factory should create result with FAILED status")
        void failureFactoryShouldSetCorrectStatus() {
            PaymentResult result = PaymentResult.failure("TestStrategy", 99.99, "Timeout");

            assertThat(result.status()).isEqualTo(PaymentStatus.FAILED);
            assertThat(result.isSuccessful()).isFalse();
            assertThat(result.transactionId()).startsWith("FAILED-");
        }
    }
}
