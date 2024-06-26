package ru.kernelpunik.teradactyle.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kernelpunik.teradactyle.models.Solution;

@Repository
public interface SolutionRepository extends CrudRepository<Solution, Long> {
}
