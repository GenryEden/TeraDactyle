package ru.kernelpunik.teradactyle.repositories;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kernelpunik.teradactyle.models.Interference;
import ru.kernelpunik.teradactyle.models.InterferenceId;

import java.util.List;

@Repository
public interface InterferenceRepository extends CrudRepository<Interference, InterferenceId> {
    List<Interference> findBySolutionId(long solutionId);
    List<Interference> findBySolutionId(long solutionId, Pageable pageable);
}
