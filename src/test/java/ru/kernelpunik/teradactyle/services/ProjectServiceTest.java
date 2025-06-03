package ru.kernelpunik.teradactyle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.repositories.ProjectRepository;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.ProjectProcessor;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private FingerprintRepository fingerprintRepository;

    @Mock
    private ProjectProcessor projectProcessor;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, projectProcessor);
    }

    @Test
    void testGetProject() {
        // Arrange
        Project expectedProject = new Project(1L, "Test Project", "Project Description");
        when(projectRepository.findById(1L)).thenReturn(Optional.of(expectedProject));

        // Act
        Project actualProject = projectService.getProject(1L);

        // Assert
        assertNotNull(actualProject);
        assertEquals(expectedProject, actualProject);
        verify(projectRepository).findById(1L);
    }

    @Test
    void testGetProjectNotFound() {
        // Arrange
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Project actualProject = projectService.getProject(999L);

        // Assert
        assertNull(actualProject);
        verify(projectRepository).findById(999L);
    }

    @Test
    void testGetAllProjects() {
        // Arrange
        List<Project> expectedProjects = Arrays.asList(
                new Project(1L, "Project 1", "Description 1"),
                new Project(2L, "Project 2", "Description 2")
        );
        when(projectRepository.findAll()).thenReturn(expectedProjects);

        // Act
        List<Project> actualProjects = projectService.getAllProjects();

        // Assert
        assertNotNull(actualProjects);
        assertEquals(2, actualProjects.size());
        assertEquals(expectedProjects, actualProjects);
        verify(projectRepository).findAll();
    }

    @Test
    void testAddProject() {
        // Arrange
        Project projectToSave = new Project(0L, "New Project", "New Description");
        Project savedProject = new Project(1L, "New Project", "New Description");
        when(projectRepository.save(projectToSave)).thenReturn(savedProject);

        // Act
        Project actualProject = projectService.addProject(projectToSave);

        // Assert
        assertNotNull(actualProject);
        assertEquals(savedProject, actualProject);
        verify(projectRepository).save(projectToSave);
    }

    @Test
    void testAnalyzeProject() {
        // Arrange
        // Create a mock File instead of using a real path
        File projectDir = mock(File.class);
        when(projectDir.exists()).thenReturn(true);
        when(projectDir.isDirectory()).thenReturn(true);
        
        TreeNode<CollisionReport> expectedResult = new TreeNode<>(new CollisionReport(), new ArrayList<>());
        CompletableFuture<TreeNode<CollisionReport>> future = CompletableFuture.completedFuture(expectedResult);
        
        when(projectProcessor.processTree(projectDir)).thenReturn(future);

        // Act
        CompletableFuture<TreeNode<CollisionReport>> actualFuture = projectService.analyzeProject(projectDir);

        // Assert
        assertNotNull(actualFuture);
        verify(projectProcessor).processTree(projectDir);
        
        // Verify the future returns the expected result
        try {
            TreeNode<CollisionReport> actualResult = actualFuture.get();
            assertEquals(expectedResult, actualResult);
        } catch (Exception e) {
            fail("Exception should not be thrown: " + e.getMessage());
        }
    }
    
    /**
     * 1. Error Handling Tests
     */
    @Test
    void testGetProjectWithRepositoryException() {
        // Arrange
        when(projectRepository.findById(1L)).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.getProject(1L);
        });
        
        assertEquals("Database connection failed", exception.getMessage());
        verify(projectRepository).findById(1L);
    }
    
    @Test
    void testAnalyzeProjectWithRepositoryException() {
        // Arrange
        File projectDir = mock(File.class);
        when(projectDir.exists()).thenReturn(true);
        when(projectDir.isDirectory()).thenReturn(true);
        
        when(projectProcessor.processTree(projectDir)).thenThrow(new RuntimeException("Processing error"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.analyzeProject(projectDir);
        });
        
        assertEquals("Processing error", exception.getMessage());
        verify(projectProcessor).processTree(projectDir);
    }
    
    /**
     * 2. Project Deletion Tests
     * 
     * Note: These tests assume there's a deleteProject method in the service.
     * You'll need to implement this method in ProjectService if it doesn't exist.
     */
    @Test
    void testDeleteProject() {
        // Arrange
        long projectId = 1L;
        Project project = new Project(projectId, "Project to Delete", "Will be deleted");
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        doNothing().when(projectRepository).deleteById(projectId);
        
        // Act
        boolean result = projectService.deleteProject(projectId);
        
        // Assert
        assertTrue(result);
        verify(projectRepository).findById(projectId);
        verify(projectRepository).deleteById(projectId);
        // Verify we're not interacting with fingerprints directly during deletion
        verifyNoInteractions(fingerprintRepository);
    }
    
    @Test
    void testDeleteProjectNotFound() {
        // Arrange
        long projectId = 999L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        
        // Act
        boolean result = projectService.deleteProject(projectId);
        
        // Assert
        assertFalse(result);
        verify(projectRepository).findById(projectId);
        verify(projectRepository, never()).deleteById(anyLong());
    }
    
    @Test
    void testDeleteProjectRepositoryException() {
        // Arrange
        long projectId = 1L;
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(new Project()));
        doThrow(new RuntimeException("Delete failed")).when(projectRepository).deleteById(projectId);
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            projectService.deleteProject(projectId);
        });
        
        assertEquals("Delete failed", exception.getMessage());
        verify(projectRepository).findById(projectId);
        verify(projectRepository).deleteById(projectId);
    }
    
    /**
     * 3. Repeated Project Analysis Tests
     */
    @Test
    void testRepeatedProjectAnalysis() throws ExecutionException, InterruptedException {
        // Arrange
        File projectDir = mock(File.class);
        when(projectDir.exists()).thenReturn(true);
        when(projectDir.isDirectory()).thenReturn(true);
        
        TreeNode<CollisionReport> expectedResult = new TreeNode<>(new CollisionReport(), new ArrayList<>());
        CompletableFuture<TreeNode<CollisionReport>> future = CompletableFuture.completedFuture(expectedResult);
        
        when(projectProcessor.processTree(projectDir)).thenReturn(future);
        
        // Act - analyze the same project twice
        CompletableFuture<TreeNode<CollisionReport>> result1 = projectService.analyzeProject(projectDir);
        CompletableFuture<TreeNode<CollisionReport>> result2 = projectService.analyzeProject(projectDir);
        
        // Assert
        assertEquals(result1.get(), result2.get());
        // Verify processTree was called exactly twice (no caching implemented)
        verify(projectProcessor, times(2)).processTree(projectDir);
    }
    
    /**
     * 4. Invalid Arguments Tests
     */
    @Test
    void testAnalyzeProjectWithNullArgument() {
        // Act & Assert
        Exception exception = assertThrows(NullPointerException.class, () -> {
            projectService.analyzeProject(null);
        });
        
        // Verify no interactions with processor since validation fails first
        verifyNoInteractions(projectProcessor);
    }
    
    @Test
    void testAnalyzeProjectWithNonExistentDirectory() {
        // Arrange
        File nonExistentDir = mock(File.class);
        when(nonExistentDir.exists()).thenReturn(false);
        when(nonExistentDir.getPath()).thenReturn("/non/existent/path");
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.analyzeProject(nonExistentDir);
        });
        
        assertTrue(exception.getMessage().contains("Invalid project directory"));
        verifyNoInteractions(projectProcessor);
    }
    
    @Test
    void testAnalyzeProjectWithFileInsteadOfDirectory() {
        // Arrange
        File file = mock(File.class);
        when(file.exists()).thenReturn(true);
        when(file.isDirectory()).thenReturn(false);
        when(file.getPath()).thenReturn("/path/to/file.txt");
        
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            projectService.analyzeProject(file);
        });
        
        assertTrue(exception.getMessage().contains("Invalid project directory"));
        verifyNoInteractions(projectProcessor);
    }
    
    /**
     * 5. Component Interaction Tests
     */
    @Test
    void testAddProjectInteractions() {
        // Arrange
        Project projectToSave = new Project(0L, "New Project", "New Description");
        when(projectRepository.save(any(Project.class))).thenReturn(projectToSave);
        
        // Act
        projectService.addProject(projectToSave);
        
        // Assert
        // Verify save is called exactly once
        verify(projectRepository, times(1)).save(projectToSave);
        // Verify no interactions with other components
        verifyNoInteractions(projectProcessor);
        verifyNoInteractions(fingerprintRepository);
    }
    
    @Test
    void testAnalyzeProjectUsesInjectedProcessor() {
        // Arrange
        File projectDir = mock(File.class);
        when(projectDir.exists()).thenReturn(true);
        when(projectDir.isDirectory()).thenReturn(true);
        
        CompletableFuture<TreeNode<CollisionReport>> future = 
            CompletableFuture.completedFuture(new TreeNode<>(new CollisionReport(), new ArrayList<>()));
        
        when(projectProcessor.processTree(projectDir)).thenReturn(future);
        
        // Act
        projectService.analyzeProject(projectDir);
        
        // Assert
        // Verify that processTree is called on the injected processor
        verify(projectProcessor).processTree(projectDir);
    }
} 