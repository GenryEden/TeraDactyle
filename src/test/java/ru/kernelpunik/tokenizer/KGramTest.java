package ru.kernelpunik.tokenizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class KGramTest {

    @Test
    void checkEmpty() {
        for (int i = 0; i < 100; i++) {
            checkEmpty(i);
        }
    }

    @Test
    void checkSingle() {
        for (int i = 1; i < 100; i++) {
            for (int j = 0; j < 10; j++) {
                checkSingle(i, j);
            }
        }
    }

    @Test
    void checkMany() {
        List<Integer> input = List.of(4, 9, 13, 18, 2, 7, 5, 11, 38, 42);
        List<Integer> expected = List.of(4, 17, 47, 80, 90, 83, 27, 49, 80, 162);
        KGram kGram = new KGram(3);
        for (int i = 0; i < input.size(); i++) {
            kGram.put(input.get(i));
            assertEquals(expected.get(i), kGram.getHashCode());
        }
    }

    @Test
    void checkManyTrimmed() {
        List<Integer> input = List.of(2, 7, 5, 11, 38, 42);
        List<Integer> expected = List.of(2, 11, 27, 49, 80, 162); // beginning is different, but end is the same
        KGram kGram = new KGram(3);
        for (int i = 0; i < input.size(); i++) {
            kGram.put(input.get(i));
            assertEquals(expected.get(i), kGram.getHashCode());
        }
    }

    void checkSingle(int k, int v) {
        KGram kGram = new KGram(k);
        kGram.put(v);
        assertEquals(v, kGram.getHashCode());
    }

    void checkEmpty(int k) {
        KGram kGram = new KGram(k);
        assertEquals(0, kGram.getHashCode());
    }
}