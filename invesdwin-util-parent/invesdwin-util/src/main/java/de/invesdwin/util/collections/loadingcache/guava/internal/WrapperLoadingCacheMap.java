package de.invesdwin.util.collections.loadingcache.guava.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.cache.LoadingCache;

import de.invesdwin.util.collections.loadingcache.ILoadingCacheMap;

/**
 * Because LoadingCache.asMap() does not load values, this acts as a workaround.
 */
@ThreadSafe
public class WrapperLoadingCacheMap<K, V> implements ILoadingCacheMap<K, V> {

    private final LoadingCache<K, V> delegate;
    private final Map<K, V> delegateAsMap;

    public WrapperLoadingCacheMap(final LoadingCache<K, V> delegate) {
        this.delegate = delegate;
        this.delegateAsMap = delegate.asMap();
    }

    @Override
    public int size() {
        return delegateAsMap.size();
    }

    @Override
    public boolean isEmpty() {
        return delegateAsMap.isEmpty();
    }

    @Override
    public boolean containsKey(final Object key) {
        return delegateAsMap.containsKey(key);
    }

    @Override
    public boolean containsValue(final Object value) {
        return delegateAsMap.containsValue(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(final Object key) {
        try {
            return delegate.getUnchecked((K) key);
        } catch (final ClassCastException e) {
            return null;
        }
    }

    @Override
    public V put(final K key, final V value) {
        return delegateAsMap.put(key, value);
    }

    @Override
    public V putIfAbsent(final K key, final V value) {
        return delegateAsMap.putIfAbsent(key, value);
    }

    @Override
    public V remove(final Object key) {
        return delegateAsMap.remove(key);
    }

    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        delegateAsMap.putAll(m);
    }

    @Override
    public void clear() {
        delegateAsMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegateAsMap.keySet();
    }

    @Override
    public Collection<V> values() {
        return delegateAsMap.values();
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return delegateAsMap.entrySet();
    }

    @Override
    public V getIfPresent(final K key) {
        return delegate.getIfPresent(key);
    }

    @Override
    public V computeIfAbsent(final K key, final Function<? super K, ? extends V> mappingFunction) {
        try {
            return delegate.get(key, new Callable<V>() {
                @Override
                public V call() throws Exception {
                    return mappingFunction.apply(key);
                }
            });
        } catch (final ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}
