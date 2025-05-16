package ru.kernelpunik.teradactyle.controllers;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kernelpunik.teradactyle.models.Project;
import ru.kernelpunik.teradactyle.services.IComponentStorageService;
import ru.kernelpunik.teradactyle.services.IProjectService;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.TreeNode;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v0/api/project")
@RequiredArgsConstructor
public class ProjectController {
    private final static Log LOG = LogFactory.getLog(ProjectController.class);
    private final IProjectService projectService;
    private final IComponentStorageService componentStorageService;

    @PostMapping("/")
    public ResponseEntity<Project> addProject(@RequestBody Project project) {
        if (project.getProjectId() != 0) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        if (project.getName() == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
        }
        return ResponseEntity.ok(projectService.addProject(project));
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<Project> getProject(@PathVariable long projectId) {
        Project project = projectService.getProject(projectId);
        if (project == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
        return ResponseEntity.ok(project);
    }

    @GetMapping("/")
    public ResponseEntity<List<Project>> getAllProjects() {
        return ResponseEntity.ok(projectService.getAllProjects());
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeProject(@RequestParam("file") MultipartFile multipartFile) {
        File projectDir;
        try {
            projectDir = componentStorageService.storeZip(multipartFile);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload and extract project files: " + e.getMessage());
        } catch (ZipException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Zip file is incorrect");
        }
        try {
            CompletableFuture<TreeNode<CollisionReport>> analysisFuture = projectService.analyzeProject(projectDir);
            TreeNode<CollisionReport> analysisResult = analysisFuture.get();
            return ResponseEntity.ok(analysisResult);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error during project analysis", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during project analysis: " + e.getMessage());
        } finally {
            // Clean up temporary files
            deleteRecursively(projectDir);
        }
    }

    @PostMapping("/analyzePlain")
    public ResponseEntity<?> analyzeProjectPlain(@RequestParam("file") MultipartFile multipartFile) {
        File projectDir;
        try {
            projectDir = componentStorageService.storePlain(multipartFile);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload and extract project files: " + e.getMessage());
        }
        try {
            CompletableFuture<TreeNode<CollisionReport>> analysisFuture = projectService.analyzeProject(projectDir);
            TreeNode<CollisionReport> analysisResult = analysisFuture.get();
            return ResponseEntity.ok(analysisResult);
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error during project analysis", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during project analysis: " + e.getMessage());
        } finally {
            // Clean up temporary files
            deleteRecursively(projectDir);
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteRecursively(entry);
                }
            }
        }
        file.delete();
    }
} 