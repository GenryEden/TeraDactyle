package ru.kernelpunik.teradactyle.controllers;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.services.IComponentService;
import ru.kernelpunik.teradactyle.services.IComponentStorageService;
import ru.kernelpunik.teradactyle.services.ILibraryService;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/v0/api/component")
@RequiredArgsConstructor
public class ComponentController {
    private final static Log LOG = LogFactory.getLog(ComponentController.class);
    private final IComponentStorageService componentStorageService;
    private final IComponentService componentService;
    private final ILibraryService libraryService;

    @PostMapping("/")
    public ResponseEntity<Component> addComponent(
        @RequestBody Component component
    ) {
        if (component.getComponentId() != 0) {
            return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .build();
        }
        if (component.getName() == null) {
            return ResponseEntity
                   .status(HttpStatus.BAD_REQUEST)
                   .build();
        }
        return ResponseEntity.ok(componentService.addComponent(component));
    }

    @PatchMapping("/uploadZip")
    public ResponseEntity<String> upload(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("component_id") long componentId
        ) {
        File ans;
        Component component = componentService.getComponent(componentId);
        if (component == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Component not found");
        }
        try {
            ans = componentStorageService.storeZip(multipartFile);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to upload the file");
        } catch (ZipException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Uploaded file cannot be unpacked");
        }
        long newFingerprints = -1;
        try {
            newFingerprints = libraryService.addLibrary(component, ans);
        } catch (IOException e) {
            return ResponseEntity
                   .status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Unable to process the file");
        } finally {
            try {
                FileUtils.deleteDirectory(ans);
            } catch (IOException e) {
                 LOG.error("Unable to delete the directory " + ans.getAbsolutePath(), e);
            }
        }
        return ResponseEntity.ok(ans.getAbsolutePath() + " newFingerprints=" + newFingerprints);
    }

    @PatchMapping("/uploadPlain")
    public ResponseEntity<String> uploadAFile(
            @RequestParam("file") MultipartFile multipartFile,
            @RequestParam("component_id") long componentId
    ) {
        File ans;
        Component component = componentService.getComponent(componentId);
        if (component == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Component not found");
        }
        try {
            ans = componentStorageService.storePlain(multipartFile);
        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Unable to upload file");
        }
        long newFingerprints = -1;
        try {
            newFingerprints = libraryService.addLibrary(component, ans);
        } catch (IOException e) {
            return ResponseEntity
                   .status(HttpStatus.INTERNAL_SERVER_ERROR)
                   .body("Unable to process the file");
        } finally {
            ans.delete();
        }
        return ResponseEntity.ok(ans.getAbsolutePath() + " newFingerprints=" + newFingerprints);
    }
}
