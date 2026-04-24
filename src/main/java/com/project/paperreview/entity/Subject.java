// === Subject.java ===
package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
public class Subject {

    @Id
    private int id;

    @NotBlank(message = "Subject name is required")
    private String name;

    // One Subject → Many Marks
    @OneToMany(mappedBy = "subject", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Marks> marksList;

    // ── GETTERS & SETTERS ──────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Marks> getMarksList() { return marksList; }
    public void setMarksList(List<Marks> marksList) { this.marksList = marksList; }
}