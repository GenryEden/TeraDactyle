package ru.kernelpunik.teradactyle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.repositories.ProjectRepository;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.ProjectProcessor;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Primary
@Service
public class ProjectService implements IProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectProcessor projectProcessor;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, FingerprintRepository fingerprintRepository) {
        this(projectRepository, new ProjectProcessor(fingerprintRepository));
    }

    public ProjectService(ProjectRepository projectRepository, ProjectProcessor projectProcessor) {
        this.projectRepository = projectRepository;
        this.projectProcessor = projectProcessor;
    }

    @Override
    public Project getProject(long projectId) {
        return projectRepository.findById(projectId).orElse(null);
    }

    @Override
    public List<Project> getAllProjects() {
        List<Project> projects = new ArrayList<>();
        projectRepository.findAll().forEach(projects::add);
        return projects;
    }

    @Override
    public Project addProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public CompletableFuture<TreeNode<CollisionReport>> analyzeProject(File projectDir) {
        // Check for null directory
        if (projectDir == null) {
            throw new NullPointerException("Project directory cannot be null");
        }
        
        // Check if directory exists
        if (!projectDir.exists()) {
            throw new IllegalArgumentException("Invalid project directory: " + projectDir.getPath());
        }
        
        // Use the injected projectProcessor instead of creating a new one
        return projectProcessor.processTree(projectDir);
    }

    /**
     * Delete a project by ID
     * @param projectId The ID of the project to delete
     * @return true if the project was found and deleted, false if the project was not found
     */
    @Override
    public boolean deleteProject(long projectId) {
        // Check if the project exists
        if (projectRepository.findById(projectId).isPresent()) {
            // Delete the project - JPA will handle cascading deletes if configured correctly
            projectRepository.deleteById(projectId);
            return true;
        }
        return false;
    }
} 