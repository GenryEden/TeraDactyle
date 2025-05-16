package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Component;

public interface IComponentService {
    Component getComponent(long componentId);
    Iterable<Component> getAllComponents();
    Component addComponent(Component component);
}
