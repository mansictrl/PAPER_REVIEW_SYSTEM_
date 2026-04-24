package com.project.paperreview.controller;

import com.project.paperreview.entity.User;
import com.project.paperreview.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Returns the logged-in user's basic info.
 *
 * The frontend calls GET /api/me and gets back:
 *   { "username": "priya", "role": "STUDENT", "prn": "202" }
 *
 * This allows student.html to auto-fill the PRN and display the name
 * without the student having to type it.
 */
@RestController
public class UserInfoController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/me")
    public Map<String, String> getMe(Authentication authentication) {

        String username = authentication.getName();
        String role     = authentication.getAuthorities().iterator().next().getAuthority();

        User user = userRepository.findByUsername(username);
        String prn = (user != null) ? user.getPrn() : null;

        return Map.of(
                "username", username,
                "role",     role,
                "prn",      prn != null ? prn : ""
        );
    }
}