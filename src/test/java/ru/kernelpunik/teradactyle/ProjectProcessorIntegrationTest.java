package ru.kernelpunik.teradactyle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.services.ProjectService;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProjectProcessorIntegrationTest {

    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private FingerprintRepository fingerprintRepository;
    
    @TempDir
    Path tempDir;
    
    @Test
    public void testProjectProcessorAnalysis() throws IOException, ExecutionException, InterruptedException {
        // Create a test project structure with sample code files
        createSampleProjectStructure(tempDir);
        
        // Process the project using the ProjectService and ProjectProcessor
        CompletableFuture<TreeNode<CollisionReport>> future = projectService.analyzeProject(tempDir.toFile());
        
        // Wait for the analysis to complete
        TreeNode<CollisionReport> result = future.get();
        
        // Verify the analysis results
        assertNotNull(result);
        assertNotNull(result.getValue());

        // The sample project is new, so it should not have collisions
        assertTrue(result.getValue().getCollisions().isEmpty());
    }
    
    private void createSampleProjectStructure(Path projectDir) throws IOException {
        // Create src directory
        Path srcDir = Files.createDirectory(projectDir.resolve("src"));
        Path mainDir = Files.createDirectory(srcDir.resolve("main"));
        Path javaDir = Files.createDirectory(mainDir.resolve("java"));
        
        // Create a sample Java file
        Path sampleJavaFile = javaDir.resolve("Sample.java");
        String javaContent = 
            "public class Sample {\n" +
            "    public static void main(String[] args) {\n" +
            "        System.out.println(\"Hello, World!\");\n" +
            "    }\n" +
            "}\n";
        Files.writeString(sampleJavaFile, javaContent);
        
        // Create a sample Python file
        Path pythonDir = Files.createDirectory(projectDir.resolve("python"));
        Path samplePythonFile = pythonDir.resolve("sample.py");
        String pythonContent =
            "def main():\n" +
            "    print('Hello, World!')\n" +
            "\n" +
            "if __name__ == '__main__':\n" +
            "    main()\n";
        Files.writeString(samplePythonFile, pythonContent);
        
        // Create a sample C++ file
        Path cppDir = Files.createDirectory(projectDir.resolve("cpp"));
        Path sampleCppFile = cppDir.resolve("sample.cpp");
        String cppContent =
            "#include <iostream>\n" +
            "\n" +
            "int main() {\n" +
            "    std::cout << \"Hello, World!\" << std::endl;\n" +
            "    return 0;\n" +
            "}\n";
        Files.writeString(sampleCppFile, cppContent);
    }
} 