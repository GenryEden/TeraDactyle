package ru.kernelpunik.teradactyle.repositories;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.kernelpunik.teradactyle.models.Fingerprint;
import ru.kernelpunik.teradactyle.models.FingerprintId;

import java.util.List;

@Repository
public interface FingerprintRepository extends CrudRepository<Fingerprint, FingerprintId> {
    List<Fingerprint> findByValueAndLanguageId(
            @Param("value") int value,
            @Param("language_id") long languageId
    );

    long countByComponentId(long componentId);
}
