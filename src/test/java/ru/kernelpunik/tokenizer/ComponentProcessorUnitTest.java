package ru.kernelpunik.tokenizer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ComponentProcessor
 * Uses MockitoExtension instead of SpringBootTest for true unit testing,
 * with mocked dependencies and without loading the Spring context
 */
@ExtendWith(MockitoExtension.class)
public class ComponentProcessorUnitTest {
    
    @Mock
    private FingerprintRepository fingerprintRepository;
    
    @TempDir
    Path tempDir;
    
    private ComponentProcessor componentProcessor;
    private static final Logger logger = Logger.getLogger(ComponentProcessorUnitTest.class.getName());
    private static final long COMPONENT_ID = 1L;
    
    @BeforeEach
    void setUp() {
        componentProcessor = new ComponentProcessor(fingerprintRepository);
        
        // Mock save method to avoid DB operations
        when(fingerprintRepository.save(any(Fingerprint.class))).thenReturn(null);
    }
    
    /**
     * Test 1: Processing projects with different directory structures.
     * Creates a project with multiple directories containing different file types
     * and verifies that the processor correctly traverses and processes the structure.
     */
    @Test
    void testProcessingDifferentDirectoryStructures() throws IOException, ExecutionException, InterruptedException {
        // Create test directory structure
        Path javaDir = Files.createDirectory(tempDir.resolve("java"));
        Path pythonDir = Files.createDirectory(tempDir.resolve("python"));
        Path cppDir = Files.createDirectory(tempDir.resolve("cpp"));
        Path miscDir = Files.createDirectory(tempDir.resolve("misc"));
        
        // Create test files
        createJavaFile(javaDir.resolve("Main.java"));
        createJavaFile(javaDir.resolve("Helper.java"));
        createPythonFile(pythonDir.resolve("script.py"));
        createCppFile(cppDir.resolve("program.cpp"));
        createCppFile(cppDir.resolve("utils.cpp"));
        
        // Create non-relevant files
        Files.writeString(miscDir.resolve("README.md"), "# Test Project");
        Files.writeString(miscDir.resolve("config.json"), "{ \"name\": \"test\" }");
        
        // Process the directory
        CompletableFuture<Void> future = componentProcessor.processComponent(COMPONENT_ID, tempDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Verify interactions
        verify(fingerprintRepository, atLeastOnce()).save(any(Fingerprint.class));
        
        // Different file types should be processed differently
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getComponentId() == COMPONENT_ID && fingerprint.getLanguageId() == Language.JAVA.id));
        
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getComponentId() == COMPONENT_ID && fingerprint.getLanguageId() == Language.PYTHON.id));
        
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getComponentId() == COMPONENT_ID && fingerprint.getLanguageId() == Language.CPP.id));
        
        logger.info("Successfully processed different directory structures");
    }
    
    /**
     * Test 2: Ignoring files by extensions.
     * Verifies that the processor correctly ignores files with non-target extensions.
     */
    @Test
    void testIgnoringFilesByExtensions() throws IOException, ExecutionException, InterruptedException {
        // Create test directory with mixed file types
        Path mixedDir = Files.createDirectory(tempDir.resolve("mixed"));
        
        // Create supported files
        createJavaFile(mixedDir.resolve("Valid.java"));
        
        // Create unsupported files with various extensions
        Files.writeString(mixedDir.resolve("document.txt"), "This is a test document");
        Files.writeString(mixedDir.resolve("config.xml"), "<config></config>");
        Files.writeString(mixedDir.resolve("page.html"), "<html></html>");
        Files.writeString(mixedDir.resolve("script.js"), "console.log('test');");
        Files.writeString(mixedDir.resolve("style.css"), "body { color: black; }");
        
        // Process the directory
        CompletableFuture<Void> future = componentProcessor.processComponent(COMPONENT_ID, mixedDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Verify interactions
        verify(fingerprintRepository, atLeastOnce()).save(any(Fingerprint.class));
        
        // Only Java files should be processed
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getComponentId() == COMPONENT_ID && fingerprint.getLanguageId() == Language.JAVA.id));
        
        // No fingerprints from non-supported files should be saved
        verify(fingerprintRepository, never()).save(argThat(fingerprint -> 
                fingerprint.getLanguageId() != Language.JAVA.id && 
                fingerprint.getLanguageId() != Language.PYTHON.id && 
                fingerprint.getLanguageId() != Language.CPP.id));
        
        logger.info("Successfully ignored files with non-target extensions");
    }
    
    /**
     * Test 3: Processing nested directories.
     * Verifies that the processor correctly traverses and processes nested directory structures.
     */
    @Test
    void testProcessingNestedDirectories() throws IOException, ExecutionException, InterruptedException {
        // Create nested directory structure using createDirectories to ensure all parent directories are created
        Path srcDir = Files.createDirectory(tempDir.resolve("src"));
        Path mainDir = Files.createDirectory(srcDir.resolve("main"));
        Path testDir = Files.createDirectory(srcDir.resolve("test"));
        
        Path javaMainDir = Files.createDirectory(mainDir.resolve("java"));
        Path javaTestDir = Files.createDirectory(testDir.resolve("java"));
        
        // Use createDirectories for deeply nested paths
        Path packageDir = Files.createDirectories(javaMainDir.resolve("com").resolve("example").resolve("app"));
        Path testPackageDir = Files.createDirectories(javaTestDir.resolve("com").resolve("example").resolve("app"));
        
        // Create files in nested directories
        createJavaFile(packageDir.resolve("Main.java"));
        createJavaFile(packageDir.resolve("Service.java"));
        createJavaFile(packageDir.resolve("Repository.java"));
        
        createJavaFile(testPackageDir.resolve("MainTest.java"));
        createJavaFile(testPackageDir.resolve("ServiceTest.java"));
        
        // Process the directory
        CompletableFuture<Void> future = componentProcessor.processComponent(COMPONENT_ID, tempDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Verify interactions
        verify(fingerprintRepository, atLeast(5)).save(any(Fingerprint.class));
        
        // Files from all nested directories should be processed
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getComponentId() == COMPONENT_ID && fingerprint.getLanguageId() == Language.JAVA.id));
        
        logger.info("Successfully processed nested directories");
    }
    
    /**
     * Test 4: Handling empty folders.
     * Verifies that the processor handles empty folders gracefully without errors.
     */
    @Test
    void testHandlingEmptyFolders() throws IOException, ExecutionException, InterruptedException {
        // Create empty directories
        Files.createDirectory(tempDir.resolve("empty1"));
        Files.createDirectory(tempDir.resolve("empty2"));
        
        // Create parent directory first, then the nested empty directory
        Path parentDir = Files.createDirectory(tempDir.resolve("parent"));
        Path nestedEmpty = Files.createDirectory(parentDir.resolve("empty"));
        
        // Process the directory
        CompletableFuture<Void> future = componentProcessor.processComponent(COMPONENT_ID, tempDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Verify no errors occurred and no fingerprints were saved
        verify(fingerprintRepository, never()).save(any(Fingerprint.class));
        
        // Add a file and verify it's processed
        createJavaFile(nestedEmpty.resolve("NewFile.java"));
        
        // Process again
        future = componentProcessor.processComponent(COMPONENT_ID, tempDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Now there should be fingerprints
        verify(fingerprintRepository, atLeastOnce()).save(any(Fingerprint.class));
        
        logger.info("Successfully handled empty folders");
    }
    
    /**
     * Test 5: Combined test with mixed content and depth.
     * Tests the processor with a complex directory structure containing 
     * mixed file types at different depths.
     */
    @Test
    void testCombinedMixedContentAndDepth() throws IOException, ExecutionException, InterruptedException {
        // Create complex directory structure
        Path srcDir = Files.createDirectory(tempDir.resolve("src"));
        Path docsDir = Files.createDirectory(tempDir.resolve("docs"));
        Path scriptsDir = Files.createDirectory(tempDir.resolve("scripts"));
        
        // Create nested directories with mixed content
        Path javaDir = Files.createDirectory(srcDir.resolve("java"));
        Path cppDir = Files.createDirectory(srcDir.resolve("cpp"));
        Path pythonDir = Files.createDirectory(scriptsDir.resolve("python"));
        
        // Create files of different types
        createJavaFile(javaDir.resolve("Main.java"));
        createJavaFile(javaDir.resolve("Utils.java"));
        createCppFile(cppDir.resolve("module.cpp"));
        createCppFile(cppDir.resolve("helper.cpp"));
        createPythonFile(pythonDir.resolve("script.py"));
        
        // Create non-processable files
        Files.writeString(docsDir.resolve("README.md"), "# Documentation");
        Files.writeString(docsDir.resolve("config.xml"), "<config></config>");
        Files.writeString(tempDir.resolve(".gitignore"), "*.class\n*.log");
        
        // Process the directory
        CompletableFuture<Void> future = componentProcessor.processComponent(COMPONENT_ID, tempDir.toFile());
        future.get(); // Wait for processing to complete
        
        // Verify interactions
        verify(fingerprintRepository, atLeastOnce()).save(any(Fingerprint.class));
        
        // Verify all supported languages were processed
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getLanguageId() == Language.JAVA.id));
        
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getLanguageId() == Language.CPP.id));
        
        verify(fingerprintRepository, atLeast(1)).save(argThat(fingerprint -> 
                fingerprint.getLanguageId() == Language.PYTHON.id));
        
        logger.info("Successfully processed mixed content at various depths");
    }
    
    // Helper methods to create test files
    
    private void createJavaFile(Path filePath) throws IOException {
        String javaContent = 
            "public class " + getClassNameFromPath(filePath) + " {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "    \n" +
            "    public void testMethod() {\n" +
            "        int a = 10;\n" +
            "        int b = 20;\n" +
            "        int sum = a + b;\n" +
            "    }\n" +
            "}\n";
        Files.writeString(filePath, javaContent);
    }
    
    private void createPythonFile(Path filePath) throws IOException {
        String pythonContent =
            "def main():\n" +
            "    print('Hello, World!')\n" +
            "    \n" +
            "def calculate_sum(a, b):\n" +
            "    return a + b\n" +
            "\n" +
            "if __name__ == '__main__':\n" +
            "    main()\n" +
            "    result = calculate_sum(10, 20)\n" +
            "    print(f'Sum: {result}')\n";
        Files.writeString(filePath, pythonContent);
    }
    
    private void createCppFile(Path filePath) throws IOException {
        String cppContent =
            "#include <iostream>\n" +
            "#include <string>\n" +
            "\n" +
            "int calculateSum(int a, int b) {\n" +
            "    return a + b;\n" +
            "}\n" +
            "\n" +
            "int main() {\n" +
            "    std::cout << \"Hello, World!\" << std::endl;\n" +
            "    \n" +
            "    int result = calculateSum(10, 20);\n" +
            "    std::cout << \"Sum: \" << result << std::endl;\n" +
            "    \n" +
            "    return 0;\n" +
            "}\n";
        Files.writeString(filePath, cppContent);
    }
    
    private String getClassNameFromPath(Path filePath) {
        String fileName = filePath.getFileName().toString();
        if (fileName.endsWith(".java")) {
            return fileName.substring(0, fileName.length() - 5);
        }
        return "DefaultClass";
    }
} 