package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.paperreview.entity.Student;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByDivision(String division);
}