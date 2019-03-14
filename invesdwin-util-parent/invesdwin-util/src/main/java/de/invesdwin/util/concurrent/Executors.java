package de.invesdwin.util.concurrent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.Immutable;

import com.google.common.util.concurrent.MoreExecutors;

import de.invesdwin.util.concurrent.internal.WrappedThreadFactory;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * As an alternative to the java executors class. Here more conventions are kept for all executors.
 * 
 * @author subes
 * 
 */
@Immutable
public final class Executors {

    /**
     * This executor does not actually run tasks in parallel but instead runs them directly in the callers thread
     */
    public static final WrappedExecutorService DISABLED_EXECUTOR = new WrappedExecutorService(
            MoreExecutors.newDirectExecutorService(), "DISABLED") {
        @Override
        public void shutdown() {
            //noop
        }

        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }
    }.withDynamicThreadName(false);

    private static int cpuThreadPoolCount = Runtime.getRuntime().availableProcessors();

    private Executors() {}

    /**
     * @see java.util.concurrent.Executors.newCachedThreadPool
     */
    public static WrappedExecutorService newCachedThreadPool(final String name) {
        final java.util.concurrent.ThreadPoolExecutor ex = (java.util.concurrent.ThreadPoolExecutor) java.util.concurrent.Executors
                .newCachedThreadPool(newFastThreadLocalThreadFactory(name));
        return new WrappedExecutorService(ex, name);
    }

    public static WrappedThreadFactory newFastThreadLocalThreadFactory(final String name) {
        return new WrappedThreadFactory(name, new DefaultThreadFactory(name));
    }

    /**
     * @see java.util.concurrent.Executors.newFixedThreadPool
     */
    public static WrappedExecutorService newFixedThreadPool(final String name, final int nThreads) {
        final java.util.concurrent.ThreadPoolExecutor ex = (java.util.concurrent.ThreadPoolExecutor) java.util.concurrent.Executors
                .newFixedThreadPool(nThreads, newFastThreadLocalThreadFactory(name));
        return new WrappedExecutorService(ex, name);
    }

    /**
     * @see java.util.concurrent.Executors.newScheduledThreadPool
     */
    public static WrappedScheduledExecutorService newScheduledThreadPool(final String name) {
        final java.util.concurrent.ScheduledThreadPoolExecutor ex = (java.util.concurrent.ScheduledThreadPoolExecutor) java.util.concurrent.Executors
                .newScheduledThreadPool(Integer.MAX_VALUE, newFastThreadLocalThreadFactory(name));
        return new WrappedScheduledExecutorService(ex, name).withDynamicThreadName(false);
    }

    /**
     * @see java.util.concurrent.Executors.newScheduledThreadPool
     */
    public static WrappedScheduledExecutorService newScheduledThreadPool(final String name, final int corePoolSize) {
        final java.util.concurrent.ScheduledThreadPoolExecutor ex = (java.util.concurrent.ScheduledThreadPoolExecutor) java.util.concurrent.Executors
                .newScheduledThreadPool(corePoolSize, newFastThreadLocalThreadFactory(name));
        return new WrappedScheduledExecutorService(ex, name).withDynamicThreadName(false);
    }

    public static WrappedExecutorService newFixedCallerRunsThreadPool(final String name, final int nThreads) {
        final java.util.concurrent.ThreadPoolExecutor ex = new java.util.concurrent.ThreadPoolExecutor(nThreads,
                nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(nThreads),
                newFastThreadLocalThreadFactory(name), new CallerRunsPolicy());
        return new WrappedExecutorService(ex, name);
    }

    /**
     * Returns the number of cpu cores for ThreadPools that are cpu intensive.
     */
    public static int getCpuThreadPoolCount() {
        return Executors.cpuThreadPoolCount;
    }

    public static void setCpuThreadPoolCount(final int cpuThreadPoolCount) {
        Executors.cpuThreadPoolCount = cpuThreadPoolCount;
    }

    public static ConfiguredForkJoinPool newForkJoinPool(final String name, final int parallelism) {
        return new ConfiguredForkJoinPool(name, parallelism, false);
    }

    public static ConfiguredForkJoinPool newAsyncForkJoinPool(final String name, final int parallelism) {
        return new ConfiguredForkJoinPool(name, parallelism, true);
    }

}