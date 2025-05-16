package ru.kernelpunik.tokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class ConcatIterator<T> implements Iterator<T> {
    private final List<Iterator<T>> iterators;
    private int current = 0;

    public ConcatIterator(Iterator<T>... iterators) {
        this.iterators = Arrays.asList(iterators);
    }

    public ConcatIterator(Iterable<T>... iterables) {
        this.iterators = new ArrayList<>(iterables.length);
        for (Iterable<T> iterable : iterables) {
            iterators.add(iterable.iterator());
        }
    }

    @Override
    public boolean hasNext() {
        return current == iterators.size();
    }

    @Override
    public T next() {
        if (hasNext()) {
            throw new NoSuchElementException();
        }
        T ans = iterators.get(current).next();
        if (iterators.get(current).hasNext()) {
            current++;
        }
        return ans;
    }
}
