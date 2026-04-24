// === controller/AdminController.java ===
package com.project.paperreview.controller;

import com.project.paperreview.entity.Marks;
import com.project.paperreview.entity.Student;
import com.project.paperreview.entity.Teacher;
import com.project.paperreview.entity.User;
import com.project.paperreview.repository.MarksRepository;
import com.project.paperreview.repository.StudentRepository;
import com.project.paperreview.repository.TeacherRepository;
import com.project.paperreview.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Admin-only REST APIs.
 * All routes under /admin/** are protected by SecurityConfig (ADMIN role only).
 *
 * GET /admin/students          → list of all students
 * GET /admin/users             → all user accounts (with role + prn)
 * GET /admin/teachers          → all teachers with subject/division info
 * GET /admin/performance       → per-student average marks (sorted by %)
 * GET /admin/report/division   → division-wise average marks
 * GET /admin/report/subject    → subject-wise average marks
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired private StudentRepository  studentRepository;
    @Autowired private UserRepository     userRepository;
    @Autowired private MarksRepository    marksRepository;
    @Autowired private TeacherRepository  teacherRepository;

    // ── ALL STUDENTS ──────────────────────────────────────────────────────────
    @GetMapping("/students")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    // ── ALL USERS (for admin user management table) ───────────────────────────
    @GetMapping("/users")
    public List<Map<String, String>> getAllUsers() {
        List<Map<String, String>> result = new ArrayList<>();
        for (User u : userRepository.findAll()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("username", u.getUsername());
            row.put("role",     u.getRole());
            row.put("prn",      u.getPrn() != null ? u.getPrn() : "-");
            result.add(row);
        }
        return result;
    }

    // ── ALL TEACHERS WITH ASSIGNMENT INFO ─────────────────────────────────────
    @GetMapping("/teachers")
    public List<Map<String, String>> getAllTeachers() {
        List<Map<String, String>> result = new ArrayList<>();
        for (Teacher t : teacherRepository.findAll()) {
            Map<String, String> row = new LinkedHashMap<>();
            row.put("name",       t.getName());
            row.put("username",   t.getUsername() != null ? t.getUsername() : "-");
            row.put("subjectIds", t.getSubjectIds() != null ? t.getSubjectIds() : "Not assigned");
            row.put("divisions",  t.getDivisions()  != null ? t.getDivisions()  : "Not assigned");
            result.add(row);
        }
        return result;
    }

    // ── PER-STUDENT PERFORMANCE ───────────────────────────────────────────────
    @GetMapping("/performance")
    public List<Map<String, Object>> getPerformance() {

        List<Student> students = studentRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (Student s : students) {
            List<Marks> marksList = marksRepository.findByPrn(s.getPrn());

            int totalObtained = 0, totalMax = 0;
            for (Marks m : marksList) {
                totalObtained += m.getMarksObtained();
                totalMax      += m.getMaxMarks();
            }

            double percentage = (totalMax > 0)
                    ? Math.round((totalObtained * 100.0 / totalMax) * 10.0) / 10.0
                    : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("prn",           s.getPrn());
            row.put("name",          s.getName());
            row.put("division",      s.getDivision());
            row.put("totalObtained", totalObtained);
            row.put("totalMax",      totalMax);
            row.put("percentage",    percentage);
            row.put("recordCount",   marksList.size());
            result.add(row);
        }

        result.sort((a, b) -> Double.compare(
                (Double) b.get("percentage"),
                (Double) a.get("percentage")));

        return result;
    }

    // ── DIVISION-WISE ANALYTICS ───────────────────────────────────────────────

    /**
     * For each division (A, B, C, D), calculate:
     *   - Number of students
     *   - Total marks obtained across all questions
     *   - Average percentage
     *
     * This powers the division-wise bar chart in admin-report.html.
     */
    @GetMapping("/report/division")
    public List<Map<String, Object>> divisionReport() {

        List<Student> allStudents = studentRepository.findAll();

        // Group students by division
        Map<String, List<Student>> byDivision = new LinkedHashMap<>();
        for (Student s : allStudents) {
            byDivision.computeIfAbsent(s.getDivision(), k -> new ArrayList<>()).add(s);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<String, List<Student>> entry : byDivision.entrySet()) {
            String division = entry.getKey();
            List<Student> students = entry.getValue();

            int totalObtained = 0, totalMax = 0;
            for (Student s : students) {
                for (Marks m : marksRepository.findByPrn(s.getPrn())) {
                    totalObtained += m.getMarksObtained();
                    totalMax      += m.getMaxMarks();
                }
            }

            double percentage = (totalMax > 0)
                    ? Math.round((totalObtained * 100.0 / totalMax) * 10.0) / 10.0
                    : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("division",      division);
            row.put("studentCount",  students.size());
            row.put("totalObtained", totalObtained);
            row.put("totalMax",      totalMax);
            row.put("percentage",    percentage);
            result.add(row);
        }

        // Sort by percentage descending
        result.sort((a, b) -> Double.compare(
                (Double) b.get("percentage"),
                (Double) a.get("percentage")));

        return result;
    }

    // ── SUBJECT-WISE ANALYTICS ────────────────────────────────────────────────

    /**
     * For each subject ID found in marks, calculate:
     *   - Total questions answered
     *   - Average marks
     *   - Average percentage
     */
    @GetMapping("/report/subject")
    public List<Map<String, Object>> subjectReport() {

        List<Marks> allMarks = marksRepository.findAll();

        // Group marks by subjectId
        Map<Integer, List<Marks>> bySubject = new LinkedHashMap<>();
        for (Marks m : allMarks) {
            bySubject.computeIfAbsent(m.getSubjectId(), k -> new ArrayList<>()).add(m);
        }

        // Subject name lookup map
        Map<Integer, String> subjectNames = new LinkedHashMap<>();
        subjectNames.put(1, "ADS");
        subjectNames.put(2, "Java");
        subjectNames.put(3, "TOC");
        subjectNames.put(4, "DBMS");

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<Integer, List<Marks>> entry : bySubject.entrySet()) {
            int subjectId = entry.getKey();
            List<Marks> marks = entry.getValue();

            int totalObtained = 0, totalMax = 0;
            for (Marks m : marks) {
                totalObtained += m.getMarksObtained();
                totalMax      += m.getMaxMarks();
            }

            double percentage = (totalMax > 0)
                    ? Math.round((totalObtained * 100.0 / totalMax) * 10.0) / 10.0
                    : 0.0;

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("subjectId",   subjectId);
            row.put("subjectName", subjectNames.getOrDefault(subjectId, "Subject " + subjectId));
            row.put("totalEntries",marks.size());
            row.put("avgObtained", totalObtained > 0 ? Math.round((totalObtained * 10.0 / marks.size())) / 10.0 : 0);
            row.put("percentage",  percentage);
            result.add(row);
        }

        result.sort((a, b) -> Double.compare(
                (Double) b.get("percentage"),
                (Double) a.get("percentage")));

        return result;
    }
}