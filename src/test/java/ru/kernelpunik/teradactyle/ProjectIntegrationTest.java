package ru.kernelpunik.teradactyle;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.teradactyle.repositories.ProjectRepository;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testProjectLifecycle() throws Exception {
        // Clean up any existing projects with the test name
        projectRepository.findAll().forEach(project -> {
            if ("Integration Test Project".equals(project.getName())) {
                projectRepository.delete(project);
            }
        });

        // 1. Create a new project
        Project project = new Project();
        project.setName("Integration Test Project");
        project.setDescription("Created during integration testing");

        String projectJson = mockMvc.perform(post("/v0/api/project/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(project)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").exists())
                .andExpect(jsonPath("$.name").value("Integration Test Project"))
                .andReturn().getResponse().getContentAsString();

        Project savedProject = objectMapper.readValue(projectJson, Project.class);
        long projectId = savedProject.getProjectId();

        // 2. Get the project by ID
        mockMvc.perform(get("/v0/api/project/" + projectId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.name").value("Integration Test Project"))
                .andExpect(jsonPath("$.description").value("Created during integration testing"));

        // 3. Verify it appears in the list of all projects
        mockMvc.perform(get("/v0/api/project/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].projectId", hasItem((int) projectId)))
                .andExpect(jsonPath("$[*].name", hasItem("Integration Test Project")));

        // 4. Test project analysis with sample data
        MockMultipartFile testZipFile = createTestZipFile();

        mockMvc.perform(multipart("/v0/api/project/analyze")
                .file(testZipFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").exists());

        // Clean up
        projectRepository.deleteById(projectId);
    }

    private MockMultipartFile createTestZipFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            // Add a sample Java file
            ZipEntry entry = new ZipEntry("Sample.java");
            zos.putNextEntry(entry);
            String content = 
                "public class Sample {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, World!\");\n" +
                "    }\n" +
                "}\n";
            zos.write(content.getBytes());
            zos.closeEntry();
        }

        return new MockMultipartFile(
                "file",
                "test-project.zip",
                "application/zip",
                baos.toByteArray()
        );
    }
} 