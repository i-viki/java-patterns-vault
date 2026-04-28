package dev.jayav.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Subject (Observable) — the stock market that publishes price change events.
 *
 * <p>Uses {@link CopyOnWriteArrayList} for the observer list, which is the
 * correct production choice for observer patterns because:
 * <ul>
 *   <li>Reads (notification iteration) vastly outnumber writes (subscribe/unsubscribe)</li>
 *   <li>Eliminates {@link java.util.ConcurrentModificationException} if an observer
 *       unsubscribes itself during event delivery</li>
 *   <li>Fully thread-safe without explicit synchronization on dispatch</li>
 * </ul>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class StockMarket {

    private static final Logger log = LoggerFactory.getLogger(StockMarket.class);

    private final String marketName;
    private final List<StockObserver> observers = new CopyOnWriteArrayList<>();

    public StockMarket(String marketName) {
        this.marketName = marketName;
    }

    /**
     * Subscribes an observer to receive future stock price events.
     *
     * <p>Duplicate registrations are silently ignored.</p>
     *
     * @param observer the observer to register (must not be null)
     */
    public void subscribe(StockObserver observer) {
        if (observer == null) throw new IllegalArgumentException("Observer must not be null");
        if (!observers.contains(observer)) {
            observers.add(observer);
            log.debug("[{}] Observer '{}' subscribed. Total: {}",
                    marketName, observer.getObserverName(), observers.size());
        }
    }

    /**
     * Unsubscribes an observer — it will no longer receive events.
     *
     * @param observer the observer to remove
     */
    public void unsubscribe(StockObserver observer) {
        boolean removed = observers.remove(observer);
        if (removed) {
            log.debug("[{}] Observer '{}' unsubscribed. Total: {}",
                    marketName, observer.getObserverName(), observers.size());
        }
    }

    /**
     * Publishes a stock price update and notifies all registered observers.
     *
     * <p>Notification is synchronous and in subscription order.
     * Observer exceptions are caught and logged to prevent one failing
     * observer from blocking others — critical for production resilience.</p>
     *
     * @param ticker        the stock ticker symbol
     * @param newPrice      the new price in USD
     * @param previousPrice the price before this update
     */
    public void publishPriceUpdate(String ticker, double newPrice, double previousPrice) {
        StockEvent event = new StockEvent(ticker, newPrice, previousPrice, Instant.now());
        log.info("[{}] Publishing: {}", marketName, event);

        for (StockObserver observer : observers) {
            try {
                observer.onStockUpdate(event);
            } catch (Exception e) {
                // Isolate faulty observer — don't let it break others
                log.error("[{}] Observer '{}' threw during event delivery: {}",
                        marketName, observer.getObserverName(), e.getMessage(), e);
            }
        }
    }

    /** Returns the number of currently registered observers. */
    public int getObserverCount() {
        return observers.size();
    }

    /** Returns the market name. */
    public String getMarketName() {
        return marketName;
    }
}
