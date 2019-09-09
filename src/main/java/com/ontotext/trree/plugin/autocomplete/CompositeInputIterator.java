package com.ontotext.trree.plugin.autocomplete;

import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.util.BytesRef;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * Combines multiple InputIterators into a single virtual InputIterator.
 */
public class CompositeInputIterator implements InputIterator, Closeable, AutoCloseable {
    private Collection<InputIterator> delegates;
    private Iterator<InputIterator> delegatesIterator;
    private InputIterator delegate;

    CompositeInputIterator(Collection<InputIterator> delegates) {
        this.delegates = delegates;
        delegatesIterator = delegates.iterator();
        if (delegatesIterator.hasNext()) {
            delegate = delegatesIterator.next();
        } else {
            delegate = InputIterator.EMPTY;
        }
    }

    @Override
    public long weight() {
        return delegate.weight();
    }

    @Override
    public BytesRef payload() {
        return delegate.payload();
    }

    @Override
    public boolean hasPayloads() {
        return true;
    }

    @Override
    public Set<BytesRef> contexts() {
        return delegate.contexts();
    }

    @Override
    public boolean hasContexts() {
        return true;
    }

    @Override
    public BytesRef next() throws IOException {
        BytesRef next = delegate.next();
        if (next == null && delegatesIterator.hasNext()) {
            delegate = delegatesIterator.next();
            return next();
        }

        return next;
    }

    @Override
    public void close() throws IOException {
        for (InputIterator itty : delegates) {
            if (itty instanceof Closeable) {
                ((Closeable) itty).close();
            }
        }
    }
}
