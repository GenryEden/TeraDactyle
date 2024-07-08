package ru.kernelpunik.teradactyle.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.models.Solution;
import ru.kernelpunik.teradactyle.services.IPlagiarismDetectorService;
import ru.kernelpunik.teradactyle.services.LanguageService;

import java.util.List;


@RestController
@RequestMapping("/v0/api")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MainController {
    private final static int DOUBTFUL_BUT_OK_CODE = 267;
    private final LanguageService languageService;
    private final IPlagiarismDetectorService plagiarismDetectorService;
    @GetMapping(value="/getLanguages", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Language> getLanguages() {
        return languageService.getLanguages();
    }

    @PostMapping(value="/putSolution", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Long> putSolution(@RequestBody Solution solution) {
        int httpStatusCode = 200;
        if (solution.getCode() == null) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        if (solution.getName() == null || solution.getDescription() == null) {
            httpStatusCode = DOUBTFUL_BUT_OK_CODE;
        }
        return ResponseEntity
                .status(httpStatusCode)
                .body(
                    plagiarismDetectorService.putSolution(solution).getSolutionId()
                );
    }

    @GetMapping(value = "/getSolution", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Solution> getSolution(@RequestParam(name = "id") int id) {
        Solution result = plagiarismDetectorService.getSolution(id);
        if (result == null) {
            return ResponseEntity
                   .notFound()
                   .build();
        } else {
            return ResponseEntity
                   .ok(result);
        }
    }

    @GetMapping(value = "/getInterferences", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Interference>> getInterferences(
            @RequestParam(name = "solution_id") int solutionId,
            @RequestParam(name = "page_size", defaultValue = "20", required = false) int pageSize,
            @RequestParam(name = "page_number", defaultValue = "0", required = false) int pageNumber
    ) {
        return ResponseEntity.ok(plagiarismDetectorService.getInterferences(solutionId, pageSize, pageNumber));
    }

}
