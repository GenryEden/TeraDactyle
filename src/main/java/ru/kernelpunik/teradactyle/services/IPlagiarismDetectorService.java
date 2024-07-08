package ru.kernelpunik.teradactyle.services;

import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.Solution;

import java.util.List;

public interface IPlagiarismDetectorService {
    Solution putSolution(Solution solution);

    Solution getSolution(long solutionId);

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
