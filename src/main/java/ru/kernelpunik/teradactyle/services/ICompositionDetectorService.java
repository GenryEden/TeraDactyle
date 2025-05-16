package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Component;
import ru.kernelpunik.teradactyle.models.Interference;

import java.util.List;

public interface ICompositionDetectorService {
    Component putComponent(Component solution);

    Component getComponent(long componentId);

    List<Interference> getInterferences(long solutionId);

    default List<Interference> getInterferences(long solutionId, int limit) {
        return getInterferences(solutionId).stream().limit(limit).toList();
    }

    default List<Interference> getInterferences(long solutionId, int limit, int page) {
        return getInterferences(solutionId).stream()
               .skip(page * limit)
               .limit(limit)
               .toList();
    }
}
