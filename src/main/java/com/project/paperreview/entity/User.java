// === User.java ===
package com.project.paperreview.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Role: "STUDENT", "TEACHER", or "ADMIN"
    private String role;

    // NEW: stores the PRN of the student user.
    // For TEACHER and ADMIN users, this will be null.
    // Hibernate will create a 'prn' column in the users table.
    private String prn;

    // ── GETTERS & SETTERS ──────────────────────────────

    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPrn() { return prn; }
    public void setPrn(String prn) { this.prn = prn; }
}