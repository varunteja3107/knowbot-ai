package com.knowbot.dao;

import com.knowbot.db.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DashboardAnalyticsDao {

    public record TaskStats(
            int total,
            int completed,
            int pending,
            int overdue) {

        public double completionPercentage() {
            return total == 0 ? 0.0 : completed * 100.0 / total;
        }
    }

    public record QuizStats(
            int attempts,
            double averagePercentage,
            double bestPercentage) {}

    public record QuizAttempt(
            int score,
            int totalQuestions,
            String completedAt) {

        public double percentage() {
            return totalQuestions <= 0
                    ? 0.0 : score * 100.0 / totalQuestions;
        }
    }

    public record StudyStreak(
            int currentDays,
            int longestDays,
            int activeDays) {}

    public TaskStats getTaskStats() throws SQLException {
        String sql = """
                SELECT
                    COUNT(*) AS total,
                    COALESCE(SUM(
                        CASE WHEN completed = 1 THEN 1 ELSE 0 END
                    ), 0) AS completed,
                    COALESCE(SUM(
                        CASE WHEN completed = 0 THEN 1 ELSE 0 END
                    ), 0) AS pending,
                    COALESCE(SUM(
                        CASE
                            WHEN completed = 0
                             AND date(due_date) < date('now', 'localtime')
                            THEN 1 ELSE 0
                        END
                    ), 0) AS overdue
                FROM study_tasks
                """;

        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            if (r.next()) {
                return new TaskStats(
                        r.getInt("total"),
                        r.getInt("completed"),
                        r.getInt("pending"),
                        r.getInt("overdue"));
            }
        }

        return new TaskStats(0, 0, 0, 0);
    }

    public QuizStats getQuizStats() throws SQLException {
        String sql = """
                SELECT
                    COUNT(*) AS attempts,
                    COALESCE(AVG(
                        CASE WHEN total_questions > 0
                             THEN score * 100.0 / total_questions
                             ELSE 0 END
                    ), 0) AS average_percentage,
                    COALESCE(MAX(
                        CASE WHEN total_questions > 0
                             THEN score * 100.0 / total_questions
                             ELSE 0 END
                    ), 0) AS best_percentage
                FROM quiz_history
                """;

        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            if (r.next()) {
                return new QuizStats(
                        r.getInt("attempts"),
                        r.getDouble("average_percentage"),
                        r.getDouble("best_percentage"));
            }
        }

        return new QuizStats(0, 0, 0);
    }

    public List<QuizAttempt> getRecentQuizzes(int limit)
            throws SQLException {

        if (limit < 1) {
            return List.of();
        }

        String sql = """
                SELECT score, total_questions, completed_at
                FROM quiz_history
                ORDER BY datetime(completed_at) DESC, id DESC
                LIMIT ?
                """;

        List<QuizAttempt> attempts = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, limit);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    attempts.add(new QuizAttempt(
                            r.getInt("score"),
                            r.getInt("total_questions"),
                            r.getString("completed_at")));
                }
            }
        }

        return attempts;
    }

    public StudyStreak getStudyStreak() throws SQLException {
        TreeSet<LocalDate> dates = new TreeSet<>();

        String sql = """
                SELECT date(completed_at) AS activity_date
                FROM quiz_history
                WHERE date(completed_at) IS NOT NULL
                """;

        try (Connection c = Database.getConnection();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {

            while (r.next()) {
                String value = r.getString("activity_date");
                if (value != null) {
                    dates.add(LocalDate.parse(value));
                }
            }
        }

        if (dates.isEmpty()) {
            return new StudyStreak(0, 0, 0);
        }

        int longest = 0;
        int run = 0;
        LocalDate previous = null;

        for (LocalDate date : dates) {
            if (previous != null && date.equals(previous.plusDays(1))) {
                run++;
            } else {
                run = 1;
            }

            longest = Math.max(longest, run);
            previous = date;
        }

        LocalDate today = LocalDate.now();
        int current = 0;
        LocalDate cursor = dates.contains(today) ? today : today.minusDays(1);

        while (dates.contains(cursor)) {
            current++;
            cursor = cursor.minusDays(1);
        }

        return new StudyStreak(current, longest, dates.size());
    }
}
