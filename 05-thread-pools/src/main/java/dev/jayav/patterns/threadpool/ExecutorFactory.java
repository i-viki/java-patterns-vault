package dev.jayav.patterns.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Factory that creates the appropriate {@link ExecutorService} for each {@link TaskType}.
 *
 * <h3>CPU-Bound Tasks:</h3>
 * Uses a fixed-size {@link ForkJoinPool} with parallelism = available CPU cores.
 * This prevents over-subscription and minimizes context switching overhead.
 *
 * <h3>I/O-Bound Tasks (Java 21+):</h3>
 * Uses {@code Executors.newVirtualThreadPerTaskExecutor()} — virtual threads are
 * extremely lightweight (~1KB heap each vs ~1MB for platform threads) and park
 * efficiently during I/O without blocking OS threads.
 * Falls back to cached thread pool on Java < 21.
 *
 * <h3>Scheduled Tasks:</h3>
 * Uses {@link ScheduledThreadPoolExecutor} with a fixed number of daemon threads
 * for periodic/delayed work.
 *
 * @author Jaya V
 * @version 1.0.0
 */
public final class ExecutorFactory {

    private static final Logger log = LoggerFactory.getLogger(ExecutorFactory.class);
    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    private ExecutorFactory() {
        throw new UnsupportedOperationException("Factory utility class");
    }

    /**
     * Creates an optimally-configured {@link ExecutorService} for the given task type.
     *
     * @param type     the type of work this executor will handle
     * @param poolName a descriptive name for monitoring/logging
     * @return a configured executor (caller is responsible for shutdown)
     */
    public static ExecutorService create(TaskType type, String poolName) {
        return switch (type) {
            case CPU_BOUND -> createCpuBoundExecutor(poolName);
            case IO_BOUND  -> createIoBoundExecutor(poolName);
            case SCHEDULED -> throw new IllegalArgumentException(
                    "Use createScheduled() for SCHEDULED task type");
        };
    }

    /**
     * Creates a {@link ScheduledExecutorService} for periodic/delayed tasks.
     *
     * @param poolName    a descriptive name
     * @param threadCount number of threads in the scheduler pool
     * @return a configured scheduled executor
     */
    public static ScheduledExecutorService createScheduled(String poolName, int threadCount) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
                threadCount,
                new NamedThreadFactory(poolName + "-scheduler", true) // daemon threads
        );
        executor.setRemoveOnCancelPolicy(true); // prevent memory leak from cancelled tasks
        log.info("[ExecutorFactory] Created SCHEDULED executor '{}' with {} threads",
                poolName, threadCount);
        return executor;
    }

    // ── Private factory methods ───────────────────────────────────────────────

    private static ExecutorService createCpuBoundExecutor(String poolName) {
        // ForkJoinPool with parallelism = cores prevents CPU over-subscription
        ForkJoinPool pool = new ForkJoinPool(
                CPU_CORES,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                (thread, ex) -> log.error("[{}] Uncaught exception in thread {}: {}",
                        poolName, thread.getName(), ex.getMessage(), ex),
                false // non-async mode (LIFO for CPU-bound tasks)
        );
        log.info("[ExecutorFactory] Created CPU_BOUND executor '{}' with {} threads (cores: {})",
                poolName, CPU_CORES, CPU_CORES);
        return pool;
    }

    private static ExecutorService createIoBoundExecutor(String poolName) {
        // Java 21: Virtual threads are ideal for I/O-bound work
        int javaVersion = Runtime.version().feature();

        if (javaVersion >= 21) {
            log.info("[ExecutorFactory] Created IO_BOUND executor '{}' with Virtual Threads (Java {})",
                    poolName, javaVersion);
            return Executors.newVirtualThreadPerTaskExecutor();
        }

        // Fallback for Java < 21: cached thread pool (threads grow as needed)
        int maxThreads = CPU_CORES * 10; // heuristic for I/O-heavy workloads
        log.info("[ExecutorFactory] Created IO_BOUND executor '{}' with cached pool (max: {})",
                poolName, maxThreads);
        return new ThreadPoolExecutor(
                CPU_CORES,       // corePoolSize
                maxThreads,      // maximumPoolSize
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000),
                new NamedThreadFactory(poolName + "-io", false)
        );
    }
}
