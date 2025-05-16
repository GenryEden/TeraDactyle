package ru.kernelpunik.teradactyle.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kernelpunik.teradactyle.models.Component;

@Repository
public interface ComponentRepository extends CrudRepository<Component, Long> {
}
