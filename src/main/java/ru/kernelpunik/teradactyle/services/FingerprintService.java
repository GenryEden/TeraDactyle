package ru.kernelpunik.teradactyle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.tokenizer.ComponentProcessor;

import java.io.File;
import java.io.IOException;

@Service
public class FingerprintService implements IFingerprintService {
    private final FingerprintRepository fingerprintRepository;
    private final ComponentProcessor componentProcessor;

    public FingerprintService(FingerprintRepository fingerprintRepository) {
        this.fingerprintRepository = fingerprintRepository;
        this.componentProcessor = new ComponentProcessor(fingerprintRepository);
    }

    @Override
    public long addFingerprints(Component component, File fileTree) throws IOException {
        long cntBefore = fingerprintRepository.countByComponentId(
                component.getComponentId()
        );
        componentProcessor.processComponent(component.getComponentId(), fileTree).join();
        long cntAfter = fingerprintRepository.countByComponentId(
                component.getComponentId()
        );
        return cntAfter - cntBefore;

    }
}
