package ru.kernelpunik.teradactyle.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.teradactyle.services.IComponentStorageService;
import ru.kernelpunik.teradactyle.services.IProjectService;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProjectController.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IProjectService projectService;

    @MockBean
    private IComponentStorageService componentStorageService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private Project testProject;
    private List<Project> projectList;
    
    @BeforeEach
    void setUp() {
        testProject = new Project(1L, "Test Project", "Test Description");
        projectList = Arrays.asList(
                testProject,
                new Project(2L, "Project 2", "Description 2")
        );
    }

    @Test
    void testAddProject() throws Exception {
        Project projectToAdd = new Project(0L, "New Project", "Project Description");
        Project savedProject = new Project(1L, "New Project", "Project Description");
        
        when(projectService.addProject(Mockito.any(Project.class))).thenReturn(savedProject);

        mockMvc.perform(post("/v0/api/project/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectToAdd)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.description").value("Project Description"));
        
        verify(projectService).addProject(Mockito.any(Project.class));
    }
    
    @Test
    void testAddProjectWithNonZeroId() throws Exception {
        Project invalidProject = new Project(5L, "Invalid Project", "Has ID already set");

        mockMvc.perform(post("/v0/api/project/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());
        
        verify(projectService, never()).addProject(Mockito.any(Project.class));
    }
    
    @Test
    void testAddProjectWithoutName() throws Exception {
        Project invalidProject = new Project(0L, null, "Missing name");

        mockMvc.perform(post("/v0/api/project/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidProject)))
                .andExpect(status().isBadRequest());
        
        verify(projectService, never()).addProject(Mockito.any(Project.class));
    }
    
    @Test
    void testGetProject() throws Exception {
        when(projectService.getProject(1L)).thenReturn(testProject);

        mockMvc.perform(get("/v0/api/project/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.name").value("Test Project"))
                .andExpect(jsonPath("$.description").value("Test Description"));
        
        verify(projectService).getProject(1L);
    }
    
    @Test
    void testGetProjectNotFound() throws Exception {
        when(projectService.getProject(999L)).thenReturn(null);

        mockMvc.perform(get("/v0/api/project/999"))
                .andExpect(status().isNotFound());
        
        verify(projectService).getProject(999L);
    }
    
    @Test
    void testGetAllProjects() throws Exception {
        when(projectService.getAllProjects()).thenReturn(projectList);

        mockMvc.perform(get("/v0/api/project/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].projectId").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Project"))
                .andExpect(jsonPath("$[1].projectId").value(2))
                .andExpect(jsonPath("$[1].name").value("Project 2"));
        
        verify(projectService).getAllProjects();
    }
    
    @Test
    void testAnalyzeProject() throws Exception {
        File tempFile = File.createTempFile("project", "dir");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.zip", "application/zip", "test content".getBytes());
        
        CollisionReport report = new CollisionReport("test-report");
        TreeNode<CollisionReport> result = new TreeNode<>(report, null);
        CompletableFuture<TreeNode<CollisionReport>> future = CompletableFuture.completedFuture(result);
        
        when(componentStorageService.storeZip(any())).thenReturn(tempFile);
        when(projectService.analyzeProject(tempFile)).thenReturn(future);

        mockMvc.perform(multipart("/v0/api/project/analyze")
                .file(multipartFile))
                .andExpect(status().isOk());
        
        verify(componentStorageService).storeZip(any());
        verify(projectService).analyzeProject(tempFile);
    }
    
    @Test
    void testAnalyzeProjectWithStorageError() throws Exception {
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.zip", "application/zip", "test content".getBytes());
        
        when(componentStorageService.storeZip(any())).thenThrow(new java.io.IOException("Storage error"));

        mockMvc.perform(multipart("/v0/api/project/analyze")
                .file(multipartFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Unable to upload and extract project files")));
        
        verify(componentStorageService).storeZip(any());
        verify(projectService, never()).analyzeProject(any());
    }
    
    @Test
    void testAnalyzeProjectWithProcessingError() throws Exception {
        File tempFile = File.createTempFile("project", "dir");
        MockMultipartFile multipartFile = new MockMultipartFile(
                "file", "test.zip", "application/zip", "test content".getBytes());
        
        CompletableFuture<TreeNode<CollisionReport>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Processing error"));
        
        when(componentStorageService.storeZip(any())).thenReturn(tempFile);
        when(projectService.analyzeProject(tempFile)).thenReturn(future);

        mockMvc.perform(multipart("/v0/api/project/analyze")
                .file(multipartFile))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Error during project analysis")));
        
        verify(componentStorageService).storeZip(any());
        verify(projectService).analyzeProject(tempFile);
    }
} 