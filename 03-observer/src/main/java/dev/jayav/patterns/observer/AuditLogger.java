package dev.jayav.patterns.observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Observer: Persists all stock events to an in-memory audit log.
 *
 * <p>In production, this would write to a database, Kafka topic, or
 * append to a structured log file for compliance and audit purposes.</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class AuditLogger implements StockObserver {

    private static final Logger log = LoggerFactory.getLogger(AuditLogger.class);
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
                    .withZone(java.time.ZoneId.of("UTC"));

    private final List<String> auditLog = new ArrayList<>();

    @Override
    public void onStockUpdate(StockEvent event) {
        String entry = String.format("[%s] AUDIT: %s | Price: $%.2f | Prev: $%.2f | Change: %+.2f%%",
                FORMATTER.format(event.occurredAt()),
                event.ticker(),
                event.price(),
                event.previousPrice(),
                event.changePercent());

        auditLog.add(entry);
        log.debug("[AuditLogger] {}", entry);
    }

    @Override
    public String getObserverName() {
        return "AuditLogger";
    }

    /** Returns all audit log entries since startup. */
    public List<String> getAuditLog() {
        return Collections.unmodifiableList(auditLog);
    }

    /** Returns the total number of events logged. */
    public int getEventCount() {
        return auditLog.size();
    }
}
