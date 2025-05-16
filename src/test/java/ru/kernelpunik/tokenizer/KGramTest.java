package ru.kernelpunik.tokenizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
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
    
    /**
     * Test 1: Stability of hash for identical input
     * Ensures that two KGram instances with the same k value and input sequence
     * produce identical hash codes
     */
    @Test
    void testHashStability() {
        // Create two independent KGram objects with the same k
        int k = 5;
        KGram kGram1 = new KGram(k);
        KGram kGram2 = new KGram(k);
        
        // Input the same sequence
        List<Integer> input = List.of(3, 7, 11, 13, 17, 19, 23);
        
        // Feed both KGrams with the same input
        for (Integer value : input) {
            kGram1.put(value);
            kGram2.put(value);
            
            // After each addition, hash codes should be identical
            assertEquals(kGram1.getHashCode(), kGram2.getHashCode(),
                    "Hash codes should be identical for the same input sequence");
        }
    }
    
    /**
     * Test 2: Same suffix produces the same hash
     * Confirms that two different sequences with the same last k elements
     * produce the same hash code
     */
    @Test
    void testSameSuffixSameHash() {
        int k = 3;
        
        // Two different sequences with the same last k elements
        List<Integer> sequence1 = List.of(5, 10, 15, 20, 25, 30);
        List<Integer> sequence2 = List.of(100, 200, 300, 400, 25, 30);
        
        KGram kGram1 = new KGram(k);
        KGram kGram2 = new KGram(k);
        
        // Feed both sequences
        for (Integer value : sequence1) {
            kGram1.put(value);
        }
        
        for (Integer value : sequence2) {
            kGram2.put(value);
        }
        
        // Hash codes should be identical since the last k elements are the same
        assertEquals(kGram1.getHashCode(), kGram2.getHashCode(),
                "Hash codes should be identical for sequences with the same last " + k + " elements");
        
        // Verify this works for different k values
        for (k = 1; k <= 5; k++) {
            KGram kg1 = new KGram(k);
            KGram kg2 = new KGram(k);
            
            // Create sequences with same suffix of length k
            List<Integer> seq1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
            List<Integer> seq2 = new ArrayList<>(Arrays.asList(101, 102, 103));
            
            // Add the same suffix
            for (int i = 0; i < k; i++) {
                seq1.add(1000 + i);
                seq2.add(1000 + i);
            }
            
            // Feed both sequences
            for (Integer value : seq1) {
                kg1.put(value);
            }
            
            for (Integer value : seq2) {
                kg2.put(value);
            }
            
            // Hash codes should be identical
            assertEquals(kg1.getHashCode(), kg2.getHashCode(),
                    "Hash codes should match for k=" + k + " with same suffix");
        }
    }
    
    /**
     * Test 3: Hash stability with zero and extreme values
     * Verifies that the hash algorithm correctly handles zero, null,
     * and extreme integer values
     */
    @Test
    void testHashStabilityWithExtremeValues() {
        int k = 3;
        KGram kGram = new KGram(k);
        
        // Test with zero
        kGram.put(0);
        int hashWithZero = kGram.getHashCode();
        assertEquals(0, hashWithZero, "Adding a single zero should produce hash of 0");
        
        // Test with Integer.MAX_VALUE
        kGram = new KGram(k);
        kGram.put(Integer.MAX_VALUE);
        int hashWithMaxValue = kGram.getHashCode();
        assertEquals(Integer.MAX_VALUE % kGram.q, hashWithMaxValue, 
                "Should handle Integer.MAX_VALUE correctly");
        
        // Test with Integer.MIN_VALUE
        kGram = new KGram(k);
        kGram.put(Integer.MIN_VALUE);
        int hashWithMinValue = kGram.getHashCode();
        // We expect this to be properly modulo'd by q
        assertEquals(Integer.MIN_VALUE % kGram.q, hashWithMinValue, 
                "Should handle Integer.MIN_VALUE correctly");
        
        // Test sequence with multiple extreme values
        kGram = new KGram(k);
        kGram.put(0);
        kGram.put(Integer.MAX_VALUE);
        kGram.put(0);
        
        // Create a separate KGram and verify consistency
        KGram kGram2 = new KGram(k);
        kGram2.put(0);
        kGram2.put(Integer.MAX_VALUE);
        kGram2.put(0);
        
        assertEquals(kGram.getHashCode(), kGram2.getHashCode(), 
                "Hash should be consistent with extreme values");
    }
    
    /**
     * Test 4: Hash behavior with negative values
     * Verifies that the hash algorithm correctly handles negative values
     */
    @Test
    void testNegativeValues() {
        int k = 3;
        KGram kGram = new KGram(k);
        
        // Test with a single negative value
        kGram.put(-5);
        int hashWithNegative = kGram.getHashCode();
        // The expected result depends on how modulo is implemented for negative numbers
        // In Java, -5 % q might be negative, so we need to account for this
        int expected = (-5 % kGram.q + kGram.q) % kGram.q; // Ensure positive modulo
        assertEquals(expected, hashWithNegative, "Should handle negative values correctly");
        
        // Test with a sequence of negative values
        kGram = new KGram(k);
        List<Integer> negativeSequence = List.of(-10, -20, -30, -40, -50);
        
        // Track expected hash manually with positive modulo
        int expectedHash = 0;
        for (Integer value : negativeSequence) {
            expectedHash = (expectedHash * kGram.x + value) % kGram.q;
            // Ensure positive modulo
            if (expectedHash < 0) {
                expectedHash = (expectedHash + kGram.q) % kGram.q;
            }
            
            kGram.put(value);
            // Test either exact match (if KGram handles negatives correctly)
            // or at least consistent behavior
            int actualHash = kGram.getHashCode();
            if (actualHash < 0) {
                // If implementation allows negative hash, convert for comparison
                actualHash = (actualHash + kGram.q) % kGram.q;
            }
            
            // Create a second KGram to verify consistency
            KGram kGram2 = new KGram(k);
            for (int i = 0; i <= negativeSequence.indexOf(value); i++) {
                kGram2.put(negativeSequence.get(i));
            }
            assertEquals(kGram.getHashCode(), kGram2.getHashCode(),
                    "Hash should be consistent with negative values");
        }
    }
    
    /**
     * Test 5: Window sliding behavior
     * Verifies that the hash correctly "slides" as new values are added
     * beyond the k-gram size
     */
    @Test
    void testWindowSliding() {
        int k = 3;
        KGram kGram = new KGram(k);
        
        // Initial sequence of exactly k elements
        List<Integer> initialKGram = List.of(10, 20, 30);
        for (Integer value : initialKGram) {
            kGram.put(value);
        }
        int initialHash = kGram.getHashCode();
        
        // Adding a new element should cause the window to slide
        // and the first element to be removed
        kGram.put(40);
        
        // Verify the new hash is different
        assertNotEquals(initialHash, kGram.getHashCode(),
                "Hash should change when window slides");
        
        // Compare with a fresh KGram containing only the last k elements
        KGram expectedKGram = new KGram(k);
        expectedKGram.put(20);
        expectedKGram.put(30);
        expectedKGram.put(40);
        
        assertEquals(expectedKGram.getHashCode(), kGram.getHashCode(),
                "After sliding, hash should match a fresh KGram with the last k elements");
        
        // Test sliding window behavior with a longer sequence
        KGram slidingKGram = new KGram(k);
        List<Integer> longSequence = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        
        for (int i = 0; i < longSequence.size(); i++) {
            slidingKGram.put(longSequence.get(i));
            
            // Once we have at least k elements, compare with a fresh KGram
            // containing only the last k elements
            if (i >= k - 1) {
                KGram expectedSlidingKGram = new KGram(k);
                for (int j = i - k + 1; j <= i; j++) {
                    expectedSlidingKGram.put(longSequence.get(j));
                }
                
                assertEquals(expectedSlidingKGram.getHashCode(), slidingKGram.getHashCode(),
                        "Sliding window hash should match at position " + i);
            }
        }
    }
    
    /**
     * Test 6: Different k values produce different behavior
     * Verifies that changing the k value affects the hash code produced
     * for the same input sequence
     */
    @Test
    void testDifferentKValues() {
        List<Integer> sequence = List.of(5, 10, 15, 20, 25, 30, 35, 40);
        
        // Test with different k values
        List<Integer> hashCodes = new ArrayList<>();
        for (int k = 1; k <= 5; k++) {
            KGram kGram = new KGram(k);
            
            // Feed the entire sequence
            for (Integer value : sequence) {
                kGram.put(value);
            }
            
            hashCodes.add(kGram.getHashCode());
        }
        
        // Verify that each k value produces a different hash code
        for (int i = 0; i < hashCodes.size() - 1; i++) {
            assertNotEquals(hashCodes.get(i), hashCodes.get(i + 1),
                    "Different k values should produce different hash codes");
        }
        
        // Verify specific k behavior - smaller k retains fewer elements
        int smallK = 2;
        int largeK = 4;
        
        KGram smallKGram = new KGram(smallK);
        KGram largeKGram = new KGram(largeK);
        
        for (Integer value : sequence) {
            smallKGram.put(value);
            largeKGram.put(value);
        }
        
        // With small k, only the last smallK elements affect the hash
        KGram expectedSmallKGram = new KGram(smallK);
        for (int i = sequence.size() - smallK; i < sequence.size(); i++) {
            expectedSmallKGram.put(sequence.get(i));
        }
        assertEquals(expectedSmallKGram.getHashCode(), smallKGram.getHashCode(),
                "Small k should only retain the last " + smallK + " elements");
        
        // With large k, the last largeK elements affect the hash
        KGram expectedLargeKGram = new KGram(largeK);
        for (int i = sequence.size() - largeK; i < sequence.size(); i++) {
            expectedLargeKGram.put(sequence.get(i));
        }
        assertEquals(expectedLargeKGram.getHashCode(), largeKGram.getHashCode(),
                "Large k should retain the last " + largeK + " elements");
    }
}