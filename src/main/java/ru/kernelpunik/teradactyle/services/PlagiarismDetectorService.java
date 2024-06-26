package ru.kernelpunik.teradactyle.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.models.Solution;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.repositories.InterferenceRepository;
import ru.kernelpunik.teradactyle.repositories.SolutionRepository;
import ru.kernelpunik.tokenizer.CollisionReport;
import ru.kernelpunik.tokenizer.PlagiarismDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlagiarismDetectorService {
    private static final int DEFAULT_LIMIT = 20;
    private final Map<Integer, PlagiarismDetector> plagiarismDetectorMap = new HashMap<>();
    private SolutionRepository solutionRepository;
    private FingerprintRepository fingerprintRepository;
    private InterferenceRepository interferenceRepository;

    public PlagiarismDetectorService(
        SolutionRepository solutionRepository,
        FingerprintRepository fingerprintRepository,
        InterferenceRepository interferenceRepository
    ) {
        this.solutionRepository = solutionRepository;
        this.fingerprintRepository = fingerprintRepository;
        this.interferenceRepository = interferenceRepository;
        for (Language language : Language.values()) {
            plagiarismDetectorMap.put(language.id, new PlagiarismDetector(language.tsLanguage, fingerprintRepository));
        }
    }

    public Solution putSolution(Solution solution) {
        Solution updSolution = solutionRepository.save(solution);
        PlagiarismDetector plagiarismDetector = plagiarismDetectorMap.getOrDefault(solution.getLanguageId(), null);
        CollisionReport collisionReport = plagiarismDetector.processSolution(solution);
        for (Map.Entry<Long, Integer> entry: collisionReport.getCollisions().entrySet()) {
            interferenceRepository.save(
                new Interference(
                    updSolution.getSolutionId(),
                    updSolution,
                    entry.getKey(),
                    null,
                        entry.getValue() * 1.0f / collisionReport.getTotalFingerprints()
                )
            );
        }
        return updSolution;
    }

    public Solution getSolution(long solutionId) {
        return solutionRepository.findById(solutionId).orElse(null);
    }

    public List<Interference> getInterferences(long solutionId) {
        return getInterferences(solutionId, DEFAULT_LIMIT);
    }

    public List<Interference> getInterferences(long solutionId, int limit) {
        return getInterferences(solutionId, limit, 0);
    }

    public List<Interference> getInterferences(long solutionId, int limit, int page) {
        return interferenceRepository.findBySolutionId(
                solutionId,
                PageRequest.of(
                        page,
                        limit,
                        Sort.by(
                                new Sort.Order(Sort.Direction.DESC, "interferenceFraction")
                        )
                )
        );
    }
}
