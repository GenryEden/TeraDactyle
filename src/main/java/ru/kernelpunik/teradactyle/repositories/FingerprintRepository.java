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
    @Query(
            "select f from Fingerprint f join Solution s on s.solutionId = f.solutionId where f.value = :value " +
                    "and s.languageId = :language_id"
    )
    List<Fingerprint> getAllFingerprintsByValueAndLanguage(
            @Param("value") int value,
            @Param("language_id") long language_id
    );
}
