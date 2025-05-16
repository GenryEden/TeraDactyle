package ru.kernelpunik.tokenizer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import org.treesitter.TreeSitterCpp;
import org.treesitter.TreeSitterJava;
import org.treesitter.TreeSitterPython;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FingerprinterTest {
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testEmptyPython() {
        testEmpty(new TreeSitterPython());
    }

    @Test
    void testEmptyCpp() {
        testEmpty(new TreeSitterCpp());
    }

    @Test
    void testEmptyJava() {
        testEmpty(new TreeSitterJava());
    }

    @Test
    void testCalculateStatisticsPython() throws IOException {
        checkByJson(
                new TreeSitterPython(),
                getPath("calculate_statistics.py"),
                getPath("calculate_statistics_py.json")
        );
    }

    @Test
    void testCalculateStatisticsModPython() throws IOException {
        checkByJson(
                new TreeSitterPython(),
                getPath("calculate_statistics_mod.py"),
                getPath("calculate_statistics_mod_py.json")
        );
    }

    @Test
    void testBasicArithmeticCpp() throws IOException {
        checkByJson(
                new TreeSitterCpp(),
                getPath("basic_arithmetic.cpp"),
                getPath("basic_arithmetic_cpp.json")
        );
    }

    @Test
    void testBasicArithmeticModCpp() throws IOException {
        checkByJson(
                new TreeSitterCpp(),
                getPath("basic_arithmetic_mod.cpp"),
                getPath("basic_arithmetic_mod_cpp.json")
        );
    }

    @Test
    void testBankingSystemJava() throws IOException {
        checkByJson(
                new TreeSitterJava(),
                getPath("banking_system.java"),
                getPath("banking_system_java.json")
        );
    }

    @Test
    void testBankingSystemModJava() throws IOException {
        checkByJson(
                new TreeSitterJava(),
                getPath("banking_system_mod.java"),
                getPath("banking_system_mod_java.json")
        );
    }

    Path getPath(String name) {
        return Path.of("src/test/resources/" + name);
    }

    void checkByJson(TSLanguage tsLanguage, Path program, Path jsonExpected) throws IOException {
        List<Integer> result = getFingerprints(tsLanguage, program);
        System.out.println(result);
        List<Integer> expected = objectMapper.readValue(Files.readString(jsonExpected), new TypeReference<>() {});
        assertIterableEquals(expected, result);
    }

    void testEmpty(TSLanguage tsLanguage) {
        List<Integer> result = getFingerprints(tsLanguage, "");
        assertTrue(result.isEmpty());
    }

    List<Integer> getFingerprints(TSLanguage tsLanguage, Path path) throws IOException {
        return getFingerprints(tsLanguage, Files.readString(path));
    }

    List<Integer> getFingerprints(TSLanguage tsLanguage, String s) {
        TSParser tsParser = new TSParser();
        tsParser.setLanguage(tsLanguage);
        Fingerprinter fingerprinter = new Fingerprinter(tsParser);
        Iterator<Integer> result = fingerprinter.getFingerprints(s);
        List<Integer> ans = new LinkedList<>();
        while (result.hasNext()) {
            ans.add(result.next());
        }
        return ans;
    }

    // New test methods for the specified requirements
    
    /**
     * Test for repeated tokens in code
     * Ensures that repetitive code patterns generate consistent fingerprints
     */
    @Test
    void testRepeatedTokens() {
        TSLanguage language = new TreeSitterJava();
        
        // Code with repeated for-loop structures
        String repeatedCode = 
            "public class RepeatedTokens {\n" +
            "    public static void main(String[] args) {\n" +
            "        for (int i = 0; i < 10; i++) {\n" +
            "            System.out.println(i);\n" +
            "        }\n" +
            "        for (int j = 0; j < 10; j++) {\n" +
            "            System.out.println(j);\n" +
            "        }\n" +
            "        for (int k = 0; k < 10; k++) {\n" +
            "            System.out.println(k);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        
        // Code with a single for-loop repeated inline
        String nonRepeatedCode = 
            "public class NonRepeatedTokens {\n" +
            "    public static void main(String[] args) {\n" +
            "        for (int i = 0; i < 30; i++) {\n" +
            "            System.out.println(i);\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
            
        List<Integer> repeatedTokensFingerprint = getFingerprints(language, repeatedCode);
        List<Integer> nonRepeatedTokensFingerprint = getFingerprints(language, nonRepeatedCode);
        
        // Validate the fingerprints are different (the repetition pattern is preserved)
        assertNotEquals(repeatedTokensFingerprint, nonRepeatedTokensFingerprint);
        
        // Repeated patterns should result in more fingerprint entries
        assertTrue(repeatedTokensFingerprint.size() > nonRepeatedTokensFingerprint.size(),
            "Repeated code patterns should generate more fingerprint entries");
    }
    
    /**
     * Test for tolerance to variable renaming
     * Ensures that fingerprints are consistent even when variable names change
     */
    @Test
    void testRenamingTolerance() {
        TSLanguage language = new TreeSitterJava();
        
        // Original code
        String originalCode = 
            "public class Original {\n" +
            "    public static void main(String[] args) {\n" +
            "        int counter = 0;\n" +
            "        while (counter < 10) {\n" +
            "            System.out.println(\"Count: \" + counter);\n" +
            "            counter++;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
            
        // Same code with renamed variables
        String renamedCode = 
            "public class Renamed {\n" +
            "    public static void main(String[] parameters) {\n" +
            "        int i = 0;\n" +
            "        while (i < 10) {\n" +
            "            System.out.println(\"Count: \" + i);\n" +
            "            i++;\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
            
        List<Integer> originalFingerprint = getFingerprints(language, originalCode);
        List<Integer> renamedFingerprint = getFingerprints(language, renamedCode);
        
        // Assert they have the same fingerprint length
        assertEquals(originalFingerprint.size(), renamedFingerprint.size(), 
            "Renamed code should have same fingerprint length");
            
        // Ideally, most fingerprints should be identical after renaming
        // We'll count how many are the same
        int matchingFingerprints = 0;
        for (int i = 0; i < originalFingerprint.size(); i++) {
            if (originalFingerprint.get(i).equals(renamedFingerprint.get(i))) {
                matchingFingerprints++;
            }
        }
        
        // Expect significant similarity (at least 70%)
        double similarityRatio = (double) matchingFingerprints / originalFingerprint.size();
        assertTrue(similarityRatio >= 0.7, 
            "After renaming, fingerprints should be at least 70% similar, but was " + similarityRatio * 100 + "%");
    }
    
    /**
     * Test for boundary conditions with short code
     * Tests behavior on very short code, with length less than window size and k
     */
    @Test
    void testBoundaryConditions() {
        TSLanguage language = new TreeSitterJava();
        int k = 20; // Default k value
        int windowSize = 100; // Default window size
        
        // Create custom Fingerprinter for boundary testing
        TSParser tsParser = new TSParser();
        tsParser.setLanguage(language);
        Fingerprinter customFingerprinter = new Fingerprinter(tsParser, k, windowSize);
        
        // Very short code (less than k tokens)
        String veryShortCode = "int x = 5;";
        
        // Short code (more than k but less than window size)
        String shortCode = 
            "public class Short {\n" +
            "    public static void main(String[] args) {\n" +
            "        int x = 5;\n" +
            "        System.out.println(x);\n" +
            "    }\n" +
            "}\n";
            
        Iterator<Integer> veryShortResult = customFingerprinter.getFingerprints(veryShortCode);
        List<Integer> veryShortFingerprints = new LinkedList<>();
        while (veryShortResult.hasNext()) {
            veryShortFingerprints.add(veryShortResult.next());
        }
        
        Iterator<Integer> shortResult = customFingerprinter.getFingerprints(shortCode);
        List<Integer> shortFingerprints = new LinkedList<>();
        while (shortResult.hasNext()) {
            shortFingerprints.add(shortResult.next());
        }
        
        // Very short code may still produce fingerprints if at least one k-gram is completed
        if (veryShortCode.length() < k) {
            assertTrue(veryShortFingerprints.size() <= 1, 
                "Very short code should produce at most one fingerprint");
        }
        
        // Short code should produce some fingerprints but fewer than the window size
        assertTrue(shortFingerprints.size() > 0, 
            "Short code should produce at least one fingerprint");
        assertTrue(shortFingerprints.size() < windowSize, 
            "Short code should produce fewer fingerprints than the window size");
    }
    
    /**
     * Test how parameters affect fingerprint length
     * Verifies that different k and windowSize parameters produce different length fingerprints
     */
    @Test
    void testParameterImpactOnFingerprintLength() {
        TSLanguage language = new TreeSitterJava();
        
        String testCode = 
            "public class Test {\n" +
            "    public static void main(String[] args) {\n" +
            "        int sum = 0;\n" +
            "        for (int i = 1; i <= 100; i++) {\n" +
            "            sum += i;\n" +
            "            System.out.println(\"Running sum: \" + sum);\n" +
            "        }\n" +
            "        System.out.println(\"Final sum: \" + sum);\n" +
            "    }\n" +
            "}\n";
            
        TSParser tsParser = new TSParser();
        tsParser.setLanguage(language);
        
        // Create fingerprinters with different parameters
        Fingerprinter defaultFingerprinter = new Fingerprinter(tsParser); // k=20, window=100
        Fingerprinter smallerKFingerprinter = new Fingerprinter(tsParser, 10, 100); // k=10, window=100
        Fingerprinter largerWindowFingerprinter = new Fingerprinter(tsParser, 20, 200); // k=20, window=200
        
        // Get fingerprints for each configuration
        List<Integer> defaultFingerprints = convertIteratorToList(defaultFingerprinter.getFingerprints(testCode));
        List<Integer> smallerKFingerprints = convertIteratorToList(smallerKFingerprinter.getFingerprints(testCode));
        List<Integer> largerWindowFingerprints = convertIteratorToList(largerWindowFingerprinter.getFingerprints(testCode));
        
        // Smaller k should produce more fingerprints
        assertTrue(smallerKFingerprints.size() >= defaultFingerprints.size(),
            "Smaller k should produce more fingerprints");
            
        // Larger window should affect how fingerprints are selected
        assertNotEquals(defaultFingerprints, largerWindowFingerprints,
            "Different window sizes should produce different fingerprints");
            
        // Output the fingerprint lengths for comparison
        System.out.println("Default parameters (k=20, window=100): " + defaultFingerprints.size() + " fingerprints");
        System.out.println("Smaller k (k=10, window=100): " + smallerKFingerprints.size() + " fingerprints");
        System.out.println("Larger window (k=20, window=200): " + largerWindowFingerprints.size() + " fingerprints");
    }
    
    /**
     * Helper method to convert an Iterator to a List
     */
    private List<Integer> convertIteratorToList(Iterator<Integer> iterator) {
        List<Integer> list = new LinkedList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }
}