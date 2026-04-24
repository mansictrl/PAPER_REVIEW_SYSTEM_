package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class Marks {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "prn")
    private String prn;

    @Column(name = "subject_id")
    private int subjectId;

    @NotBlank(message = "Question is required")
    private String question;

    @Min(value = 0,  message = "Marks cannot be negative")
    @Max(value = 100, message = "Marks cannot exceed 100")
    private int marksObtained;

    private int maxMarks;

    // 🔹 Student relation
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "prn", insertable = false, updatable = false)
    @JsonIgnore
    private Student student;

    // 🔹 Subject relation (ONLY ONE — FIXED)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", insertable = false, updatable = false)
    private Subject subject;

    // ===== GETTERS & SETTERS =====

    public int getId() { return id; }

    public String getPrn() { return prn; }
    public void setPrn(String prn) { this.prn = prn; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public int getMarksObtained() { return marksObtained; }
    public void setMarksObtained(int marks) { this.marksObtained = marks; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
}