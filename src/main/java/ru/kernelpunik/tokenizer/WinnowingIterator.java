package ru.kernelpunik.tokenizer;

import java.util.*;

public class WinnowingIterator implements Iterator<Integer> {
    private final Iterator<Integer> iterator;
    private final int[] window;
    private final int k;
    private boolean filled = false;
    private int counter = 0;
    private int minValue;
    public WinnowingIterator(Iterator<Integer> iterator, int k) {
        this.iterator = iterator;
        this.k = k;
        this.window = new int[k];
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public Integer next() {
        int ans;
        while (true) {
            int prevValue = window[counter];
            int newValue = iterator.next();
            window[counter] = newValue;
            counter = (counter + 1) % window.length;
            if (counter == 0) {
                filled = true;
            }
            if (!filled) {
                if (!iterator.hasNext()) {
                    return newValue;
                } else {
                    continue;
                }
            }
            if (prevValue == minValue) {
                minValue = Integer.MAX_VALUE;
                for (int j : window) {
                    minValue = Math.min(minValue, j);
                }
                return minValue;
            }
            if (newValue < minValue) {
                minValue = newValue;
                ans = newValue;
                break;
            }
            if (!iterator.hasNext()) {
                return newValue;
            }
        }
        return ans;
    }
}
