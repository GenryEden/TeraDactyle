package ru.kernelpunik.teradactyle.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.ComponentRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ComponentService class.
 * Tests the functionality of retrieving, adding, and managing components.
 */
@ExtendWith(MockitoExtension.class)
class ComponentServiceTest {
    
    @Mock
    private ComponentRepository componentRepository;
    
    private ComponentService componentService;
    
    @BeforeEach
    void setUp() {
        componentService = new ComponentService(componentRepository);
    }
    
    /**
     * Test retrieving a component by ID when it exists in the repository.
     */
    @Test
    void testGetComponent() {
        // Arrange
        Component expectedComponent = new Component(1L, "Test Component", "Test Description");
        when(componentRepository.findById(1L)).thenReturn(Optional.of(expectedComponent));
        
        // Act
        Component actualComponent = componentService.getComponent(1L);
        
        // Assert
        assertNotNull(actualComponent);
        assertEquals(expectedComponent, actualComponent);
        assertEquals("Test Component", actualComponent.getName());
        assertEquals("Test Description", actualComponent.getDescription());
        verify(componentRepository).findById(1L);
    }
    
    /**
     * Test retrieving a component by ID when it doesn't exist in the repository.
     */
    @Test
    void testGetComponentNotFound() {
        // Arrange
        when(componentRepository.findById(999L)).thenReturn(Optional.empty());
        
        // Act
        Component actualComponent = componentService.getComponent(999L);
        
        // Assert
        assertNull(actualComponent);
        verify(componentRepository).findById(999L);
    }
    
    /**
     * Test retrieving all components from the repository.
     */
    @Test
    void testGetAllComponents() {
        // Arrange
        List<Component> expectedComponents = Arrays.asList(
                new Component(1L, "Component 1", "Description 1"),
                new Component(2L, "Component 2", "Description 2")
        );
        when(componentRepository.findAll()).thenReturn(expectedComponents);
        
        // Act
        Iterable<Component> actualComponents = componentService.getAllComponents();
        
        // Assert
        assertNotNull(actualComponents);
        // Convert Iterable to List for easier assertion
        List<Component> actualComponentsList = iterableToList(actualComponents);
        assertEquals(2, actualComponentsList.size());
        assertEquals(expectedComponents, actualComponentsList);
        verify(componentRepository).findAll();
    }
    
    /**
     * Test retrieving all components when the repository is empty.
     */
    @Test
    void testGetAllComponentsEmpty() {
        // Arrange
        when(componentRepository.findAll()).thenReturn(List.of());
        
        // Act
        Iterable<Component> actualComponents = componentService.getAllComponents();
        
        // Assert
        assertNotNull(actualComponents);
        assertFalse(actualComponents.iterator().hasNext());
        verify(componentRepository).findAll();
    }
    
    /**
     * Test adding a new component to the repository.
     */
    @Test
    void testAddComponent() {
        // Arrange
        Component componentToSave = new Component(0L, "New Component", "New Description");
        Component savedComponent = new Component(1L, "New Component", "New Description");
        when(componentRepository.save(componentToSave)).thenReturn(savedComponent);
        
        // Act
        Component actualComponent = componentService.addComponent(componentToSave);
        
        // Assert
        assertNotNull(actualComponent);
        assertEquals(savedComponent, actualComponent);
        assertEquals(1L, actualComponent.getComponentId());
        assertEquals("New Component", actualComponent.getName());
        assertEquals("New Description", actualComponent.getDescription());
        verify(componentRepository).save(componentToSave);
    }
    
    /**
     * Test that adding a component with null values doesn't cause exceptions
     * and delegates to the repository.
     */
    @Test
    void testAddComponentWithNullValues() {
        // Arrange
        Component componentWithNulls = new Component(0L, null, null);
        Component savedComponent = new Component(1L, null, null);
        when(componentRepository.save(componentWithNulls)).thenReturn(savedComponent);
        
        // Act
        Component actualComponent = componentService.addComponent(componentWithNulls);
        
        // Assert
        assertNotNull(actualComponent);
        assertEquals(savedComponent, actualComponent);
        assertEquals(1L, actualComponent.getComponentId());
        assertNull(actualComponent.getName());
        assertNull(actualComponent.getDescription());
        verify(componentRepository).save(componentWithNulls);
    }
    
    /**
     * Test error handling when the repository throws an exception during findById.
     */
    @Test
    void testGetComponentWithRepositoryException() {
        // Arrange
        when(componentRepository.findById(1L)).thenThrow(new RuntimeException("Database connection failed"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            componentService.getComponent(1L);
        });
        
        assertEquals("Database connection failed", exception.getMessage());
        verify(componentRepository).findById(1L);
    }
    
    /**
     * Test error handling when the repository throws an exception during findAll.
     */
    @Test
    void testGetAllComponentsWithRepositoryException() {
        // Arrange
        when(componentRepository.findAll()).thenThrow(new RuntimeException("Database error"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            componentService.getAllComponents();
        });
        
        assertEquals("Database error", exception.getMessage());
        verify(componentRepository).findAll();
    }
    
    /**
     * Test error handling when the repository throws an exception during save.
     */
    @Test
    void testAddComponentWithRepositoryException() {
        // Arrange
        Component component = new Component(0L, "Test", "Description");
        when(componentRepository.save(component)).thenThrow(new RuntimeException("Save failed"));
        
        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            componentService.addComponent(component);
        });
        
        assertEquals("Save failed", exception.getMessage());
        verify(componentRepository).save(component);
    }
    
    /**
     * Helper method to convert Iterable to List for easier assertions.
     */
    private <T> List<T> iterableToList(Iterable<T> iterable) {
        List<T> result = new java.util.ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }
} 