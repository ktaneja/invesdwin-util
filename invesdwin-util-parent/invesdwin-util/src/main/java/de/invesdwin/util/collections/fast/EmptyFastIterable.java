package de.invesdwin.util.collections.fast;

import javax.annotation.concurrent.Immutable;

import de.invesdwin.util.collections.iterable.EmptyCloseableIterator;
import de.invesdwin.util.collections.iterable.ICloseableIterator;
import de.invesdwin.util.lang.Objects;

@Immutable
public final class EmptyFastIterable<E> implements IFastIterable<E> {

    @SuppressWarnings("rawtypes")
    private static final EmptyFastIterable INSTANCE = new EmptyFastIterable<>();

    @SuppressWarnings("unchecked")
    private final E[] array = (E[]) Objects.EMPTY_ARRAY;

    private EmptyFastIterable() {}

    @Override
    public ICloseableIterator<E> iterator() {
        return EmptyCloseableIterator.getInstance();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(final E value) {
        return false;
    }

    @Override
    public E[] asArray(final Class<E> type) {
        return array;
    }

    @SuppressWarnings("unchecked")
    public static <T> EmptyFastIterable<T> getInstance() {
        return INSTANCE;
    }

}
