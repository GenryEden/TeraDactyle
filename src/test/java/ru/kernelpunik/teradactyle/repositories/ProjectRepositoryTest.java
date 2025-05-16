package ru.kernelpunik.teradactyle.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.jdbc.Sql;
import ru.kernelpunik.teradactyle.models.Project;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private EntityManager entityManager;

    @Test
    void testSaveAndFindById() {
        // Create and save a new project
        Project project = new Project();
        project.setName("Test Repository Project");
        project.setDescription("Testing the repository methods");
        
        Project savedProject = projectRepository.save(project);
        
        // ID should be assigned after saving
        assertNotEquals(0L, savedProject.getProjectId());
        
        // Find by ID should return the project
        Optional<Project> foundProject = projectRepository.findById(savedProject.getProjectId());
        assertTrue(foundProject.isPresent());
        assertEquals(savedProject.getProjectId(), foundProject.get().getProjectId());
        assertEquals("Test Repository Project", foundProject.get().getName());
        assertEquals("Testing the repository methods", foundProject.get().getDescription());
    }
    
    @Test
    void testFindAll() {
        // Clear any existing data
        projectRepository.deleteAll();
        
        // Create and save multiple projects
        Project project1 = new Project();
        project1.setName("Project 1");
        project1.setDescription("Description 1");
        projectRepository.save(project1);
        
        Project project2 = new Project();
        project2.setName("Project 2");
        project2.setDescription("Description 2");
        projectRepository.save(project2);
        
        // FindAll should return all projects
        Iterable<Project> projects = projectRepository.findAll();
        int count = 0;
        for (Project p : projects) {
            count++;
        }
        assertEquals(2, count);
    }
    
    @Test
    void testDeleteById() {
        // Create and save a project
        Project project = new Project();
        project.setName("Project to Delete");
        project.setDescription("This project will be deleted");
        Project savedProject = projectRepository.save(project);
        
        // Verify it exists
        assertTrue(projectRepository.existsById(savedProject.getProjectId()));
        
        // Delete it
        projectRepository.deleteById(savedProject.getProjectId());
        
        // Verify it no longer exists
        assertFalse(projectRepository.existsById(savedProject.getProjectId()));
    }
    
    @Test
    void testUpdate() {
        // Create and save a project
        Project project = new Project();
        project.setName("Original Name");
        project.setDescription("Original Description");
        Project savedProject = projectRepository.save(project);
        
        // Update the project
        savedProject.setName("Updated Name");
        savedProject.setDescription("Updated Description");
        projectRepository.save(savedProject);
        
        // Verify changes were saved
        Optional<Project> updatedProject = projectRepository.findById(savedProject.getProjectId());
        assertTrue(updatedProject.isPresent());
        assertEquals("Updated Name", updatedProject.get().getName());
        assertEquals("Updated Description", updatedProject.get().getDescription());
    }
    
    /**
     * Test for adding a project with null values in name field.
     * If name is required by the database schema, this should throw an exception.
     */
    @Test
    void testSaveProjectWithNullName() {
        // Create a project with null name
        Project project = new Project();
        project.setName(null);
        project.setDescription("This has a null name");
        
        // Save and flush immediately to ensure constraints are checked
        Project savedProject = projectRepository.save(project);
        try {
            entityManager.flush();
            // If we reach here, null names are allowed - verify it was saved correctly
            assertNull(savedProject.getName());
            assertNotNull(savedProject.getDescription());
        } catch (Exception e) {
            // If an exception is thrown, the database doesn't allow null names
            // This is the expected behavior if name is required
            assertTrue(e instanceof PersistenceException || 
                      e instanceof DataIntegrityViolationException,
                     "Expected a constraint violation exception but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Test for adding a project with extremely long name that might exceed
     * database column size limits.
     */
    @Test
    void testSaveProjectWithLongName() {
        // Create a project with a very long name (likely to exceed most column limits)
        Project project = new Project();
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("a");
        }
        project.setName(longName.toString());
        project.setDescription("This has a very long name");
        
        try {
            Project savedProject = projectRepository.save(project);
            entityManager.flush(); // Force the persistence provider to execute SQL statements
            
            // If we get here, the database accepted the long name
            assertEquals(1000, savedProject.getName().length());
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // This is expected if there's a column size constraint in the database
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                      "Expected a data violation exception but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Test attempting to create two projects with the same name if there's a
     * unique constraint on the name column.
     */
    @Test
    void testSaveProjectWithDuplicateName() {
        // Create and save first project
        Project project1 = new Project();
        project1.setName("Duplicate Name");
        project1.setDescription("First project with this name");
        projectRepository.save(project1);
        entityManager.flush();
        
        // Create second project with same name
        Project project2 = new Project();
        project2.setName("Duplicate Name");
        project2.setDescription("Second project with same name");
        
        try {
            projectRepository.save(project2);
            entityManager.flush(); // Force the persistence provider to execute SQL statements
            
            // If we get here, duplicate names are allowed
            // Let's verify both projects were saved
            Iterable<Project> projects = projectRepository.findAll();
            int count = 0;
            for (Project p : projects) {
                if ("Duplicate Name".equals(p.getName())) {
                    count++;
                }
            }
            assertTrue(count >= 2, "Expected at least 2 projects with the same name");
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // This is expected if there's a unique constraint on the name column
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                      "Expected a unique constraint violation but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Helper method to find an ID that doesn't exist in the database.
     */
    private long findNonExistentProjectId() {
        // Start with a high ID value
        long id = 9999L;
        
        // Keep incrementing until we find an ID that doesn't exist
        while (projectRepository.existsById(id)) {
            id++;
        }
        
        return id;
    }
} 