package com.project.paperreview.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String redirectDashboard(Authentication auth) {

        String role = auth.getAuthorities().iterator().next().getAuthority();

        switch (role) {
            case "ADMIN":
                return "redirect:/admin.html";
            case "TEACHER":
                return "redirect:/teacher.html";
            case "STUDENT":
                return "redirect:/student.html";
            default:
                return "redirect:/";
        }
    }
}