package com.knowbot.dao;

import com.knowbot.db.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudyTaskDao {

    public record StudyTask(
            int id,
            String title,
            String subject,
            String priority,
            String dueDate,
            boolean completed,
            String createdAt) {}

    public void addTask(String title, String subject,
                        String priority, String dueDate)
            throws SQLException {
        String sql = """
                INSERT INTO study_tasks
                (title, subject, priority, due_date)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, title);
            p.setString(2, subject);
            p.setString(3, priority);
            p.setString(4, dueDate);
            p.executeUpdate();
        }
    }

    public List<StudyTask> findAll() throws SQLException {
        List<StudyTask> tasks = new ArrayList<>();
        String sql = """
                SELECT id, title, subject, priority, due_date,
                       completed, created_at
                FROM study_tasks
                ORDER BY completed ASC, due_date ASC, id DESC
                """;
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) {
                tasks.add(new StudyTask(
                        r.getInt("id"),
                        r.getString("title"),
                        r.getString("subject"),
                        r.getString("priority"),
                        r.getString("due_date"),
                        r.getInt("completed") == 1,
                        r.getString("created_at")));
            }
        }
        return tasks;
    }

    public void setCompleted(int id, boolean completed)
            throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE study_tasks SET completed = ? WHERE id = ?")) {
            p.setInt(1, completed ? 1 : 0);
            p.setInt(2, id);
            p.executeUpdate();
        }
    }

    public void deleteTask(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(
                     "DELETE FROM study_tasks WHERE id = ?")) {
            p.setInt(1, id);
            p.executeUpdate();
        }
    }
}
