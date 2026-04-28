package dev.jayav.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observer: Fires price threshold alerts when stock moves beyond configured limits.
 *
 * <p>Configurable with a percentage drop threshold and a percentage gain threshold.
 * Accumulates fired alerts for inspection (useful for unit testing).</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class AlertEngine implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(AlertEngine.class);

    private final double dropThresholdPercent;  // e.g. -5.0 for 5% drop alert
    private final double gainThresholdPercent;  // e.g. +10.0 for 10% gain alert
    private final List<String> firedAlerts = new ArrayList<>();

    public AlertEngine(double dropThresholdPercent, double gainThresholdPercent) {
        this.dropThresholdPercent = dropThresholdPercent;
        this.gainThresholdPercent = gainThresholdPercent;
    }

    @Override
    public void onStockUpdate(StockEvent event) {
        double change = event.changePercent();

        if (change <= -Math.abs(dropThresholdPercent)) {
            String alert = String.format("ALERT DROP: %s fell %.2f%% (%.2f → %.2f)",
                    event.ticker(), change, event.previousPrice(), event.price());
            firedAlerts.add(alert);
            log.warn("[AlertEngine] {}", alert);
        }

        if (change >= gainThresholdPercent) {
            String alert = String.format("ALERT GAIN: %s rose +%.2f%% (%.2f → %.2f)",
                    event.ticker(), change, event.previousPrice(), event.price());
            firedAlerts.add(alert);
            log.info("[AlertEngine] {}", alert);
        }
    }

    @Override
    public String getObserverName() {
        return "AlertEngine[drop≤" + dropThresholdPercent + "%, gain≥" + gainThresholdPercent + "%]";
    }

    /** Returns all alerts fired since this engine started. */
    public List<String> getFiredAlerts() {
        return Collections.unmodifiableList(firedAlerts);
    }

    /** Clears alert history. */
    public void clearAlerts() {
        firedAlerts.clear();
    }
}
