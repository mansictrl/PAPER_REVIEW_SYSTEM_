// === controller/RegistrationController.java ===
package com.project.paperreview.controller;

import com.project.paperreview.entity.Student;
import com.project.paperreview.entity.Teacher;
import com.project.paperreview.entity.User;
import com.project.paperreview.repository.StudentRepository;
import com.project.paperreview.repository.TeacherRepository;
import com.project.paperreview.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/register")
public class RegistrationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ── STUDENT REGISTRATION ─────────────────────────────

    @PostMapping("/student")
    public String registerStudent(
            @RequestParam String prn,
            @RequestParam String name,
            @RequestParam String division,
            @RequestParam String username,
            @RequestParam String password) {

        if (userRepository.findByUsername(username) != null)
            return "ERROR: Username already exists.";

        if (userRepository.findByPrn(prn) != null)
            return "ERROR: This PRN already has an account.";

        if (!studentRepository.existsById(prn)) {
            Student s = new Student();
            s.setPrn(prn);
            s.setName(name);
            s.setDivision(division);
            studentRepository.save(s);
        }

        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole("STUDENT");
        u.setPrn(prn);
        userRepository.save(u);

        return "Student registered! PRN: " + prn + ", Username: " + username;
    }

    // ── TEACHER REGISTRATION ─────────────────────────────

    @PostMapping("/teacher")
    public String registerTeacher(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "") String subjectIds,
            @RequestParam(required = false, defaultValue = "") String divisions) {

        System.out.println("STEP 1: Starting teacher registration");

        // Check duplicate username
        if (userRepository.findByUsername(username) != null) {
            System.out.println("ERROR: Username already exists");
            return "ERROR: Username already exists.";
        }

        // 1️⃣ Save in USERS table
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        u.setRole("TEACHER");
        u.setPrn(null);
        userRepository.save(u);

        System.out.println("STEP 2: Saved in USERS table");

        try {
            // 2️⃣ Save in TEACHER table
            Teacher t = new Teacher();
            t.setName(name);
            t.setUsername(username);

            // Handle empty values properly
            t.setSubjectIds(subjectIds == null || subjectIds.isBlank() ? null : subjectIds);
            t.setDivisions(divisions == null || divisions.isBlank() ? null : divisions);

            System.out.println("STEP 3: About to save in TEACHER table");

            teacherRepository.save(t);

            System.out.println("STEP 4: Saved in TEACHER table");

        } catch (Exception e) {
            System.out.println("ERROR saving teacher: " + e.getMessage());
            e.printStackTrace();
            return "ERROR while saving teacher";
        }

        return "Teacher created successfully: " + username;
    }

    // ── ASSIGN SUBJECT + DIVISION ─────────────────────────

    @PostMapping("/teacher/assign")
    public String assignTeacher(
            @RequestParam String username,
            @RequestParam String subjectIds,
            @RequestParam String divisions) {

        Teacher t = teacherRepository.findByUsername(username);

        if (t == null)
            return "ERROR: Teacher not found";

        t.setSubjectIds(subjectIds);
        t.setDivisions(divisions);

        teacherRepository.save(t);

        return "Teacher updated: " + username;
    }

    // ── DELETE TEACHER ───────────────────────────────────

    @DeleteMapping("/teacher/{username}")
    public String deleteTeacher(@PathVariable String username) {

        Teacher t = teacherRepository.findByUsername(username);
        if (t != null) teacherRepository.delete(t);

        User u = userRepository.findByUsername(username);
        if (u != null) userRepository.delete(u);

        return "Teacher deleted: " + username;
    }
}