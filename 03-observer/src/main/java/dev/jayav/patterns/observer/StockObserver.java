package dev.jayav.patterns.observer;

/**
 * Observer interface for receiving stock market price events.
 *
 * <p>Any component that needs to react to stock price changes implements
 * this interface and registers with a {@link StockMarket} subject.
 * The subject knows nothing about what observers do with events.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public interface StockObserver {

    /**
     * Called by the {@link StockMarket} when a stock price update occurs.
     *
     * <p>Implementations must be non-blocking. For heavy processing,
     * enqueue the event and handle it asynchronously.</p>
     *
     * @param event the immutable stock price event
     */
    void onStockUpdate(StockEvent event);

    /**
     * Returns the name of this observer (used for logging and diagnostics).
     *
     * @return human-readable observer name
     */
    String getObserverName();
}
