package ru.kernelpunik.tokenizer;

import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.Solution;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;


public class PlagiarismDetector {
    final Fingerprinter fingerprinter;
    final FingerprintRepository fingerprintRepository;
    public PlagiarismDetector(TSLanguage language, FingerprintRepository fingerprintRepository) {
        TSParser tsParser = new TSParser();
        tsParser.setLanguage(language);
        fingerprinter = new Fingerprinter(tsParser);
        this.fingerprintRepository = fingerprintRepository;

    }

    public CollisionReport processSolution(Solution solution) {
        Iterator<Integer> fingerprints = fingerprinter.getFingerprints(solution.getCode());
        CollisionReport collisionReport = new CollisionReport();
        while (fingerprints.hasNext()) {
            int fingerprint = fingerprints.next();
            collisionReport.addFingerprint();
            List<Fingerprint> foundFingerprints = fingerprintRepository.getAllFingerprintsByValueAndLanguage(
                fingerprint,
                solution.getLanguageId()
            );
            for (Fingerprint collision: foundFingerprints) {
                if (!solution.equals(collision.getSolution())) {
                    collisionReport.addCollisionWith(collision.getSolutionId());
                }
            }
            fingerprintRepository.save(new Fingerprint(fingerprint, solution.getSolutionId(), solution));
        }
        return collisionReport;
    }
}
