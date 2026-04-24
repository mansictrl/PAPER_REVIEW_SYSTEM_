package com.project.paperreview.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.project.paperreview.entity.Marks;
import com.project.paperreview.service.JdbcMarksService;

import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/jdbc/marks")
public class JdbcMarksController {

    @Autowired
    private JdbcMarksService service;

    // 🔹 SINGLE INSERT
    @GetMapping("/add")
    public String addMarks() {

        Marks m = new Marks();
        m.setPrn("101");
        m.setSubjectId(1);
        m.setQuestion("Q1");
        m.setMarksObtained(5);
        m.setMaxMarks(10);

        service.addMarks(m);

        return "Inserted using JDBC!";
    }

    // 🔥 BATCH INSERT DEMO
    @GetMapping("/batch")
    public String batchInsert() {

        List<Marks> list = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Marks m = new Marks();
            m.setPrn("101");
            m.setSubjectId(1);
            m.setQuestion("Q" + i);
            m.setMarksObtained(5);
            m.setMaxMarks(10);
            list.add(m);
        }

        service.addBatch(list);

        return "Batch Insert Done!";
    }

    // 🔹 FETCH
    @GetMapping("/student/{prn}")
    public List<Marks> getMarks(@PathVariable String prn) {
        return service.getMarks(prn);
    }
}