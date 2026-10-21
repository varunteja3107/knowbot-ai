package com.knowbot.dao;

import com.knowbot.db.Database;
import com.knowbot.model.Note;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteDao {

    public void saveNote(String title, String content)
            throws SQLException {
        saveNote(title, content, "General", false);
    }

    public void saveNote(String title, String content,
                         String category, boolean favorite)
            throws SQLException {

        String sql = """
                INSERT INTO notes (title, content, category, favorite)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, category);
            statement.setInt(4, favorite ? 1 : 0);
            statement.executeUpdate();
        }
    }

    public void updateNote(int id, String title, String content,
                           String category, boolean favorite)
            throws SQLException {

        String sql = """
                UPDATE notes
                SET title = ?, content = ?, category = ?, favorite = ?,
                    updated_at = datetime('now', 'localtime')
                WHERE id = ?
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title);
            statement.setString(2, content);
            statement.setString(3, category);
            statement.setInt(4, favorite ? 1 : 0);
            statement.setInt(5, id);

            if (statement.executeUpdate() == 0) {
                throw new SQLException("The selected note no longer exists.");
            }
        }
    }

    public void deleteNote(int id) throws SQLException {
        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "DELETE FROM notes WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    public List<Note> findAllNotes() throws SQLException {
        return searchNotes("");
    }

    public List<Note> searchNotes(String query) throws SQLException {
        String sql = """
                SELECT id, title, content, created_at, category, favorite
                FROM notes
                WHERE title LIKE ? OR content LIKE ? OR category LIKE ?
                ORDER BY favorite DESC, id DESC
                """;

        List<Note> notes = new ArrayList<>();
        String pattern = "%" + query + "%";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            statement.setString(3, pattern);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    notes.add(new Note(
                            result.getInt("id"),
                            result.getString("title"),
                            result.getString("content"),
                            result.getString("created_at"),
                            result.getString("category"),
                            result.getInt("favorite") == 1
                    ));
                }
            }
        }

        return notes;
    }
}
