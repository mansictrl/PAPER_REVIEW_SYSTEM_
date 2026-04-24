package com.project.paperreview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.paperreview.entity.Student;
import com.project.paperreview.repository.StudentRepository;

import java.util.List;

@RestController
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private StudentRepository repo;

    // Test API
    @GetMapping("/test")
    public String test() {
        return "Working!";
    }

    // Add student
    @GetMapping("/add")
    public String addStudent() {
        Student s = new Student();
        s.setPrn("101");
        s.setName("Swapnil");
        s.setDivision("A");

        repo.save(s);

        return "Student Added!";
    }

    // NEW METHOD (IMPORTANT)
    @GetMapping("/all")
    public List<Student> getAllStudents() {
        return repo.findAll();
    }
    @GetMapping("/division/{div}")
    public List<Student> getByDivision(@PathVariable String div) {
        return repo.findByDivision(div);
    }
}