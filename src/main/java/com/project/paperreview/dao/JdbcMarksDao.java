	package com.project.paperreview.dao;
	
	import java.sql.*;
	import java.util.ArrayList;
	import java.util.List;
	
	import org.springframework.stereotype.Repository;
	
	import com.project.paperreview.entity.Marks;
	
	@Repository
	public class JdbcMarksDao {
	
	    private final String URL = "jdbc:mysql://localhost:3306/paper_system";
	    private final String USER = "root";
	    private final String PASS = "root"; // change this
	
	    // 🔹 Connection
	    private Connection getConnection() throws Exception {
	        return DriverManager.getConnection(URL, USER, PASS);
	    }
	
	    // 🔹 SINGLE INSERT
	    public void insertMarks(Marks m) {
	
	        String sql = "INSERT INTO marks (prn, subject_id, question, marks_obtained, max_marks) VALUES (?, ?, ?, ?, ?)";
	
	        try (Connection con = getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	
	            ps.setString(1, m.getPrn());
	            ps.setInt(2, m.getSubjectId());
	            ps.setString(3, m.getQuestion());
	            ps.setInt(4, m.getMarksObtained());
	            ps.setInt(5, m.getMaxMarks());
	
	            ps.executeUpdate();
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	
	    // 🔥 BATCH INSERT (IMPORTANT)
	    public void batchInsert(List<Marks> list) {
	
	        String sql = "INSERT INTO marks (prn, subject_id, question, marks_obtained, max_marks) VALUES (?, ?, ?, ?, ?)";
	
	        try (Connection con = getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	
	            for (Marks m : list) {
	                ps.setString(1, m.getPrn());
	                ps.setInt(2, m.getSubjectId());
	                ps.setString(3, m.getQuestion());
	                ps.setInt(4, m.getMarksObtained());
	                ps.setInt(5, m.getMaxMarks());
	
	                ps.addBatch();
	            }
	
	            ps.executeBatch(); // 🔥 key line
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	
	    // 🔹 FETCH MARKS
	    public List<Marks> getMarksByPrn(String prn) {
	
	        List<Marks> list = new ArrayList<>();
	
	        String sql = "SELECT * FROM marks WHERE prn = ?";
	
	        try (Connection con = getConnection();
	             PreparedStatement ps = con.prepareStatement(sql)) {
	
	            ps.setString(1, prn);
	
	            ResultSet rs = ps.executeQuery();
	
	            while (rs.next()) {
	                Marks m = new Marks();
	                m.setPrn(rs.getString("prn"));
	                m.setSubjectId(rs.getInt("subject_id"));
	                m.setQuestion(rs.getString("question"));
	                m.setMarksObtained(rs.getInt("marks_obtained"));
	                m.setMaxMarks(rs.getInt("max_marks"));
	
	                list.add(m);
	            }
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	
	        return list;
	    }
	}