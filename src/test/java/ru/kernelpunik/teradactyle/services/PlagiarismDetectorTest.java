package ru.kernelpunik.teradactyle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.kernelpunik.teradactyle.AntiplagiatApplication;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.models.Solution;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class PlagiarismDetectorTest {
    AtomicInteger counter;


    private final IPlagiarismDetectorService plagiarismDetectorService;

    public PlagiarismDetectorTest(IPlagiarismDetectorService plagiarismDetectorService) {
        this.plagiarismDetectorService = plagiarismDetectorService;
    }

    @BeforeEach
    void beforeEach() {
        counter = new AtomicInteger(0);
    }

    @Test
    void checkPythonCalculateStatistics() throws IOException {
        Path path = getPath("calculate_statistics.py");
        Solution solution = createDefaultSolution(Language.PYTHON, path);
        checkSame(solution);
    }

    @Test
    void checkPythonCalculateStatisticsMod() throws IOException {
        Path path = getPath("calculate_statistics_mod.py");
        Solution solution = createDefaultSolution(Language.PYTHON, path);
        checkSame(solution);
    }

    @Test
    void checkCppBasicArithmetic() throws IOException {
        Path path = getPath("basic_arithmetic.cpp");
        Solution solution = createDefaultSolution(Language.CPP, path);
        checkSame(solution);
    }

    @Test
    void checkCppBasicArithmeticMod() throws IOException {
        Path path = getPath("basic_arithmetic_mod.cpp");
        Solution solution = createDefaultSolution(Language.CPP, path);
        checkSame(solution);
    }

    @Test
    void checkJavaBankingSystem() throws IOException {
        Path path = getPath("banking_system.java");
        Solution solution = createDefaultSolution(Language.JAVA, path);
        checkSame(solution);
    }

    @Test
    void checkJavaBankingSystemMod() throws IOException {
        Path path = getPath("banking_system_mod.java");
        Solution solution = createDefaultSolution(Language.JAVA, path);
        checkSame(solution);
    }

    @Test
    void checkPython() throws IOException {
        Path path1 = getPath("calculate_statistics.py");
        Path path2 = getPath("calculate_statistics_mod.py");
        test2(Language.PYTHON, path1, path2, 0.20512820780277252);
        test2(Language.PYTHON, path2, path1, 0.3855421543121338);
    }

    @Test
    void checkJava() throws IOException {
        Path path1 = getPath("banking_system.java");
        Path path2 = getPath("banking_system_mod.java");
        test2(Language.JAVA, path1, path2, 0.4399999976158142);
        test2(Language.JAVA, path2, path1, 0.6143497824668884);
    }

    @Test
    void checkCpp() throws IOException {
        Path path1 = getPath("basic_arithmetic.cpp");
        Path path2 = getPath("basic_arithmetic_mod.cpp");
        test2(Language.CPP, path1, path2, 0.16428571939468384);
        test2(Language.CPP, path2, path1, 0.34408602118492126);
    }

    void test2(Language language, Path path1, Path path2, double interferenceVal) throws IOException {
        Solution solution1 = createDefaultSolution(language, path1);
        Solution solution2 = createDefaultSolution(language, path2);
        plagiarismDetectorService.putSolution(solution1);
        List<Interference> interference1 = plagiarismDetectorService.getInterferences(solution1.getSolutionId());
        for (Interference interference : interference1) {
            assertNotSame(interference.getInterferedSolutionId(), solution1.getSolutionId());
        }
        plagiarismDetectorService.putSolution(solution2);
        List<Interference> interference2 = plagiarismDetectorService.getInterferences(solution2.getSolutionId());
        boolean metFirst = false;
        for (Interference interf : interference2) {
            if (interf.getInterferedSolutionId() == solution1.getSolutionId()) {
                assertEquals(interferenceVal, interf.getInterferenceFraction(), 1e-3);
                metFirst = true;
                break;
            }
        }
        assertTrue(metFirst);
    }

    Solution createDefaultSolution(Language language, Path file) throws IOException {
        Solution solution = new Solution();
        solution.setName("Solution #" + counter.incrementAndGet());
        solution.setCode(Files.readString(file));
        solution.setLanguageId(language.id);
        solution.setDescription("Solution is written in " + language.name);
        return solution;
    }

    void checkSame(Solution solution) {
        Solution firstSolution = loadAndCheck(solution);
        Solution secondSolution = loadAndCheck(copy(solution));
        assertSame(firstSolution, secondSolution);
        List<Interference> interferences = plagiarismDetectorService.getInterferences(firstSolution.getSolutionId());
        for (Interference interf : interferences) {
            assertNotEquals(interf.getInterferedSolutionId().longValue(), secondSolution.getSolutionId());
        }

        List<Interference> interferences2 = plagiarismDetectorService.getInterferences(secondSolution.getSolutionId());
        boolean metFirst = false;
        for (Interference interf : interferences2) {
            assertEquals(1.0, interf.getInterferenceFraction(), 1e-6);
            if (interf.getInterferedSolutionId() == firstSolution.getSolutionId()) {
                metFirst = true;
                break;
            }
        }
        assertTrue(metFirst);

    }

    Solution loadAndCheck(Solution solution) {
        long id = plagiarismDetectorService.putSolution(solution).getSolutionId();
        Solution result = plagiarismDetectorService.getSolution(id);
        assertSame(solution, result);
        return result;
    }

    void assertSame(Solution first, Solution second) {
        assertEquals(first.getLanguageId(), second.getLanguageId());
        assertEquals(first.getCode(), second.getCode());
        assertEquals(first.getDescription(), second.getDescription());
        assertEquals(first.getName(), second.getName());
    }


    Path getPath(String name) {
        return Path.of("src/test/resources/" + name);
    }

    Solution copy(Solution solution) {
        Solution ans = new Solution();
        ans.setLanguageId(solution.getLanguageId());
        ans.setCode(solution.getCode());
        ans.setDescription(solution.getDescription());
        ans.setName(solution.getName());
        return ans;
    }
}