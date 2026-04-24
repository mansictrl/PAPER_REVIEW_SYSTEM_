package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.paperreview.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Integer> {
}