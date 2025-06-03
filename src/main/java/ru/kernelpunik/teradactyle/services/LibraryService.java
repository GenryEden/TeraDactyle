package ru.kernelpunik.teradactyle.services;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.FingerprintRepository;
import ru.kernelpunik.tokenizer.ComponentProcessor;

import java.io.File;
import java.io.IOException;

@Primary
@Service
public class LibraryService implements ILibraryService {
    private final FingerprintRepository fingerprintRepository;
    private final ComponentProcessor componentProcessor;

    public LibraryService(FingerprintRepository fingerprintRepository) {
        this.fingerprintRepository = fingerprintRepository;
        this.componentProcessor = new ComponentProcessor(fingerprintRepository);
    }

    @Override
    public long addLibrary(Component component, File fileTree) throws IOException {
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
