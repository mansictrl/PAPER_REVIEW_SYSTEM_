// src/main/java/com/project/paperreview/controller/QueryController.java
package com.project.paperreview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.project.paperreview.entity.Query;
import com.project.paperreview.repository.QueryRepository;
import com.project.paperreview.service.QueryService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/query")
public class QueryController {

    @Autowired
    private QueryService queryService;

    @Autowired
    private QueryRepository queryRepository;

    // ── ADD QUERY (with duplicate prevention) ─────────────────────────────────
    /**
     * POST /query/add
     *
     * Before saving, checks if a Pending query already exists
     * for the same PRN + subjectId + question.
     *
     * Returns plain text:
     *   "Query Submitted Successfully!"    → saved OK
     *   "DUPLICATE: ..."                  → blocked, not saved
     *
     * Frontend reads the response text and shows the error message
     * instead of the success screen when it starts with "DUPLICATE:".
     */
    @PostMapping("/add")
    public ResponseEntity<String> addQuery(
            @RequestParam String prn,
            @RequestParam int subjectId,
            @RequestParam String question,
            @RequestParam String message) {

        // 🔴 STEP 1 — Check duplicate FIRST
        Optional<Query> existing =
            queryRepository.findByPrnAndSubjectIdAndQuestion(prn, subjectId, question);

        if (existing.isPresent()) {
            Query q = existing.get();

            if ("Pending".equalsIgnoreCase(q.getStatus())) {
                return ResponseEntity
                    .badRequest()
                    .body("Query already submitted for this question");
            }
        }

        try {
            // 🔴 STEP 2 — Save query
            Query q = new Query();
            q.setPrn(prn);
            q.setSubjectId(subjectId);
            q.setQuestion(question);
            q.setMessage(message);
            q.setStatus("Pending");

            queryService.addQuery(q);

            return ResponseEntity.ok("Query Submitted Successfully!");

        } catch (Exception e) {
            // 🔴 STEP 3 — Catch DB duplicate error (IMPORTANT)
            return ResponseEntity
                .badRequest()
                .body("Query already submitted (DB constraint)");
        }
    }

    // ── VIEW ALL QUERIES ──────────────────────────────────────────────────────
    @GetMapping("/all")
    public List<Query> getAllQueries() {
        return queryService.getAllQueries();
    }

    // ── VIEW STUDENT QUERIES ──────────────────────────────────────────────────
    @GetMapping("/student/{prn}")
    public List<Query> getStudentQueries(@PathVariable String prn) {
        return queryService.getQueriesByPrn(prn);
    }

    // ── RESOLVE QUERY ─────────────────────────────────────────────────────────
    @GetMapping("/resolve/{id}")
    public String resolveQuery(@PathVariable Long id) {
        queryService.resolveQuery(id);
        return "Query Resolved!";
    }

    // ── PENDING QUERIES ───────────────────────────────────────────────────────
    @GetMapping("/pending")
    public List<Query> getPendingQueries() {
        return queryService.getPendingQueries();
    }

    // ── DELETE QUERY (Admin only — enforced in SecurityConfig) ────────────────
    /**
     * DELETE /query/{id}
     *
     * Admin can delete spam, duplicate, or irrelevant queries.
     * Used by the Delete button in viewQueries.html (visible only to ADMIN).
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteQuery(@PathVariable Long id) {
        if (!queryRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Query not found: " + id);
        }
        queryRepository.deleteById(id);
        return ResponseEntity.ok("Query deleted.");
    }
}