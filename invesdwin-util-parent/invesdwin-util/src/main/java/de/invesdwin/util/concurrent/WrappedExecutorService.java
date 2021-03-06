package de.invesdwin.util.concurrent;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import de.invesdwin.util.collections.factory.ILockCollectionFactory;
import de.invesdwin.util.collections.fast.IFastIterableSet;
import de.invesdwin.util.collections.loadingcache.ALoadingCache;
import de.invesdwin.util.concurrent.future.InterruptingFuture;
import de.invesdwin.util.concurrent.internal.IWrappedExecutorServiceInternal;
import de.invesdwin.util.concurrent.internal.WrappedCallable;
import de.invesdwin.util.concurrent.internal.WrappedRunnable;
import de.invesdwin.util.concurrent.internal.WrappedThreadFactory;
import de.invesdwin.util.concurrent.lock.Locks;
import de.invesdwin.util.shutdown.IShutdownHook;
import de.invesdwin.util.shutdown.ShutdownHookManager;
import de.invesdwin.util.time.duration.Duration;
import de.invesdwin.util.time.fdate.FTimeUnit;

@ThreadSafe
public class WrappedExecutorService implements ExecutorService {

    private static final Duration FIXED_THREAD_KEEPALIVE_TIMEOUT = new Duration(60, FTimeUnit.SECONDS);

    protected final IWrappedExecutorServiceInternal internal = new IWrappedExecutorServiceInternal() {

        @Override
        public boolean isLogExceptions() {
            return WrappedExecutorService.this.isLogExceptions();
        }

        @Override
        public boolean isDynamicThreadName() {
            return WrappedExecutorService.this.isDynamicThreadName();
        }

        @Override
        public void incrementPendingCount(final boolean skipWaitOnFullPendingCount) throws InterruptedException {
            WrappedExecutorService.this.incrementPendingCount(skipWaitOnFullPendingCount);
        }

        @Override
        public void decrementPendingCount() {
            WrappedExecutorService.this.decrementPendingCount();
        }

        @Override
        public String getName() {
            return WrappedExecutorService.this.getName();
        }

    };
    private final Lock pendingCountLock;
    private final ALoadingCache<Integer, Condition> pendingCount_condition = new ALoadingCache<Integer, Condition>() {
        @Override
        protected Condition loadValue(final Integer key) {
            return pendingCountLock.newCondition();
        }
    };
    private final IFastIterableSet<IPendingCountListener> pendingCountListeners = ILockCollectionFactory
            .getInstance(true)
            .newFastIterableLinkedSet();
    private final AtomicInteger pendingCount = new AtomicInteger();
    private final Object pendingCountWaitLock = new Object();
    private final ExecutorService delegate;
    private volatile boolean logExceptions = true;
    private volatile boolean waitOnFullPendingCount = false;
    private volatile boolean dynamicThreadName = true;
    private final String name;

    @GuardedBy("this")
    private IShutdownHook shutdownHook;

    protected WrappedExecutorService(final ExecutorService delegate, final String name) {
        this.delegate = delegate;
        this.shutdownHook = newShutdownHook(delegate);
        this.name = name;
        this.pendingCountLock = Locks
                .newReentrantLock(WrappedExecutorService.class.getSimpleName() + "_" + name + "_pendingCountLock");
        configure();
    }

    /**
     * Prevent reference leak to this instance by using a static method
     */
    private static IShutdownHook newShutdownHook(final ExecutorService delegate) {
        return new IShutdownHook() {
            @Override
            public void shutdown() throws Exception {
                delegate.shutdownNow();
            }
        };
    }

    public boolean isLogExceptions() {
        return logExceptions;
    }

    public WrappedExecutorService withLogExceptions(final boolean logExceptions) {
        this.logExceptions = logExceptions;
        return this;
    }

    public WrappedExecutorService withDynamicThreadName(final boolean dynamicThreadName) {
        this.dynamicThreadName = dynamicThreadName;
        return this;
    }

    public boolean isDynamicThreadName() {
        return dynamicThreadName;
    }

    public String getName() {
        return name;
    }

    private void incrementPendingCount(final boolean skipWaitOnFullPendingCount) throws InterruptedException {
        if (waitOnFullPendingCount && !skipWaitOnFullPendingCount) {
            synchronized (pendingCountWaitLock) {
                //Only one waiting thread may be woken up when this limit is reached!
                while (pendingCount.get() >= getFullPendingCount()) {
                    awaitPendingCount(getMaximumPoolSize() - 1);
                }
                notifyPendingCountListeners(pendingCount.incrementAndGet());
            }
        } else {
            notifyPendingCountListeners(pendingCount.incrementAndGet());
        }
    }

    private void decrementPendingCount() {
        notifyPendingCountListeners(pendingCount.decrementAndGet());
    }

    private void notifyPendingCountListeners(final int currentPendingCount) {
        pendingCountLock.lock();
        try {
            if (!pendingCount_condition.isEmpty()) {
                for (final Entry<Integer, Condition> e : pendingCount_condition.entrySet()) {
                    final Integer limit = e.getKey();
                    if (currentPendingCount <= limit) {
                        final Condition condition = e.getValue();
                        if (condition != null) {
                            condition.signalAll();
                        }
                    }
                }
            }
            if (!pendingCountListeners.isEmpty()) {
                final IPendingCountListener[] array = pendingCountListeners.asArray(IPendingCountListener.class);
                for (int i = 0; i < array.length; i++) {
                    array[i].onPendingCountChanged(currentPendingCount);
                }
            }
        } finally {
            pendingCountLock.unlock();
        }
    }

    private synchronized void configure() {
        /*
         * All executors should be shutdown on application shutdown.
         */
        ShutdownHookManager.register(shutdownHook);

        if (delegate instanceof java.util.concurrent.ThreadPoolExecutor) {
            final java.util.concurrent.ThreadPoolExecutor cDelegate = (java.util.concurrent.ThreadPoolExecutor) delegate;
            /*
             * All threads should stop after 60 seconds of idle time
             */
            cDelegate.setKeepAliveTime(FIXED_THREAD_KEEPALIVE_TIMEOUT.longValue(),
                    FIXED_THREAD_KEEPALIVE_TIMEOUT.getTimeUnit().timeUnitValue());
            /*
             * Fixes non starting with corepoolsize von 0 and not filled queue (Java Conurrency In Practice Chapter
             * 8.3.1). If this bug can occur, a exception would be thrown here.
             */
            cDelegate.allowCoreThreadTimeOut(true);
            /*
             * Named threads improve debugging.
             */
            final WrappedThreadFactory threadFactory;
            if (cDelegate.getThreadFactory() instanceof WrappedThreadFactory) {
                threadFactory = (WrappedThreadFactory) cDelegate.getThreadFactory();
            } else {
                threadFactory = new WrappedThreadFactory(name, cDelegate.getThreadFactory());
                cDelegate.setThreadFactory(threadFactory);
            }
            threadFactory.setParent(internal);

        }
    }

    private synchronized void unconfigure() {
        if (shutdownHook != null) {
            ShutdownHookManager.unregister(shutdownHook);
        }
        shutdownHook = null;
    }

    public boolean isWaitOnFullPendingCount() {
        return waitOnFullPendingCount;
    }

    public WrappedExecutorService withWaitOnFullPendingCount(final boolean waitOnFullPendingCount) {
        this.waitOnFullPendingCount = waitOnFullPendingCount;
        return this;
    }

    public ExecutorService getWrappedInstance() {
        return delegate;
    }

    @Override
    public void shutdown() {
        getWrappedInstance().shutdown();
        unconfigure();
    }

    @Override
    public List<Runnable> shutdownNow() {
        final List<Runnable> l = getWrappedInstance().shutdownNow();
        unconfigure();
        return l;
    }

    @Override
    public boolean isShutdown() {
        return getWrappedInstance().isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return getWrappedInstance().isTerminated();
    }

    @Override
    public boolean awaitTermination(final long timeout, final TimeUnit unit) throws InterruptedException {
        return getWrappedInstance().awaitTermination(timeout, unit);
    }

    public boolean awaitTermination() throws InterruptedException {
        return awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    public int getPendingCount() {
        return pendingCount.get();
    }

    /**
     * Waits for pendingCount to shrink to a given limit size.
     * 
     * WARNING: Pay attention when using this feature so that executors don't have circular task dependencies. If they
     * depend on each others pendingCount, this may cause a deadlock!
     */
    public void awaitPendingCount(final int limit) throws InterruptedException {
        pendingCountLock.lock();
        try {
            final Condition condition = pendingCount_condition.get(limit);
            while (getPendingCount() > limit) {
                Threads.throwIfInterrupted();
                condition.await();
            }
        } finally {
            pendingCountLock.unlock();
        }
    }

    public void waitOnFullPendingCount() throws InterruptedException {
        awaitPendingCount(getFullPendingCount());
    }

    public int getMaximumPoolSize() {
        final ExecutorService delegate = getWrappedInstance();
        if (delegate instanceof java.util.concurrent.ThreadPoolExecutor) {
            final java.util.concurrent.ThreadPoolExecutor cDelegate = (java.util.concurrent.ThreadPoolExecutor) delegate;
            return cDelegate.getMaximumPoolSize();
        }
        return 0;
    }

    public int getFullPendingCount() {
        return getMaximumPoolSize();
    }

    @Override
    public void execute(final Runnable command) {
        try {
            getWrappedInstance().execute(WrappedRunnable.newInstance(internal, command));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        try {
            return getWrappedInstance().submit(WrappedCallable.newInstance(internal, task));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new InterruptingFuture<T>();
        }
    }

    @Override
    public <T> Future<T> submit(final Runnable task, final T result) {
        try {
            return getWrappedInstance().submit(WrappedRunnable.newInstance(internal, task), result);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new InterruptingFuture<T>();
        }
    }

    @Override
    public Future<?> submit(final Runnable task) {
        try {
            return getWrappedInstance().submit(WrappedRunnable.newInstance(internal, task));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return new InterruptingFuture<Object>();
        }
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return getWrappedInstance().invokeAll(WrappedCallable.newInstance(internal, tasks));
    }

    @Override
    public <T> List<Future<T>> invokeAll(final Collection<? extends Callable<T>> tasks, final long timeout,
            final TimeUnit unit) throws InterruptedException {
        return getWrappedInstance().invokeAll(WrappedCallable.newInstance(internal, tasks), timeout, unit);
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks)
            throws InterruptedException, ExecutionException {
        return getWrappedInstance().invokeAny(WrappedCallable.newInstance(internal, tasks));
    }

    @Override
    public <T> T invokeAny(final Collection<? extends Callable<T>> tasks, final long timeout, final TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return getWrappedInstance().invokeAny(WrappedCallable.newInstance(internal, tasks), timeout, unit);
    }

    public IFastIterableSet<IPendingCountListener> getPendingCountListeners() {
        return pendingCountListeners;
    }

}
