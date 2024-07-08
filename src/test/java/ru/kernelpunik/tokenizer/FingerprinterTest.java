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

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}