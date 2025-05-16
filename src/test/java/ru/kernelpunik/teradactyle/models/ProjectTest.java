package ru.kernelpunik.teradactyle.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    @Test
    void testProjectCreation() {
        Project project = new Project(1L, "Test Project", "Project Description");
        
        assertEquals(1L, project.getProjectId());
        assertEquals("Test Project", project.getName());
        assertEquals("Project Description", project.getDescription());
    }
    
    @Test
    void testEquality() {
        Project project1 = new Project(1L, "Project 1", "Description 1");
        Project project2 = new Project(1L, "Different Name", "Different Description");
        Project project3 = new Project(2L, "Project 1", "Description 1");
        
        // Two projects with same ID should be equal
        assertEquals(project1, project2);
        
        // Projects with different IDs should not be equal
        assertNotEquals(project1, project3);
        
        // A project should be equal to itself
        assertEquals(project1, project1);
        
        // A project should not be equal to null or other objects
        assertNotEquals(project1, null);
        assertNotEquals(project1, "String");
    }
    
    @Test
    void testHashCode() {
        Project project1 = new Project(1L, "Project 1", "Description 1");
        Project project2 = new Project(1L, "Different Name", "Different Description");
        
        // Projects with same ID should have same hashCode
        assertEquals(project1.hashCode(), project2.hashCode());
    }
    
    @Test
    void testSetters() {
        Project project = new Project();
        
        project.setProjectId(42L);
        project.setName("Updated Name");
        project.setDescription("Updated Description");
        
        assertEquals(42L, project.getProjectId());
        assertEquals("Updated Name", project.getName());
        assertEquals("Updated Description", project.getDescription());
    }
} 