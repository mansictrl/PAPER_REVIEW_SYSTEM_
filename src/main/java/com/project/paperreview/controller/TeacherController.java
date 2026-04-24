// === controller/TeacherController.java ===
package com.project.paperreview.controller;

import com.project.paperreview.entity.Teacher;
import com.project.paperreview.repository.TeacherRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherRepository repo;

    /**
     * GET /teacher/my-info
     *
     * Returns the logged-in teacher's subject and division assignments.
     * The frontend uses this to restrict the dropdowns in marksEntry.html.
     *
     * Response example:
     *   {
     *     "username":   "sharma",
     *     "name":       "Prof. Sharma",
     *     "subjectIds": "1,3",
     *     "divisions":  "A,B"
     *   }
     */
    @GetMapping("/my-info")
    public Map<String, String> getMyInfo(Authentication authentication) {

        String username = authentication.getName();
        Teacher t = repo.findByUsername(username);

        Map<String, String> result = new LinkedHashMap<>();
        result.put("username",   username);
        result.put("name",       t != null ? t.getName() : username);
        result.put("subjectIds", t != null && t.getSubjectIds() != null ? t.getSubjectIds() : "");
        result.put("divisions",  t != null && t.getDivisions()  != null ? t.getDivisions()  : "");
        return result;
    }

    // All teachers list (used by admin)
    @GetMapping("/all")
    public List<Teacher> getAllTeachers() {
        return repo.findAll();
    }
}