// === repository/TeacherRepository.java ===
package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.paperreview.entity.Teacher;

public interface TeacherRepository extends JpaRepository<Teacher, Integer> {

    // Find teacher by their login username
    // Spring Data auto-generates: SELECT * FROM teacher WHERE username = ?
    Teacher findByUsername(String username);
}