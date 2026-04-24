// === Student.java ===
package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;

@Entity
@Table(name = "student")
public class Student {

    @Id
    @NotBlank(message = "PRN is required")
    @Size(min = 1, max = 20, message = "PRN must be 1-20 characters")
    private String prn;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be 2-100 characters")
    private String name;

    @NotBlank(message = "Division is required")
    private String division;

    // NEW: One Student → Many Marks records
    // mappedBy = "student" means Marks entity holds the foreign key
    // FetchType.LAZY = marks are NOT loaded until explicitly needed (good performance)
    // @JsonIgnore = prevents infinite loop in JSON response
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Marks> marksList;

    // ── GETTERS & SETTERS ──────────────────────────────

    public String getPrn() { return prn; }
    public void setPrn(String prn) { this.prn = prn; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }

    public List<Marks> getMarksList() { return marksList; }
    public void setMarksList(List<Marks> marksList) { this.marksList = marksList; }
}