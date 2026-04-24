// === controller/MarksController.java ===
package com.project.paperreview.controller;

import com.project.paperreview.entity.Marks;
import com.project.paperreview.entity.User;
import com.project.paperreview.entity.Teacher;
import com.project.paperreview.repository.UserRepository;
import com.project.paperreview.repository.TeacherRepository;
import com.project.paperreview.service.MarksService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/marks")
public class MarksController {

    @Autowired
    private MarksService marksService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    // ── ADD/UPDATE MARKS (UPSERT) ─────────────────────────────────────────────

    /**
     * This is the main marks entry endpoint used by marksEntry.html.
     *
     * CHANGED from old /marks/add:
     *   OLD: always INSERT → caused duplicates
     *   NEW: upsert → INSERT if not exists, UPDATE if exists
     *
     * TEACHER RESTRICTION:
     *   Checks that the logged-in teacher is allowed to enter marks
     *   for the requested subjectId and the student's division.
     *
     * URL: GET /marks/add?prn=&subjectId=&question=&marks=&division=
     */
    @GetMapping("/add")
    public ResponseEntity<String> addMarks(
            @RequestParam String prn,
            @RequestParam int subjectId,
            @RequestParam String question,
            @RequestParam int marks,
            @RequestParam(required = false, defaultValue = "") String division,
            Authentication authentication) {

        String loggedInUsername = authentication.getName();
        boolean isTeacher = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("TEACHER"));

        // Only restrict if the caller is a Teacher (not Admin)
        if (isTeacher) {
            Teacher teacher = teacherRepository.findByUsername(loggedInUsername);

            // Check subject access
            if (teacher != null && !teacher.isAllowedSubject(subjectId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: you are not assigned to subject " + subjectId);
            }

            // Check division access (only if division param provided)
            if (teacher != null && !division.isBlank() && !teacher.isAllowedDivision(division)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Access denied: you are not assigned to division " + division);
            }
        }

        // Upsert — no duplicates
        marksService.upsertMarks(prn, subjectId, question, marks, 10);
        return ResponseEntity.ok("Marks saved!");
    }

    // ── VIEW ALL MARKS ────────────────────────────────────────────────────────
    @GetMapping("/all")
    public List<Marks> getAllMarks() {
        return marksService.getAllMarks();
    }

    // ── VIEW MARKS BY PRN (security-checked) ──────────────────────────────────
    @GetMapping("/student/{prn}")
    public ResponseEntity<?> getMarksByStudent(
            @PathVariable String prn,
            @RequestParam(required = false) Integer subjectId,
            Authentication authentication) {

        String loggedInUsername = authentication.getName();

        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("STUDENT"));

        if (isStudent) {
            User user = userRepository.findByUsername(loggedInUsername);
            if (user == null || user.getPrn() == null) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "No PRN linked to your account."));
            }
            if (!user.getPrn().equals(prn)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Access denied."));
            }
        }

        // Subject filter: if subjectId param given, return only that subject's marks
        List<Marks> marksList = (subjectId != null)
                ? marksService.getMarksByPrnAndSubject(prn, subjectId)
                : marksService.getMarksByPrn(prn);

        return ResponseEntity.ok(marksList);
    }

    // ── UPDATE MARKS + RESOLVE QUERY ──────────────────────────────────────────
    @PostMapping("/update")
    public String updateMarks(
            @RequestParam String prn,
            @RequestParam int subjectId,
            @RequestParam String question,
            @RequestParam int newMarks,
            @RequestParam int queryId) {

        marksService.updateMarksAndResolveQuery(prn, subjectId, question, newMarks, queryId);
        return "Marks Updated & Query Resolved!";
    }
}