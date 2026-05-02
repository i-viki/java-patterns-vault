package dev.jayav.patterns.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom {@link ThreadFactory} that names threads for observability.
 *
 * <p>Named threads are essential in production — they appear in thread dumps,
 * profilers (VisualVM, JFR), and APM tools (Datadog, New Relic), making
 * debugging blocked or runaway threads significantly easier.</p>
 *
 * <p>Example thread name: {@code image-resize-cpu-1}, {@code db-fetch-io-3}</p>
 *
 * @author Jaya V
 * @version 1.0.0
 */
public class NamedThreadFactory implements ThreadFactory {

    private final String prefix;
    private final boolean daemon;
    private final AtomicInteger counter = new AtomicInteger(1);

    /**
     * Creates a named thread factory.
     *
     * @param prefix the prefix for all thread names (e.g., "image-resize-cpu")
     * @param daemon {@code true} to create daemon threads (JVM won't wait for them at shutdown)
     */
    public NamedThreadFactory(String prefix, boolean daemon) {
        this.prefix = prefix;
        this.daemon = daemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread thread = new Thread(r, prefix + "-" + counter.getAndIncrement());
        thread.setDaemon(daemon);
        thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }
}
