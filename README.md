# Java Patterns Vault

[![Java](https://img.shields.io/badge/Java-21%20LTS-18181b?logo=openjdk&logoColor=ED8B00&labelColor=101012)](https://adoptium.net)
[![Maven](https://img.shields.io/badge/Maven-3.9-18181b?logo=apachemaven&logoColor=C71A36&labelColor=101012)](https://maven.apache.org)
[![JUnit](https://img.shields.io/badge/JUnit-5-18181b?logo=junit5&logoColor=25A162&labelColor=101012)](https://junit.org/junit5/)
[![CI](https://img.shields.io/badge/GitHub_Actions-CI-18181b?logo=githubactions&logoColor=2088FF&labelColor=101012)](https://github.com/i-viki/java-patterns-vault/actions)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

An enterprise-grade, **modular architectural repository of reference design patterns and Java 21 concurrency primitives**. Engineered as an isolated multi-module Maven suite, each pattern provides production-realistic domain implementations, rigorous JUnit 5 test suites, full Javadoc specifications, and modern Java features including **Virtual Threads (Project Loom)**, **sealed interfaces**, **records**, and **ForkJoinPool parallelism**.

---

## Key Architecture Pillars

- **Modular Maven Multi-Module Architecture (`pom.xml`):**
  Decoupled parent Bill of Materials (BOM) orchestrating 5 isolated pattern modules with shared Java 21 compiler targets and testing dependencies.
- **Dynamic Strategy Pattern (`01-strategy`):**
  Runtime-swappable payment processing engines (Credit Card, PayPal, Crypto) adhering strictly to the Open-Closed Principle (OCP) with immutable transaction audit logs.
- **Decoupled Factory Pattern (`02-factory`):**
  Multi-channel notification dispatchers (Email, SMS, Push) isolating concrete instantiations behind static factory abstractions and Java `Optional` return semantics.
- **High-Throughput Observer Pattern (`03-observer`):**
  Thread-safe event propagation engine modeling live stock market ticks across portfolio trackers using `CopyOnWriteArrayList` and immutable event records.
- **Fault-Tolerant Circuit Breaker (`04-circuit-breaker`):**
  Finite state machine (`CLOSED` ↔ `OPEN` ↔ `HALF_OPEN`) with atomic state transitions (`AtomicReference`) isolating external API dependencies from cascading failures.
- **Modern Concurrency & Workload Allocators (`05-thread-pools`):**
  Java 21 executor selection distinguishing between CPU-bound parallelism (`ForkJoinPool`) and high-concurrency I/O workloads (`Executors.newVirtualThreadPerTaskExecutor()`).

---

## System Architecture

```mermaid
flowchart TB
    subgraph VAULT["Java Patterns Vault (Parent POM / Java 21 LTS)"]
        direction TB
        BOM["Parent Bill of Materials<br/>Shared JUnit 5 & Java 21 Compiler Config"]
    end

    subgraph MODULES["Modular Domain Implementations"]
        direction TB
        M1["01-strategy<br/>• PaymentContext Engine<br/>• OCP Runtime Swap"]
        M2["02-factory<br/>• NotificationFactory<br/>• Multi-Channel Dispatch"]
        M3["03-observer<br/>• StockMarket Publisher<br/>• CopyOnWrite Listeners"]
        M4["04-circuit-breaker<br/>• FSM State Machine<br/>• AtomicReference Isolation"]
        M5["05-thread-pools<br/>• Virtual Threads (I/O)<br/>• ForkJoinPool (CPU)"]
    end

    subgraph EXECUTION["Runtime Execution & Verification Tier"]
        DEMO["Interactive CLI Demonstrations<br/>mvn exec:java"]
        JUNIT["JUnit 5 Verification Suites<br/>100% Assertion Coverage"]
    end

    BOM --> M1
    BOM --> M2
    BOM --> M3
    BOM --> M4
    BOM --> M5

    MODULES --> DEMO
    MODULES --> JUNIT
```

### Multi-Module Structure

```text
java-patterns-vault/
├── pom.xml                    # Parent POM (Java 21, shared dependencies BOM)
├── 01-strategy/               # Payment processing (Credit Card, PayPal, Crypto)
├── 02-factory/                # Notifications (Email, SMS, Push)
├── 03-observer/               # Stock market (Portfolio, AlertEngine, AuditLogger)
├── 04-circuit-breaker/        # External API fault isolation with FSM
├── 05-thread-pools/           # Executor configuration by workload type
└── docs/                      # Architecture diagrams and pattern overview
```

---

## Pattern Taxonomy & Workload Model Invariants

The vault categorizes software engineering patterns into domain-specific execution semantics:

$$N_{\text{threads}}^{\text{CPU}} = N_{\text{cores}}, \quad N_{\text{threads}}^{\text{IO}} = \infty \text{ (Virtual Thread Fibers)}$$

| Module | Design Pattern | Production Problem Solved | Core Java 21 Primitives | Key Guarantees |
| :--- | :--- | :--- | :--- | :--- |
| **`01-strategy`** | Behavioral / Strategy | Eliminates fragile `if-else` branching in billing | Interfaces, Polymorphism, Records | OCP compliance, immutable audit trail |
| **`02-factory`** | Creational / Factory | Decouples notification dispatch from callers | Static Factories, Enums, `Optional<T>` | Encapsulated instantiation, zero direct coupling |
| **`03-observer`** | Behavioral / Observer | Thread-safe pub/sub broadcast for financial tickers | `CopyOnWriteArrayList`, Immutable Records | Lock-free reads, real-time subscriber fanout |
| **`04-circuit-breaker`**| Resilience / FSM | Prevents cascading gateway outages during downstream failure | `AtomicReference`, CAS, State Pattern | Sub-millisecond fail-fast, automated recovery probe |
| **`05-thread-pools`** | Concurrency / Executor | Optimal throughput matching hardware constraints | Virtual Threads, `ForkJoinPool`, `ScheduledExecutor` | Workload-matched hardware saturation |

---

## Pattern Implementation Highlights & Code Snippets

### 1. Strategy Pattern — Runtime Payment Engines
```java
PaymentContext ctx = new PaymentContext(creditCardStrategy, "ORD-001");
ctx.executePayment(249.99);

// Zero code change — dynamically swap payment provider at runtime
ctx.setStrategy(paypalStrategy);
ctx.executePayment(249.99);
```

### 2. Factory Pattern — Multi-Channel Notifications
```java
// Caller never couples to concrete service implementations
NotificationServiceFactory.create(NotificationChannel.EMAIL)
    .ifPresent(svc -> svc.send("user@example.com", "Order fulfilled successfully!"));
```

### 3. Observer Pattern — Stock Market Event Broadcasting
```java
StockMarket nasdaq = new StockMarket("NASDAQ");
nasdaq.subscribe(new PortfolioTracker("Jayavignesh", positions));
nasdaq.subscribe(new AlertEngine(5.0, 8.0));
nasdaq.publishPriceUpdate("NVDA", 620.00, 680.00); // Trigger auto-alerts on threshold
```

### 4. Circuit Breaker Pattern — Finite State Machine
```java
CircuitBreaker<String> breaker = new CircuitBreaker<>(
    new CircuitBreakerConfig.Builder("inventory-service")
        .failureThreshold(3)
        .openTimeoutMs(10_000)
        .build());

String result = fallback.executeWithFallback(breaker, () -> api.call(requestId));
// Deterministic lifecycle: CLOSED -> OPEN -> HALF_OPEN -> CLOSED (auto-recovery)
```

### 5. Thread Pools — Workload-Matched Executor Selection
```java
// CPU-bound: ForkJoinPool matched to available CPU cores
ExecutorService cpuPool = ExecutorFactory.create(TaskType.CPU_BOUND, "image-processing");

// I/O-bound: Lightweight Virtual Threads (Java 21 Project Loom)
ExecutorService ioPool = ExecutorFactory.create(TaskType.IO_BOUND, "database-fetch");

// Scheduled: Dedicated background timer
ScheduledExecutorService scheduler = ExecutorFactory.createScheduled("cron-reports", 2);
```

---

## Quickstart & Platform Orchestration

### Prerequisites
- Java 21 LTS (Oracle OpenJDK or Eclipse Temurin)
- Apache Maven 3.9+

---

### Option A: Build and Run All Test Suites (Recommended)

Compile all modules and execute the automated verification test suite:
```bash
# Clone repository
git clone https://github.com/i-viki/java-patterns-vault.git
cd java-patterns-vault

# Compile parent BOM and sub-modules
mvn clean compile

# Execute all JUnit 5 test suites
mvn test
```

---

### Option B: Interactive Pattern Demonstrations

Execute live standalone pattern demonstrations directly from the CLI:

#### Strategy Pattern Demo
```bash
mvn -pl 01-strategy exec:java -Dexec.mainClass=dev.jayavignesh.patterns.strategy.StrategyDemo
```

#### Factory Pattern Demo
```bash
mvn -pl 02-factory exec:java -Dexec.mainClass=dev.jayavignesh.patterns.factory.FactoryDemo
```

#### Observer Pattern Demo
```bash
mvn -pl 03-observer exec:java -Dexec.mainClass=dev.jayavignesh.patterns.observer.ObserverDemo
```

#### Circuit Breaker Pattern Demo
```bash
mvn -pl 04-circuit-breaker exec:java -Dexec.mainClass=dev.jayavignesh.patterns.circuitbreaker.CircuitBreakerDemo
```

#### Thread Pools Concurrency Demo
```bash
mvn -pl 05-thread-pools exec:java -Dexec.mainClass=dev.jayavignesh.patterns.threadpool.ThreadPoolDemo
```

---

## Architecture Roadmap & Pattern Horizons

The vault expands systematically across four architectural maturity tiers:

- **Tier 1 — Core Creational & Behavioral Patterns (Completed):**
  `Strategy` · `Factory` · `Observer` · `Circuit Breaker` · `Thread Pool Allocator`
- **Tier 2 — Structural Patterns (Next Release):**
  `Decorator` · `Command` · `Builder` · `Proxy` · `Singleton`
- **Tier 3 — Concurrency & Synchronization Primitives:**
  `Producer-Consumer` · `Read-Write Lock` · `Semaphore Guard` · `CompletableFuture Chain`
- **Tier 4 — Distributed Architectural Patterns:**
  `Event Sourcing` · `CQRS` · `Choreographed Saga` · `Bulkhead Isolation`

---

## Decoupled Module Strategy

The repository isolates dependencies and execution scopes across Maven sub-modules:

| Module Directory | Artifact ID | Primary Execution Class | Scope |
| :--- | :--- | :--- | :--- |
| **`01-strategy`** | `pattern-strategy` | `dev.jayavignesh.patterns.strategy.StrategyDemo` | Payment engine swapping & audit trail |
| **`02-factory`** | `pattern-factory` | `dev.jayavignesh.patterns.factory.FactoryDemo` | Multi-channel notification delivery |
| **`03-observer`** | `pattern-observer` | `dev.jayavignesh.patterns.observer.ObserverDemo` | Stock market ticker broadcast |
| **`04-circuit-breaker`**| `pattern-circuit-breaker` | `dev.jayavignesh.patterns.circuitbreaker.CircuitBreakerDemo`| Microservice fault isolation FSM |
| **`05-thread-pools`** | `pattern-thread-pools` | `dev.jayavignesh.patterns.threadpool.ThreadPoolDemo` | Virtual Threads vs ForkJoinPool benchmarking |

---

## Live Telemetry & Observability Hubs

| Hub / Service | Type | Target | Role & Live Features |
| :--- | :--- | :--- | :--- |
| **GitHub Actions CI** | CI Pipeline | [.github/workflows/ci.yml](.github/workflows/ci.yml) | Continuous automated build and JUnit 5 regression test validation. |
| **Javadoc Portal** | Documentation | `mvn javadoc:aggregate` | Comprehensive aggregated API and design pattern documentation. |

---

## Testing & Quality Assurance Suite

### Multi-Module JUnit 5 Regression Suite
```bash
mvn test
# Executes all test suites across all 5 modules, asserting zero race conditions and state compliance.
```

### Module-Specific Targeted Test
```bash
mvn -pl 04-circuit-breaker test
# Validates atomic state transitions: CLOSED -> OPEN -> HALF_OPEN -> CLOSED.
```

---

## Author

**Jayavignesh**
- **Website:** [jayavignesh.dev](https://jayavignesh.dev)
- **GitHub:** [@i-viki](https://github.com/i-viki)
- **Email:** [hello@jayavignesh.dev](mailto:hello@jayavignesh.dev)

---

## License

This platform is distributed under the **MIT License**. See the [`LICENSE`](LICENSE) file for complete terms.
