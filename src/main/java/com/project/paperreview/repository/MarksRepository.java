// === repository/MarksRepository.java ===
package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.project.paperreview.entity.Marks;

import java.util.List;
import java.util.Optional;

public interface MarksRepository extends JpaRepository<Marks, Integer> {

    List<Marks> findByPrn(String prn);

    // Used for subject-wise filtering in student dashboard
    List<Marks> findByPrnAndSubjectId(String prn, int subjectId);

    // Used by upsert logic — find exact row for one question
    // Returns Optional so we can check if it exists
    @Query("SELECT m FROM Marks m WHERE m.prn = :prn AND m.subjectId = :subjectId AND m.question = :question")
    Optional<Marks> findExact(
            @Param("prn") String prn,
            @Param("subjectId") int subjectId,
            @Param("question") String question
    );

    // Used by admin analytics: average per subject
    @Query("SELECT m.subjectId, AVG(m.marksObtained), AVG(m.maxMarks) FROM Marks m GROUP BY m.subjectId")
    List<Object[]> avgMarksBySubject();

    // Used by admin analytics: average per division
    @Query("SELECT s.division, AVG(m.marksObtained), AVG(m.maxMarks) " +
           "FROM Marks m JOIN Student s ON m.prn = s.prn " +
           "GROUP BY s.division")
    List<Object[]> avgMarksByDivision();
}