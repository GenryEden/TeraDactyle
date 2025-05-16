package ru.kernelpunik.tokenizer;

import org.treesitter.TSLanguage;
import org.treesitter.TSParser;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;

import java.util.Iterator;
import java.util.List;


public class CompositionDetector {
    final Fingerprinter fingerprinter;
    final FingerprintRepository fingerprintRepository;
    public CompositionDetector(TSLanguage language, FingerprintRepository fingerprintRepository) {
        TSParser tsParser = new TSParser();
        tsParser.setLanguage(language);
        fingerprinter = new Fingerprinter(tsParser);
        this.fingerprintRepository = fingerprintRepository;
    }

    public CollisionReport processSolution(Component solution) {
        return null;
    }
}
