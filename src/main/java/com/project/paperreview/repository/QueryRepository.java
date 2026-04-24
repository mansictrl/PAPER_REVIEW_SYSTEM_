// src/main/java/com/project/paperreview/repository/QueryRepository.java
package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.paperreview.entity.Query;

import java.util.List;
import java.util.Optional;

public interface QueryRepository extends JpaRepository<Query, Long> {

    long countByStatus(String status);

    List<Query> findByPrn(String prn);

    List<Query> findByStatus(String status);

    /**
     * Used by QueryController to detect duplicate Pending queries.
     *
     * Spring Data auto-generates:
     *   SELECT * FROM query
     *   WHERE prn=? AND subject_id=? AND question=?
     *   LIMIT 1
     *
     * We then check if the result's status is "Pending" to block a re-submission.
     */
    Optional<Query> findByPrnAndSubjectIdAndQuestion(
            String prn, int subjectId, String question);
}