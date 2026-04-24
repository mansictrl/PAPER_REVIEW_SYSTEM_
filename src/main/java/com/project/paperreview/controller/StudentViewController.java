// === controller/StudentViewController.java ===
package com.project.paperreview.controller;

import com.project.paperreview.entity.Marks;
import com.project.paperreview.entity.Subject;
import com.project.paperreview.entity.User;
import com.project.paperreview.repository.SubjectRepository;
import com.project.paperreview.repository.UserRepository;
import com.project.paperreview.service.MarksService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * CO2 — Thymeleaf Controller
 *
 * Serves the student marks page with optional subject filtering.
 * This is the ONLY marks view for students — removes duplicate flow.
 */
@Controller
public class StudentViewController {

    @Autowired private MarksService     marksService;
    @Autowired private UserRepository   userRepository;
    @Autowired private SubjectRepository subjectRepository;

    /**
     * GET /view/marks
     * GET /view/marks?subjectId=1   (filter by subject)
     *
     * 1. Reads logged-in username from Spring Security session
     * 2. Looks up their PRN from users table
     * 3. Fetches marks (all, or filtered by subjectId)
     * 4. Passes to Thymeleaf template
     */
    @GetMapping("/view/marks")
    public String viewMyMarks(
            @RequestParam(required = false) Integer subjectId,
            Authentication authentication,
            Model model) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username);

        if (user == null || user.getPrn() == null) {
            model.addAttribute("error", "No student record linked to your account.");
            model.addAttribute("marksList", List.of());
            model.addAttribute("subjects", subjectRepository.findAll());
            return "student-marks";
        }

        String prn = user.getPrn();

        // Fetch marks — filtered or all
        List<Marks> marksList = (subjectId != null)
                ? marksService.getMarksByPrnAndSubject(prn, subjectId)
                : marksService.getMarksByPrn(prn);

        // All subjects for the dropdown
        List<Subject> subjects = subjectRepository.findAll();

        model.addAttribute("prn",              prn);
        model.addAttribute("name",             user.getUsername());
        model.addAttribute("marksList",        marksList);
        model.addAttribute("total",            marksList.size());
        model.addAttribute("subjects",         subjects);
        model.addAttribute("selectedSubjectId",subjectId);

        return "student-marks";
    }
}