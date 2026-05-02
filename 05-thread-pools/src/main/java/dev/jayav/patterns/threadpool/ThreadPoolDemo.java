package dev.jayav.patterns.threadpool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Demonstrates optimal thread pool configuration for different workload types.
 *
 * <p>Three scenarios:
 * <ol>
 *   <li>CPU-bound: Image resize via ForkJoinPool (parallelism = CPU cores)</li>
 *   <li>I/O-bound: Database fetch via Virtual Threads (Java 21) or cached pool</li>
 *   <li>Scheduled: Report generation via ScheduledExecutorService</li>
 * </ol>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class ThreadPoolDemo {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolDemo.class);

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║       Thread Pools Pattern — Executor Demo       ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.printf("  CPU Cores: %d | Java: %s%n%n",
                Runtime.getRuntime().availableProcessors(),
                Runtime.version());

        demoCpuBound();
        demoIoBound();
        demoScheduled();
    }

    // ── Demo 1: CPU-Bound (ForkJoinPool) ─────────────────────────────────────

    private static void demoCpuBound() throws InterruptedException, ExecutionException {
        System.out.println("▶ CPU-Bound: Image resize batch (ForkJoinPool)");

        ExecutorService cpuPool = ExecutorFactory.create(TaskType.CPU_BOUND, "image-resize");
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            final int imgId = i;
            futures.add(cpuPool.submit(() -> simulateImageResize(imgId)));
        }

        for (Future<String> f : futures) {
            System.out.println("  " + f.get());
        }

        shutdownGracefully(cpuPool, "image-resize");
        System.out.println();
    }

    // ── Demo 2: I/O-Bound (Virtual Threads / Cached Pool) ────────────────────

    private static void demoIoBound() throws InterruptedException, ExecutionException {
        System.out.println("▶ I/O-Bound: Parallel DB fetch (Virtual Threads / Cached Pool)");

        ExecutorService ioPool = ExecutorFactory.create(TaskType.IO_BOUND, "db-fetch");
        List<Future<String>> futures = new ArrayList<>();

        String[] userIds = {"USR-001", "USR-042", "USR-107", "USR-215", "USR-388"};
        for (String userId : userIds) {
            futures.add(ioPool.submit(() -> simulateDatabaseFetch(userId)));
        }

        for (Future<String> f : futures) {
            System.out.println("  " + f.get());
        }

        shutdownGracefully(ioPool, "db-fetch");
        System.out.println();
    }

    // ── Demo 3: Scheduled ─────────────────────────────────────────────────────

    private static void demoScheduled() throws InterruptedException {
        System.out.println("▶ Scheduled: Report heartbeat (every 500ms, 3 times)");

        ScheduledExecutorService scheduler =
                ExecutorFactory.createScheduled("report-scheduler", 1);

        scheduler.scheduleAtFixedRate(
                () -> System.out.println("  [Report] Health report generated at " +
                        java.time.LocalTime.now()),
                0, 500, TimeUnit.MILLISECONDS
        );

        Thread.sleep(1600); // Let it fire ~3 times
        shutdownGracefully(scheduler, "report-scheduler");
        System.out.println();
    }

    // ── Task simulations ──────────────────────────────────────────────────────

    private static String simulateImageResize(int imageId) throws InterruptedException {
        // CPU-intensive work (e.g., Lanczos resampling) — simulated as sleep
        Thread.sleep(50 + (imageId % 3) * 20);
        return String.format("[CPU] Image-%03d resized on thread '%s'",
                imageId, Thread.currentThread().getName());
    }

    private static String simulateDatabaseFetch(String userId) throws InterruptedException {
        // I/O-blocking work — thread parks waiting for DB response
        Thread.sleep(80 + (int)(Math.random() * 100));
        return String.format("[I/O] User '%s' fetched on thread '%s'",
                userId, Thread.currentThread().getName());
    }

    // ── Graceful shutdown ─────────────────────────────────────────────────────

    private static void shutdownGracefully(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("[{}] Pool did not terminate in time — forcing shutdown", name);
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
