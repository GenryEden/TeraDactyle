package ru.kernelpunik.teradactyle.controllers;

import lombok.RequiredArgsConstructor;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.services.IComparerService;
import ru.kernelpunik.teradactyle.services.IComponentService;
import ru.kernelpunik.teradactyle.services.IComponentStorageService;
import ru.kernelpunik.teradactyle.services.ILibraryService;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/v0/api/compare")
@RequiredArgsConstructor
public class ComparerController {
    private final static Log LOG = LogFactory.getLog(ComparerController.class);
    private final IComparerService comparerService;

    @PostMapping("/")
    public ResponseEntity<Double> addComponent(
        @RequestParam("input") String input,
        @RequestParam("reference") String reference
    ) {
        if (input == null || reference == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        try {
            return ResponseEntity.ok(comparerService.compareSolutions(
                    input,
                    reference,
                    Language.JAVA
            ));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
