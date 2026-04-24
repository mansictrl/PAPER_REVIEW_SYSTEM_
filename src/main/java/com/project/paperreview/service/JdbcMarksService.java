package com.project.paperreview.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.project.paperreview.dao.JdbcMarksDao;
import com.project.paperreview.entity.Marks;

import java.util.List;

@Service
public class JdbcMarksService {

    @Autowired
    private JdbcMarksDao dao;

    public void addMarks(Marks m) {
        dao.insertMarks(m);
    }

    public void addBatch(List<Marks> list) {
        dao.batchInsert(list);
    }

    public List<Marks> getMarks(String prn) {
        return dao.getMarksByPrn(prn);
    }
}