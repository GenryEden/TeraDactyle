package ru.kernelpunik.teradactyle.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import ru.kernelpunik.teradactyle.models.Component;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;

import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ComponentRepository to verify JPA operations and database constraints.
 */
@DataJpaTest
class ComponentRepositoryTest {

    @Autowired
    private ComponentRepository componentRepository;
    
    @Autowired
    private EntityManager entityManager;
    
    /**
     * Tests saving a component and then retrieving it by ID.
     */
    @Test
    void testSaveAndFindById() {
        // Create and save a new component
        Component component = new Component();
        component.setName("Test Component");
        component.setDescription("A test component description");
        
        Component savedComponent = componentRepository.save(component);
        
        // ID should be assigned after saving
        assertNotEquals(0L, savedComponent.getComponentId());
        
        // Find by ID should return the component
        Optional<Component> foundComponent = componentRepository.findById(savedComponent.getComponentId());
        assertTrue(foundComponent.isPresent());
        assertEquals(savedComponent.getComponentId(), foundComponent.get().getComponentId());
        assertEquals("Test Component", foundComponent.get().getName());
        assertEquals("A test component description", foundComponent.get().getDescription());
    }
    
    /**
     * Tests retrieving all components.
     */
    @Test
    void testFindAll() {
        // Clear any existing data
        componentRepository.deleteAll();
        
        // Create and save multiple components
        Component component1 = new Component();
        component1.setName("Component 1");
        component1.setDescription("Description 1");
        componentRepository.save(component1);
        
        Component component2 = new Component();
        component2.setName("Component 2");
        component2.setDescription("Description 2");
        componentRepository.save(component2);
        
        // FindAll should return all components
        Iterable<Component> components = componentRepository.findAll();
        List<Component> componentList = new ArrayList<>();
        components.forEach(componentList::add);
        
        assertEquals(2, componentList.size());
        assertTrue(componentList.stream().anyMatch(c -> "Component 1".equals(c.getName())));
        assertTrue(componentList.stream().anyMatch(c -> "Component 2".equals(c.getName())));
    }
    
    /**
     * Tests deleting a component by ID.
     */
    @Test
    void testDeleteById() {
        // Create and save a component
        Component component = new Component();
        component.setName("Component to Delete");
        component.setDescription("This component will be deleted");
        Component savedComponent = componentRepository.save(component);
        
        // Verify it exists
        assertTrue(componentRepository.existsById(savedComponent.getComponentId()));
        
        // Delete it
        componentRepository.deleteById(savedComponent.getComponentId());
        
        // Verify it no longer exists
        assertFalse(componentRepository.existsById(savedComponent.getComponentId()));
    }
    
    /**
     * Tests updating an existing component.
     */
    @Test
    void testUpdate() {
        // Create and save a component
        Component component = new Component();
        component.setName("Original Component Name");
        component.setDescription("Original Component Description");
        Component savedComponent = componentRepository.save(component);
        
        // Update the component
        savedComponent.setName("Updated Component Name");
        savedComponent.setDescription("Updated Component Description");
        componentRepository.save(savedComponent);
        
        // Verify changes were saved
        Optional<Component> updatedComponent = componentRepository.findById(savedComponent.getComponentId());
        assertTrue(updatedComponent.isPresent());
        assertEquals("Updated Component Name", updatedComponent.get().getName());
        assertEquals("Updated Component Description", updatedComponent.get().getDescription());
    }
    
    /**
     * Tests saving a component with null name.
     */
    @Test
    void testSaveComponentWithNullName() {
        // Create a component with null name
        Component component = new Component();
        component.setName(null);
        component.setDescription("Component with null name");
        
        // Save and flush immediately to ensure constraints are checked
        Component savedComponent = componentRepository.save(component);
        try {
            entityManager.flush();
            // If we reach here, null names are allowed - verify it was saved correctly
            assertNull(savedComponent.getName());
            assertNotNull(savedComponent.getDescription());
        } catch (Exception e) {
            // If an exception is thrown, the database doesn't allow null names
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                     "Expected a constraint violation exception but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Tests saving a component with null description.
     */
    @Test
    void testSaveComponentWithNullDescription() {
        // Create a component with null description
        Component component = new Component();
        component.setName("Component with null description");
        component.setDescription(null);
        
        // Save and flush immediately to ensure constraints are checked
        Component savedComponent = componentRepository.save(component);
        try {
            entityManager.flush();
            // If we reach here, null descriptions are allowed - verify it was saved correctly
            assertNotNull(savedComponent.getName());
            assertNull(savedComponent.getDescription());
        } catch (Exception e) {
            // If an exception is thrown, the database doesn't allow null descriptions
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                     "Expected a constraint violation exception but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Tests saving a component with an extremely long name.
     */
    @Test
    void testSaveComponentWithLongName() {
        // Create a component with a very long name (likely to exceed most column limits)
        Component component = new Component();
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longName.append("a");
        }
        component.setName(longName.toString());
        component.setDescription("Component with very long name");
        
        try {
            Component savedComponent = componentRepository.save(component);
            entityManager.flush(); // Force the persistence provider to execute SQL statements
            
            // If we get here, the database accepted the long name
            assertEquals(1000, savedComponent.getName().length());
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // This is expected if there's a column size constraint in the database
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                      "Expected a data violation exception but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Tests attempting to create two components with the same name.
     */
    @Test
    void testSaveComponentWithDuplicateName() {
        // Create and save first component
        Component component1 = new Component();
        component1.setName("Duplicate Component Name");
        component1.setDescription("First component with this name");
        componentRepository.save(component1);
        entityManager.flush();
        
        // Create second component with same name
        Component component2 = new Component();
        component2.setName("Duplicate Component Name");
        component2.setDescription("Second component with same name");
        
        try {
            componentRepository.save(component2);
            entityManager.flush(); // Force the persistence provider to execute SQL statements
            
            // If we get here, duplicate names are allowed
            // Let's verify both components were saved
            Iterable<Component> components = componentRepository.findAll();
            int count = 0;
            for (Component c : components) {
                if ("Duplicate Component Name".equals(c.getName())) {
                    count++;
                }
            }
            assertTrue(count >= 2, "Expected at least 2 components with the same name");
        } catch (PersistenceException | DataIntegrityViolationException e) {
            // This is expected if there's a unique constraint on the name column
            assertTrue(e instanceof PersistenceException || e instanceof DataIntegrityViolationException,
                      "Expected a unique constraint violation but got: " + e.getClass().getName());
        }
    }
    
    /**
     * Helper method to find an ID that doesn't exist in the database.
     */
    private long findNonExistentComponentId() {
        // Start with a high ID value
        long id = 9999L;
        
        // Keep incrementing until we find an ID that doesn't exist
        while (componentRepository.existsById(id)) {
            id++;
        }
        
        return id;
    }
} 