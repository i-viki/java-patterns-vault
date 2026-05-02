# Java Patterns Vault

![Build](https://img.shields.io/github/actions/workflow/status/i-viki/java-patterns-vault/ci.yml?branch=main&style=flat-square&label=Build)
![Java](https://img.shields.io/badge/Java-21-007396?style=flat-square&logo=java)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apache-maven)
![JUnit](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)

Reference-grade implementations of battle-tested design patterns used in real production systems.
Each pattern is a self-contained Maven module with production-realistic use cases, full Javadoc, and JUnit 5 test suites.

---

## Patterns

| # | Pattern | Use Case | Key Java Concepts |
|---|---------|----------|-------------------|
| [01](./01-strategy) | **Strategy** | Runtime-swappable payment engines | Interface, OCP, audit trail |
| [02](./02-factory) | **Factory** | Multi-channel notification services | Static factory, `Optional<>`, enum |
| [03](./03-observer) | **Observer** | Stock market event propagation | `CopyOnWriteArrayList`, `record`, FSM |
| [04](./04-circuit-breaker) | **Circuit Breaker** | Fault isolation for external APIs | `AtomicReference`, FSM, fallback |
| [05](./05-thread-pools) | **Thread Pools** | CPU-bound vs I/O-bound executors | `ForkJoinPool`, Virtual Threads, `ScheduledExecutorService` |

---

## Project Structure

```
java-patterns-vault/
├── pom.xml                    ← Parent POM (Java 21, shared deps BOM)
├── 01-strategy/               ← Payment processing (Credit Card, PayPal, Crypto)
├── 02-factory/                ← Notifications (Email, SMS, Push)
├── 03-observer/               ← Stock market (Portfolio, AlertEngine, AuditLogger)
├── 04-circuit-breaker/        ← External API fault isolation with FSM
├── 05-thread-pools/           ← Executor configuration by workload type
└── docs/                      ← Architecture diagrams and pattern overview
```

---

## Getting Started

**Prerequisites:** Java 21+, Maven 3.9+

```bash
# Clone
git clone https://github.com/i-viki/java-patterns-vault.git
cd java-patterns-vault

# Build all modules
mvn clean compile

# Run all tests
mvn test

# Run a specific pattern demo
mvn -pl 01-strategy exec:java -Dexec.mainClass=dev.jayav.patterns.strategy.StrategyDemo
mvn -pl 02-factory  exec:java -Dexec.mainClass=dev.jayav.patterns.factory.FactoryDemo
mvn -pl 03-observer exec:java -Dexec.mainClass=dev.jayav.patterns.observer.ObserverDemo
mvn -pl 04-circuit-breaker exec:java -Dexec.mainClass=dev.jayav.patterns.circuitbreaker.CircuitBreakerDemo
mvn -pl 05-thread-pools    exec:java -Dexec.mainClass=dev.jayav.patterns.threadpool.ThreadPoolDemo
```

---

## Pattern Highlights

### Strategy — Payment Engines
```java
PaymentContext ctx = new PaymentContext(creditCardStrategy, "ORD-001");
ctx.executePayment(249.99);

// Zero code change — swap at runtime
ctx.setStrategy(paypalStrategy);
ctx.executePayment(249.99);
```

### Factory — Notification Services
```java
// Caller never touches concrete classes
NotificationServiceFactory.create(NotificationChannel.EMAIL)
    .ifPresent(svc -> svc.send("user@example.com", "Welcome!"));
```

### Observer — Stock Events
```java
StockMarket nasdaq = new StockMarket("NASDAQ");
nasdaq.subscribe(new PortfolioTracker("Jaya V", positions));
nasdaq.subscribe(new AlertEngine(5.0, 8.0));
nasdaq.publishPriceUpdate("NVDA", 620.00, 680.00); // -8.8% → alert fires
```

### Circuit Breaker — FSM
```java
CircuitBreaker<String> breaker = new CircuitBreaker<>(
    new CircuitBreakerConfig.Builder("inventory-service")
        .failureThreshold(3).openTimeoutMs(10_000).build());

String result = fallback.executeWithFallback(breaker, () -> api.call(requestId));
// CLOSED → OPEN → HALF_OPEN → CLOSED (auto-recovery)
```

### Thread Pools — Executor Selection
```java
// CPU-bound: ForkJoinPool(parallelism = CPU cores)
ExecutorService cpuPool = ExecutorFactory.create(TaskType.CPU_BOUND, "image-resize");

// I/O-bound: Virtual Threads (Java 21)
ExecutorService ioPool = ExecutorFactory.create(TaskType.IO_BOUND, "db-fetch");

// Scheduled: ScheduledExecutorService
ScheduledExecutorService scheduler = ExecutorFactory.createScheduled("reports", 2);
```

---

## Roadmap

**Tier 2 — Structural Patterns**
`Decorator` · `Command` · `Builder` · `Proxy` · `Singleton`

**Tier 3 — Concurrency Patterns**
`Producer-Consumer` · `Read-Write Lock` · `Semaphore Guard` · `CompletableFuture Chain`

**Tier 4 — Architectural Patterns**
`Event Sourcing` · `CQRS` · `Saga` · `Bulkhead`

---

## Author

**Jaya V** — [GitHub](https://github.com/i-viki)

## License

[MIT](./LICENSE)
