package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface IProjectService {
    /**
     * Get a project by ID
     */
    Project getProject(long projectId);
    
    /**
     * Get all projects
     */
    List<Project> getAllProjects();
    
    /**
     * Add a new project
     */
    Project addProject(Project project);
    
    /**
     * Analyze a project directory for plagiarism
     */
    CompletableFuture<TreeNode<CollisionReport>> analyzeProject(File projectDir);
    
    /**
     * Delete a project by ID
     * @param projectId The ID of the project to delete
     * @return true if the project was found and deleted, false if the project was not found
     */
    boolean deleteProject(long projectId);
} 