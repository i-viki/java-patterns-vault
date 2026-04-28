package dev.jayav.patterns.observer;

import java.time.Instant;

/**
 * Immutable event record representing a stock price change.
 *
 * <p>Using a Java {@code record} guarantees immutability — all observers
 * receive the same snapshot with no risk of mutation mid-delivery.</p>
 *
 * @param ticker      stock ticker symbol (e.g., "AAPL", "NVDA")
 * @param price       new price in USD
 * @param previousPrice price before this update
 * @param occurredAt  UTC timestamp of the price change
 *
 * @author Jaya V
 * @version 1.0.0
 */
public record StockEvent(
        String ticker,
        double price,
        double previousPrice,
        Instant occurredAt
) {
    /**
     * Calculates the percentage change from the previous price.
     *
     * @return positive value for gains, negative for losses
     */
    public double changePercent() {
        if (previousPrice == 0) return 0;
        return ((price - previousPrice) / previousPrice) * 100;
    }

    /** Returns {@code true} if the price went up from the previous value. */
    public boolean isGain() {
        return price > previousPrice;
    }

    /** Returns {@code true} if the price went down from the previous value. */
    public boolean isLoss() {
        return price < previousPrice;
    }

    @Override
    public String toString() {
        return String.format("StockEvent[%s: $%.2f (%+.2f%%)]",
                ticker, price, changePercent());
    }
}
