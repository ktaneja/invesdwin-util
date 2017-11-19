package de.invesdwin.util.collections.eviction;

import javax.annotation.concurrent.Immutable;

@Immutable
public enum EvictionMode {

    LeastRecentlyAdded {
        @Override
        public <K, V> IEvictionMap<K, V> newMap(final int maximumSize) {
            return new LeastRecentlyAddedMap<>(maximumSize);
        }
    },
    LeastRecentlyModified {
        @Override
        public <K, V> IEvictionMap<K, V> newMap(final int maximumSize) {
            return new LeastRecentlyModifiedMap<>(maximumSize);
        }
    },
    LeastRecentlyUsed {
        @Override
        public <K, V> IEvictionMap<K, V> newMap(final int maximumSize) {
            return new LeastRecentlyUsedMap<>(maximumSize);
        }
    };

    public abstract <K, V> IEvictionMap<K, V> newMap(int maximumSize);

}
