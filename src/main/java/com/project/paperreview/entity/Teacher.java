// === entity/Teacher.java ===
package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * Teacher entity.
 *
 * Key design decisions:
 *   username   → links this Teacher to the User in the 'users' table
 *               (same value as users.username for that teacher)
 *   subjectIds → comma-separated subject IDs this teacher is allowed to teach
 *               e.g. "1,3"  means Subject IDs 1 and 3
 *   divisions  → comma-separated divisions e.g. "A,B" or "B,D"
 *
 * We use comma-separated strings (not a join table) to keep it SY-level simple.
 * Hibernate auto-creates/updates the 'teacher' table via ddl-auto=update.
 */
@Entity
@Table(name = "teacher")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NotBlank(message = "Name is required")
    private String name;

    // Links to the 'users' table — same username as User.username
    @Column(unique = true)
    private String username;

    // Which subjects this teacher can teach  e.g. "1" or "1,3"
    // Subject IDs from the 'subject' table
    private String subjectIds;

    // Which divisions this teacher handles  e.g. "A" or "A,B"
    private String divisions;

    // ── GETTERS & SETTERS ───────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSubjectIds() { return subjectIds; }
    public void setSubjectIds(String subjectIds) { this.subjectIds = subjectIds; }

    public String getDivisions() { return divisions; }
    public void setDivisions(String divisions) { this.divisions = divisions; }

    // ── HELPER METHODS ───────────────────────────────────────────

    /**
     * Returns true if this teacher is allowed to teach the given subjectId.
     * e.g. subjectIds = "1,3"  → isAllowedSubject(1) = true, isAllowedSubject(2) = false
     */
    public boolean isAllowedSubject(int subjectId) {
        if (subjectIds == null || subjectIds.isBlank()) return false;
        for (String part : subjectIds.split(",")) {
            if (part.trim().equals(String.valueOf(subjectId))) return true;
        }
        return false;
    }

    /**
     * Returns true if this teacher handles the given division.
     * e.g. divisions = "A,B"  → isAllowedDivision("A") = true, isAllowedDivision("C") = false
     */
    public boolean isAllowedDivision(String division) {
        if (divisions == null || divisions.isBlank()) return false;
        for (String part : divisions.split(",")) {
            if (part.trim().equalsIgnoreCase(division)) return true;
        }
        return false;
    }
}