// === Query.java ===
package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "PRN is required")
    private String prn;

    private int subjectId;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Message cannot be empty")
    @Size(min = 5, max = 500, message = "Message must be 5-500 characters")
    private String message;

    private String status;

    // ── GETTERS & SETTERS ──────────────────────────────

    public int getId() { return id; }

    public String getPrn() { return prn; }
    public void setPrn(String prn) { this.prn = prn; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}