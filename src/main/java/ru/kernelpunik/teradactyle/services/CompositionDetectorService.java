package ru.kernelpunik.teradactyle.services;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.Language;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.teradactyle.repositories.InterferenceRepository;
import ru.kernelpunik.teradactyle.repositories.ComponentRepository;
import ru.kernelpunik.tokenizer.CompositionDetector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompositionDetectorService implements ICompositionDetectorService {
    private static final int DEFAULT_LIMIT = 20;
    private final Map<Integer, CompositionDetector> compositionDetectorMap = new HashMap<>();
    private ComponentRepository componentRepository;
    private InterferenceRepository interferenceRepository;

    public CompositionDetectorService(
        ComponentRepository componentRepository,
        FingerprintRepository fingerprintRepository,
        InterferenceRepository interferenceRepository
    ) {
        this.componentRepository = componentRepository;
        this.interferenceRepository = interferenceRepository;
        for (Language language : Language.values()) {
            compositionDetectorMap.put(language.id, new CompositionDetector(language.tsLanguage, fingerprintRepository));
        }
    }

    @Override
    public Component putComponent(Component component) {
        return componentRepository.save(component);
    }

    @Override
    public Component getComponent(long componentId) {
        return componentRepository.findById(componentId).orElse(null);
    }

    @Override
    public List<Interference> getInterferences(long solutionId) {
        return getInterferences(solutionId, DEFAULT_LIMIT);
    }

    @Override
    public List<Interference> getInterferences(long solutionId, int limit) {
        return getInterferences(solutionId, limit, 0);
    }

    @Override
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
