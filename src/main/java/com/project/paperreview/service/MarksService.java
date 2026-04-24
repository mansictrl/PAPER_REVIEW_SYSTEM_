// src/main/java/com/project/paperreview/service/MarksService.java
package com.project.paperreview.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.paperreview.entity.Marks;
import com.project.paperreview.repository.MarksRepository;

import java.util.List;
import java.util.Optional;

@Service
public class MarksService {

    @Autowired
    private MarksRepository marksRepo;

    @Autowired
    private QueryService queryService;

    // ── UPSERT MARKS ──────────────────────────────────────────────────────────
    /**
     * Insert OR Update marks for one (prn, subjectId, question) combination.
     *
     * FIX: Validation runs BEFORE setting the value on the entity.
     * Old code set marksObtained first, then checked — which meant the
     * bad value was already on the object even if we threw an exception.
     */
    public Marks upsertMarks(String prn, int subjectId, String question,
                             int marksObtained, int maxMarks) {

        // STEP 1 — Validate BEFORE touching any DB object
        if (marksObtained < 0) {
            throw new RuntimeException("Marks cannot be negative");
        }
        if (marksObtained > maxMarks) {
            throw new RuntimeException(
                "Marks (" + marksObtained + ") cannot exceed maximum marks (" + maxMarks + ")");
        }

        // STEP 2 — Find existing row or create new
        Optional<Marks> existing = marksRepo.findExact(prn, subjectId, question);

        Marks m;
        if (existing.isPresent()) {
            m = existing.get();   // UPDATE path
        } else {
            m = new Marks();      // INSERT path
            m.setPrn(prn);
            m.setSubjectId(subjectId);
            m.setQuestion(question);
        }

        // STEP 3 — Set validated values and save
        m.setMarksObtained(marksObtained);
        m.setMaxMarks(maxMarks);
        return marksRepo.save(m);
    }

    // ── ORIGINAL ADD (kept for JDBC demo controller) ──────────────────────────
    public Marks addMarks(Marks m) {
        return marksRepo.save(m);
    }

    // ── FETCH ─────────────────────────────────────────────────────────────────
    public List<Marks> getMarksByPrn(String prn) {
        return marksRepo.findByPrn(prn);
    }

    public List<Marks> getMarksByPrnAndSubject(String prn, int subjectId) {
        return marksRepo.findByPrnAndSubjectId(prn, subjectId);
    }

    public List<Marks> getAllMarks() {
        return marksRepo.findAll();
    }

    // ── UPDATE MARKS + RESOLVE QUERY ──────────────────────────────────────────
    public void updateMarksAndResolveQuery(String prn, int subjectId,
            String question,
            int newMarks, int queryId) {

Optional<Marks> existing =
marksRepo.findExact(prn, subjectId, question);

if (existing.isEmpty()) {
throw new RuntimeException("Marks record not found for update");
}

Marks m = existing.get();

// validation
if (newMarks < 0 || newMarks > m.getMaxMarks()) {
throw new RuntimeException("Invalid marks");
}

m.setMarksObtained(newMarks);
marksRepo.save(m);

queryService.resolveQuery((long) queryId);
 }
}