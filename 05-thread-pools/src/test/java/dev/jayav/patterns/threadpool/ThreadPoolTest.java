package dev.jayav.patterns.threadpool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Thread Pool pattern — verifies pool sizing and task completion guarantees.
 *
 * @author Jaya V
 * @version 1.0.0
 */
@DisplayName("Thread Pools Pattern — Executor Configuration Tests")
class ThreadPoolTest {

    private static final int CPU_CORES = Runtime.getRuntime().availableProcessors();

    @Nested
    @DisplayName("ExecutorFactory")
    class ExecutorFactoryTests {

        @Test
        @DisplayName("CPU_BOUND executor should complete all submitted tasks")
        void cpuBoundShouldCompleteAllTasks() throws Exception {
            ExecutorService executor = ExecutorFactory.create(TaskType.CPU_BOUND, "test-cpu");
            int taskCount = CPU_CORES * 2;
            List<Future<Integer>> futures = new ArrayList<>();

            for (int i = 0; i < taskCount; i++) {
                final int val = i;
                futures.add(executor.submit(() -> val * val));
            }

            List<Integer> results = new ArrayList<>();
            for (Future<Integer> f : futures) {
                results.add(f.get(5, TimeUnit.SECONDS));
            }

            assertThat(results).hasSize(taskCount);
            shutdownAndAwait(executor);
        }

        @Test
        @DisplayName("IO_BOUND executor should complete all submitted tasks concurrently")
        void ioBoundShouldCompleteAllTasksConcurrently() throws Exception {
            ExecutorService executor = ExecutorFactory.create(TaskType.IO_BOUND, "test-io");
            int taskCount = 20;
            List<Future<String>> futures = new ArrayList<>();

            for (int i = 0; i < taskCount; i++) {
                final int id = i;
                futures.add(executor.submit(() -> {
                    Thread.sleep(50); // simulate I/O
                    return "task-" + id;
                }));
            }

            for (Future<String> f : futures) {
                assertThat(f.get(10, TimeUnit.SECONDS)).startsWith("task-");
            }

            shutdownAndAwait(executor);
        }

        @Test
        @DisplayName("SCHEDULED executor should fire task at least once")
        void scheduledExecutorShouldFireTask() throws Exception {
            ScheduledExecutorService scheduler =
                    ExecutorFactory.createScheduled("test-scheduler", 1);
            CountDownLatch latch = new CountDownLatch(1);

            scheduler.schedule(latch::countDown, 100, TimeUnit.MILLISECONDS);

            boolean fired = latch.await(2, TimeUnit.SECONDS);
            assertThat(fired).isTrue();

            scheduler.shutdownNow();
        }

        @Test
        @DisplayName("CPU_BOUND executor should use at most CPU core count threads")
        void cpuBoundShouldNotExceedCoreCount() {
            ExecutorService executor = ExecutorFactory.create(TaskType.CPU_BOUND, "sizing-test");
            // ForkJoinPool is bounded by parallelism = available processors
            assertThat(executor).isInstanceOf(ForkJoinPool.class);
            assertThat(((ForkJoinPool) executor).getParallelism()).isEqualTo(CPU_CORES);
            shutdownAndAwait(executor);
        }

        @Test
        @DisplayName("should throw for SCHEDULED type in create()")
        void shouldThrowForScheduledType() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ExecutorFactory.create(TaskType.SCHEDULED, "test"));
        }

        @Test
        @DisplayName("factory class should not be instantiable")
        void factoryClassShouldNotBeInstantiable() throws Exception {
            var ctor = ExecutorFactory.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            assertThatExceptionOfType(java.lang.reflect.InvocationTargetException.class)
                    .isThrownBy(ctor::newInstance)
                    .havingCause().isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("NamedThreadFactory")
    class NamedThreadFactoryTests {

        @Test
        @DisplayName("should create threads with the configured prefix")
        void shouldCreateNamedThreads() throws Exception {
            NamedThreadFactory factory = new NamedThreadFactory("my-pool", false);
            ExecutorService executor = Executors.newSingleThreadExecutor(factory);

            Future<String> result = executor.submit(() -> Thread.currentThread().getName());
            String threadName = result.get(2, TimeUnit.SECONDS);

            assertThat(threadName).startsWith("my-pool-");
            executor.shutdownNow();
        }

        @Test
        @DisplayName("daemon threads should have daemon flag set")
        void shouldCreateDaemonThreads() {
            NamedThreadFactory factory = new NamedThreadFactory("daemon-pool", true);
            Thread thread = factory.newThread(() -> {});
            assertThat(thread.isDaemon()).isTrue();
        }

        @Test
        @DisplayName("non-daemon threads should not have daemon flag")
        void shouldCreateNonDaemonThreads() {
            NamedThreadFactory factory = new NamedThreadFactory("worker-pool", false);
            Thread thread = factory.newThread(() -> {});
            assertThat(thread.isDaemon()).isFalse();
        }
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void shutdownAndAwait(ExecutorService executor) {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            executor.shutdownNow();
        }
    }
}
