package dev.jayav.patterns.observer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for the Observer Pattern implementation.
 *
 * @author Jaya V
 * @version 1.0.0
 */
@DisplayName("Observer Pattern — Stock Market Tests")
class ObserverPatternTest {

    private StockMarket market;
    private AuditLogger auditLogger;
    private AlertEngine alertEngine;

    @BeforeEach
    void setUp() {
        market      = new StockMarket("TEST-EXCHANGE");
        auditLogger = new AuditLogger();
        alertEngine = new AlertEngine(5.0, 8.0);
    }

    @Nested
    @DisplayName("StockMarket (Subject)")
    class StockMarketTests {

        @Test
        @DisplayName("should start with zero observers")
        void shouldStartWithNoObservers() {
            assertThat(market.getObserverCount()).isZero();
        }

        @Test
        @DisplayName("should increment observer count on subscribe")
        void shouldIncrementOnSubscribe() {
            market.subscribe(auditLogger);
            market.subscribe(alertEngine);

            assertThat(market.getObserverCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("should not add duplicate observers")
        void shouldNotAddDuplicateObservers() {
            market.subscribe(auditLogger);
            market.subscribe(auditLogger); // duplicate

            assertThat(market.getObserverCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("should decrement observer count on unsubscribe")
        void shouldDecrementOnUnsubscribe() {
            market.subscribe(auditLogger);
            market.unsubscribe(auditLogger);

            assertThat(market.getObserverCount()).isZero();
        }

        @Test
        @DisplayName("should throw when null observer is subscribed")
        void shouldThrowForNullObserver() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> market.subscribe(null));
        }

        @Test
        @DisplayName("should deliver events to all registered observers")
        void shouldDeliverEventsToAllObservers() {
            market.subscribe(auditLogger);
            market.subscribe(alertEngine);

            market.publishPriceUpdate("AAPL", 180.00, 170.00);

            assertThat(auditLogger.getEventCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("unsubscribed observer should not receive further events")
        void unsubscribedObserverShouldNotReceiveEvents() {
            market.subscribe(auditLogger);
            market.publishPriceUpdate("AAPL", 180.00, 170.00);

            market.unsubscribe(auditLogger);
            market.publishPriceUpdate("AAPL", 185.00, 180.00);

            // Only received 1 event (before unsubscribe)
            assertThat(auditLogger.getEventCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("AlertEngine (Observer)")
    class AlertEngineTests {

        @Test
        @DisplayName("should fire drop alert when price falls beyond threshold")
        void shouldFireDropAlert() {
            market.subscribe(alertEngine);
            market.publishPriceUpdate("NVDA", 620.00, 680.00); // -8.8% drop

            assertThat(alertEngine.getFiredAlerts()).hasSize(1);
            assertThat(alertEngine.getFiredAlerts().get(0)).contains("ALERT DROP");
        }

        @Test
        @DisplayName("should fire gain alert when price rises beyond threshold")
        void shouldFireGainAlert() {
            market.subscribe(alertEngine);
            market.publishPriceUpdate("MSFT", 425.00, 390.00); // +8.97% gain

            assertThat(alertEngine.getFiredAlerts()).hasSize(1);
            assertThat(alertEngine.getFiredAlerts().get(0)).contains("ALERT GAIN");
        }

        @Test
        @DisplayName("should not fire alert for change within threshold")
        void shouldNotFireForSmallChange() {
            market.subscribe(alertEngine);
            market.publishPriceUpdate("AAPL", 182.50, 178.00); // +2.5% — below both thresholds

            assertThat(alertEngine.getFiredAlerts()).isEmpty();
        }
    }

    @Nested
    @DisplayName("AuditLogger (Observer)")
    class AuditLoggerTests {

        @Test
        @DisplayName("should log every event received")
        void shouldLogEveryEvent() {
            market.subscribe(auditLogger);

            market.publishPriceUpdate("AAPL", 180.00, 175.00);
            market.publishPriceUpdate("NVDA", 650.00, 680.00);
            market.publishPriceUpdate("MSFT", 410.00, 400.00);

            assertThat(auditLogger.getEventCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("audit log should be unmodifiable")
        void auditLogShouldBeUnmodifiable() {
            market.subscribe(auditLogger);
            market.publishPriceUpdate("AAPL", 180.00, 175.00);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> auditLogger.getAuditLog().clear());
        }
    }

    @Nested
    @DisplayName("StockEvent (Record)")
    class StockEventTests {

        @Test
        @DisplayName("should calculate positive change percent correctly")
        void shouldCalculatePositiveChangePercent() {
            StockEvent event = new StockEvent("AAPL", 110.0, 100.0,
                    java.time.Instant.now());

            assertThat(event.changePercent()).isCloseTo(10.0, within(0.01));
            assertThat(event.isGain()).isTrue();
            assertThat(event.isLoss()).isFalse();
        }

        @Test
        @DisplayName("should calculate negative change percent correctly")
        void shouldCalculateNegativeChangePercent() {
            StockEvent event = new StockEvent("NVDA", 90.0, 100.0,
                    java.time.Instant.now());

            assertThat(event.changePercent()).isCloseTo(-10.0, within(0.01));
            assertThat(event.isLoss()).isTrue();
        }
    }
}
