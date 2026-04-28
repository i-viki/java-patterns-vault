package dev.jayav.patterns.observer;

import java.util.Map;

/**
 * Demonstration of the Observer Pattern using a simulated stock market.
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class ObserverDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       Observer Pattern — Stock Market Demo       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        // ── Setup Subject ─────────────────────────────────────────────────
        StockMarket nasdaq = new StockMarket("NASDAQ");

        // ── Setup Observers ───────────────────────────────────────────────
        PortfolioTracker portfolio = new PortfolioTracker("Jaya V",
                Map.of("AAPL", 50, "NVDA", 10, "MSFT", 25));
        AlertEngine alerts = new AlertEngine(5.0, 8.0);   // alert on ±5% or +8%
        AuditLogger audit  = new AuditLogger();

        // ── Subscribe ─────────────────────────────────────────────────────
        nasdaq.subscribe(portfolio);
        nasdaq.subscribe(alerts);
        nasdaq.subscribe(audit);
        System.out.printf("Observers registered: %d%n%n", nasdaq.getObserverCount());

        // ── Simulate market events ────────────────────────────────────────
        System.out.println("▶ Publishing market events...");
        nasdaq.publishPriceUpdate("AAPL", 182.50, 178.00);   // +2.5% normal gain
        nasdaq.publishPriceUpdate("NVDA", 620.00, 680.00);   // -8.8% sharp drop → alert!
        nasdaq.publishPriceUpdate("MSFT", 425.00, 390.00);   // +8.97% strong gain → alert!
        nasdaq.publishPriceUpdate("AAPL", 175.00, 182.50);   // -4.1% moderate drop

        // ── Unsubscribe demo ─────────────────────────────────────────────
        System.out.println("\n▶ Unsubscribing PortfolioTracker...");
        nasdaq.unsubscribe(portfolio);
        System.out.printf("Observers after unsubscribe: %d%n", nasdaq.getObserverCount());
        nasdaq.publishPriceUpdate("AAPL", 180.00, 175.00); // portfolio won't see this

        // ── Audit summary ─────────────────────────────────────────────────
        System.out.printf("%n══ Audit Log (%d events) ══%n", audit.getEventCount());
        audit.getAuditLog().forEach(entry -> System.out.println("  " + entry));

        // ── Alert summary ─────────────────────────────────────────────────
        System.out.printf("%n══ Fired Alerts (%d) ══%n", alerts.getFiredAlerts().size());
        alerts.getFiredAlerts().forEach(a -> System.out.println("  " + a));
    }
}
