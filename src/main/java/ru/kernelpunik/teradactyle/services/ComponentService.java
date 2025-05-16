package ru.kernelpunik.teradactyle.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.repositories.ComponentRepository;

@Service
@RequiredArgsConstructor
public class ComponentService implements IComponentService {
    private final ComponentRepository componentRepository;
    @Override
    public Component getComponent(long componentId) {
        return componentRepository.findById(componentId).orElse(null);
    }

    @Override
    public Iterable<Component> getAllComponents() {
        return componentRepository.findAll();
    }

    @Override
    public Component addComponent(Component component) {
         return componentRepository.save(component);
    }
}
