package com.knowbot.dao;

import com.knowbot.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizHistoryDao {

    public record Attempt(
            int id, int score, int totalQuestions, String completedAt) {
        public double percentage() {
            return totalQuestions == 0 ? 0
                    : score * 100.0 / totalQuestions;
        }
    }

    public record Stats(int attempts, double average, double best) {}

    public void saveAttempt(int score, int totalQuestions)
            throws SQLException {
        if (totalQuestions <= 0 || score < 0 || score > totalQuestions) {
            throw new IllegalArgumentException("Invalid quiz result.");
        }

        String sql = """
                INSERT INTO quiz_history (score, total_questions)
                VALUES (?, ?)
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, score);
            statement.setInt(2, totalQuestions);
            statement.executeUpdate();
        }
    }

    public List<Attempt> findAll() throws SQLException {
        List<Attempt> attempts = new ArrayList<>();

        String sql = """
                SELECT id, score, total_questions, completed_at
                FROM quiz_history
                ORDER BY id DESC
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                attempts.add(new Attempt(
                        result.getInt("id"),
                        result.getInt("score"),
                        result.getInt("total_questions"),
                        result.getString("completed_at")
                ));
            }
        }
        return attempts;
    }

    public Stats getStats() throws SQLException {
        String sql = """
                SELECT COUNT(*) AS attempts,
                       COALESCE(AVG(score * 100.0 / total_questions), 0)
                           AS average_score,
                       COALESCE(MAX(score * 100.0 / total_questions), 0)
                           AS best_score
                FROM quiz_history
                WHERE total_questions > 0
                """;

        try (Connection connection = Database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return new Stats(
                    result.getInt("attempts"),
                    result.getDouble("average_score"),
                    result.getDouble("best_score")
            );
        }
    }
}
