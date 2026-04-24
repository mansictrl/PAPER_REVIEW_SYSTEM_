// === UserRepository.java ===
package com.project.paperreview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.project.paperreview.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring Data JPA auto-generates:  SELECT * FROM users WHERE username = ?
    User findByUsername(String username);

    // NEW: check if a PRN already has an account
    // This auto-generates: SELECT * FROM users WHERE prn = ?
    User findByPrn(String prn);
}