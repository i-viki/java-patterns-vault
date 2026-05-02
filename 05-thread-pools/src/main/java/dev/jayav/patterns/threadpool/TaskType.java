package dev.jayav.patterns.threadpool;

/**
 * Categorizes work by its primary bottleneck resource.
 *
 * <p>This distinction drives the correct Executor choice:
 * <ul>
 *   <li>{@link #CPU_BOUND} — thread count = CPU cores (avoid context switching)</li>
 *   <li>{@link #IO_BOUND}  — thread count >> cores (threads block on I/O, not CPU)</li>
 *   <li>{@link #SCHEDULED} — periodic/delayed execution with ScheduledExecutorService</li>
 * </ul>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public enum TaskType {

    /**
     * Work that saturates the CPU (image resize, encryption, compression, ML inference).
     * Optimal pool size: {@code Runtime.getRuntime().availableProcessors()}.
     */
    CPU_BOUND,

    /**
     * Work blocked on external I/O (database queries, HTTP calls, file reads).
     * Optimal pool size: much larger than CPU count; Java 21 virtual threads ideal.
     */
    IO_BOUND,

    /**
     * Periodic or delayed work (health checks, report generation, cache refresh).
     * Uses {@link java.util.concurrent.ScheduledExecutorService}.
     */
    SCHEDULED
}
