package com.project.paperreview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.paperreview.entity.Subject;
import com.project.paperreview.repository.SubjectRepository;

import java.util.List;

@RestController
@RequestMapping("/subject")
public class SubjectController {

    @Autowired
    private SubjectRepository repo;

    // OPTIONAL (not needed if using SQL)
    @GetMapping("/add")
    public String addSubject() {
        Subject s = new Subject();
        s.setId(1);
        s.setName("Data Structures");

        repo.save(s);

        return "Subject Added!";
    }

    @GetMapping("/all")
    public List<Subject> getAllSubjects() {
        return repo.findAll();
    }
}