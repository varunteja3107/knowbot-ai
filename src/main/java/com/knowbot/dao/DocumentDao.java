package com.knowbot.dao;

import com.knowbot.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentDao {

    public record Document(
            int id,
            String filename,
            String filePath,
            String content,
            String importedAt) {}

    public int saveDocument(
            String filename, String filePath, String content)
            throws SQLException {

        if (filename == null || filename.isBlank()) {
            throw new IllegalArgumentException("Filename is required.");
        }
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path is required.");
        }
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "The document contains no extractable text.");
        }

        String sql = """
                INSERT INTO documents (filename, file_path, content)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, filename);
            statement.setString(2, filePath);
            statement.setString(3, content);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException("Could not save the document.");
    }

    public List<Document> findAllDocuments() throws SQLException {
        String sql = """
                SELECT id, filename, file_path, content, imported_at
                FROM documents
                ORDER BY id DESC
                """;

        List<Document> documents = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                documents.add(readDocument(result));
            }
        }

        return documents;
    }

    public List<Document> searchDocuments(String query)
            throws SQLException {

        if (query == null || query.isBlank()) {
            return findAllDocuments();
        }

        String sql = """
                SELECT id, filename, file_path, content, imported_at
                FROM documents
                WHERE instr(lower(filename), lower(?)) > 0
                   OR instr(lower(content), lower(?)) > 0
                ORDER BY id DESC
                """;

        List<Document> documents = new ArrayList<>();

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, query);
            statement.setString(2, query);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    documents.add(readDocument(result));
                }
            }
        }

        return documents;
    }

    public boolean deleteDocument(int id) throws SQLException {
        String sql = "DELETE FROM documents WHERE id = ?";

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private Document readDocument(ResultSet result) throws SQLException {
        return new Document(
                result.getInt("id"),
                result.getString("filename"),
                result.getString("file_path"),
                result.getString("content"),
                result.getString("imported_at")
        );
    }
}
