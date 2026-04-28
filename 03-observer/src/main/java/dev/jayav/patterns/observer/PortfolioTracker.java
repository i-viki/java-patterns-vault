package dev.jayav.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Observer: Tracks portfolio value across multiple stock holdings.
 *
 * <p>Maintains a position map (ticker → quantity) and recalculates
 * total portfolio value on every relevant stock event.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class PortfolioTracker implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(PortfolioTracker.class);

    private final String ownerName;
    private final Map<String, Integer> positions; // ticker → quantity held
    private final Map<String, Double> lastPrices; // ticker → last known price

    public PortfolioTracker(String ownerName, Map<String, Integer> positions) {
        this.ownerName  = ownerName;
        this.positions  = new HashMap<>(positions);
        this.lastPrices = new HashMap<>();
    }

    @Override
    public void onStockUpdate(StockEvent event) {
        if (!positions.containsKey(event.ticker())) return; // not in portfolio

        lastPrices.put(event.ticker(), event.price());
        double totalValue = calculatePortfolioValue();
        double change = event.changePercent();

        log.info("[Portfolio:{}] {} updated ${} → ${} ({}{:.2f}%). Portfolio value: ${}",
                ownerName, event.ticker(),
                String.format("%.2f", event.previousPrice()),
                String.format("%.2f", event.price()),
                change >= 0 ? "+" : "",
                change,
                String.format("%.2f", totalValue));
    }

    @Override
    public String getObserverName() {
        return "PortfolioTracker[" + ownerName + "]";
    }

    /** Calculates total portfolio value based on current known prices. */
    public double calculatePortfolioValue() {
        return positions.entrySet().stream()
                .mapToDouble(entry -> {
                    double price = lastPrices.getOrDefault(entry.getKey(), 0.0);
                    return price * entry.getValue();
                })
                .sum();
    }

    public Map<String, Integer> getPositions() {
        return Map.copyOf(positions);
    }
}
