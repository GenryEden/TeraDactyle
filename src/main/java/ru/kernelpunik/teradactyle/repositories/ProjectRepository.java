package ru.kernelpunik.teradactyle.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kernelpunik.teradactyle.models.Project;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {
} 