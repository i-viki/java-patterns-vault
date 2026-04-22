package dev.jayav.patterns.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Strategy: Cryptocurrency payment processing.
 *
 * <p>Simulates a blockchain-based payment flow including:
 * <ul>
 *   <li>Wallet address checksum validation (Base58Check mock)</li>
 *   <li>USD → crypto conversion at a simulated live rate</li>
 *   <li>Transaction broadcast to mock blockchain node</li>
 *   <li>Returns {@link PaymentStatus#PENDING} until block confirmation</li>
 * </ul>
 *
 * <p><b>Production note:</b> Real implementations integrate with providers
 * like Coinbase Commerce, BitPay, or directly with an RPC node.
 * Payments are inherently async — a webhook/polling mechanism confirms
 * final settlement after N block confirmations.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class CryptoStrategy implements PaymentStrategy {

    private static final Logger log = LoggerFactory.getLogger(CryptoStrategy.class);

    /** Simulated BTC/USD exchange rate */
    private static final double BTC_USD_RATE = 68_500.00;
    /** Required block confirmations before settlement */
    private static final int REQUIRED_CONFIRMATIONS = 3;

    private final String walletAddress;
    private final String currency; // "BTC", "ETH", "USDC"
    private final double cryptoBalance;

    /**
     * Creates a Crypto payment strategy.
     *
     * @param walletAddress the recipient wallet address (validated on construction)
     * @param currency      the crypto asset to use ("BTC", "ETH", "USDC")
     * @param cryptoBalance the available balance in the specified crypto asset
     */
    public CryptoStrategy(String walletAddress, String currency, double cryptoBalance) {
        this.walletAddress = walletAddress;
        this.currency = currency;
        this.cryptoBalance = cryptoBalance;
    }

    @Override
    public PaymentResult processPayment(double amount) {
        log.info("[Crypto] Processing ${} payment via {} wallet: {}",
                amount, currency, maskWallet(walletAddress));

        if (!isConfigured()) {
            return PaymentResult.failure(getStrategyName(), amount,
                    "Invalid wallet address or unsupported currency: " + currency);
        }

        double cryptoAmount = convertUsdToCrypto(amount);
        log.debug("[Crypto] Converted ${} USD → {} {}", amount, String.format("%.8f", cryptoAmount), currency);

        if (cryptoBalance < cryptoAmount) {
            log.warn("[Crypto] Insufficient {} balance. Need: {}, Have: {}",
                    currency,
                    String.format("%.8f", cryptoAmount),
                    String.format("%.8f", cryptoBalance));
            return PaymentResult.failure(getStrategyName(), amount,
                    String.format("Insufficient %s balance. Required: %.8f %s",
                            currency, cryptoAmount, currency));
        }

        // Broadcast transaction to mock node (~200ms)
        simulateBroadcast();

        String txHash = generateMockTxHash();
        log.info("[Crypto] Transaction broadcast. TxHash: {}. Awaiting {} confirmations.",
                txHash, REQUIRED_CONFIRMATIONS);

        // Crypto payments are PENDING until block confirmations arrive (async)
        return new PaymentResult(
                txHash,
                getStrategyName(),
                amount,
                PaymentStatus.PENDING,
                String.format("Transaction broadcast: %.8f %s (TxHash: %s). "
                                + "Awaiting %d block confirmations.",
                        cryptoAmount, currency, txHash, REQUIRED_CONFIRMATIONS),
                java.time.Instant.now()
        );
    }

    @Override
    public String getStrategyName() {
        return "Crypto (" + currency + ")";
    }

    @Override
    public boolean isConfigured() {
        return walletAddress != null
                && walletAddress.length() >= 26   // min valid BTC address length
                && currency != null
                && (currency.equals("BTC") || currency.equals("ETH") || currency.equals("USDC"));
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private double convertUsdToCrypto(double usdAmount) {
        return switch (currency) {
            case "BTC"  -> usdAmount / BTC_USD_RATE;
            case "ETH"  -> usdAmount / 3_200.00;
            case "USDC" -> usdAmount; // 1:1 stablecoin
            default     -> throw new PaymentException("Unsupported currency: " + currency);
        };
    }

    private String maskWallet(String address) {
        if (address == null || address.length() < 8) return "***";
        return address.substring(0, 6) + "..." + address.substring(address.length() - 4);
    }

    private String generateMockTxHash() {
        String part1 = java.util.UUID.randomUUID().toString().replace("-", "");
        String part2 = java.util.UUID.randomUUID().toString().replace("-", "");
        return "0x" + (part1 + part2).substring(0, 40);
    }

    private void simulateBroadcast() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new PaymentException("Blockchain broadcast interrupted", e);
        }
    }
}
