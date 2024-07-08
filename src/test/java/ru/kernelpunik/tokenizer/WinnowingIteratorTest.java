package ru.kernelpunik.tokenizer;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WinnowingIteratorTest {
    @Test
    void checkEmpty() {
        for (int i = 0; i < 100; i++) {
            checkEmpty(i);
        }
    }

    void checkEmpty(int k) {
        var iterator = new WinnowingIterator(List.<Integer>of().iterator(), k);
        assertFalse(iterator.hasNext());
    }

    @Test
    void checkSingle() {
        for (int k = 1; k < 100; k++) {
            checkSingle(k);
        }
    }

    @Test
    void checkMany() {
        List<Integer> input = List.of(77, 72, 42, 17, 98, 50, 17, 98, 8, 88, 67, 39, 77, 72, 42, 17,98);
        List<Integer> expected = List.of(17, 17, 8, 39, 17, 98);
        WinnowingIterator winnowingIterator = new WinnowingIterator(input.iterator(), 4);
        assertIterableEquals(expected, (Iterable<Integer>) () -> winnowingIterator);
    }

    void checkSingle(int k) {
        var iterator = new WinnowingIterator(List.of(1).iterator(), k);
        assertEquals(1, iterator.next());
        assertFalse(iterator.hasNext());
    }


}