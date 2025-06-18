package ru.kernelpunik.tokenizer;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;

public class FilterIterator<T> implements Iterator<T> {
    private final Iterator<T> inner;
    private final Predicate<? super T> predicate;
    private T nextMatch;
    private boolean nextMatchIsReady = false;

    public FilterIterator(Iterator<T> inner, Predicate<? super T> predicate) {
        this.inner = inner;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext() {
        if (nextMatchIsReady) {
            return true;
        }
        while (inner.hasNext()) {
            T candidate = inner.next();
            if (predicate.test(candidate)) {
                nextMatch = candidate;
                nextMatchIsReady = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if (!nextMatchIsReady && !hasNext()) {
            throw new NoSuchElementException();
        }
        nextMatchIsReady = false;
        return nextMatch;
    }
}
