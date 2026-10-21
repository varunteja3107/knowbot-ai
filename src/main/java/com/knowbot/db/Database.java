package com.knowbot.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {

    private Database() {}

    private static Path getDatabasePath() {
        String customPath = System.getProperty("knowbot.db.path");

        if (customPath != null && !customPath.isBlank()) {
            return Path.of(customPath);
        }

        return Path.of(
                System.getProperty("user.home"),
                ".knowbot-ai",
                "knowbot.db"
        );
    }

    public static Connection getConnection() throws SQLException {
        Path path = getDatabasePath();

        try {
            Files.createDirectories(path.getParent());
        } catch (Exception e) {
            throw new SQLException("Cannot create database directory.", e);
        }

        return DriverManager.getConnection(
                "jdbc:sqlite:" + path.toAbsolutePath()
        );
    }

    public static void initialize() throws SQLException {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute("""
                CREATE TABLE IF NOT EXISTS notes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    content TEXT NOT NULL,
                    created_at TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime')),
                    category TEXT NOT NULL DEFAULT 'General',
                    favorite INTEGER NOT NULL DEFAULT 0,
                    updated_at TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime'))
                )
                """);

            addColumnIfMissing(
                    connection, "category",
                    "TEXT NOT NULL DEFAULT 'General'"
            );

            addColumnIfMissing(
                    connection, "favorite",
                    "INTEGER NOT NULL DEFAULT 0"
            );

            addColumnIfMissing(
                    connection, "updated_at",
                    "TEXT NOT NULL DEFAULT ''"
            );

            statement.execute("""
                CREATE TABLE IF NOT EXISTS documents (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    filename TEXT NOT NULL,
                    file_path TEXT NOT NULL,
                    content TEXT NOT NULL,
                    imported_at TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime'))
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS quiz_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    score INTEGER NOT NULL,
                    total_questions INTEGER NOT NULL,
                    completed_at TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime'))
                )
                """);

            statement.execute("""
                CREATE TABLE IF NOT EXISTS study_tasks (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    subject TEXT NOT NULL DEFAULT 'General',
                    priority TEXT NOT NULL DEFAULT 'Medium',
                    due_date TEXT NOT NULL,
                    completed INTEGER NOT NULL DEFAULT 0,
                    created_at TEXT NOT NULL DEFAULT
                        (datetime('now', 'localtime'))
                )
                """);
        }
    }

    private static void addColumnIfMissing(
            Connection connection, String column, String definition)
            throws SQLException {

        boolean exists = false;

        try (Statement statement = connection.createStatement();
             ResultSet result = statement.executeQuery(
                     "PRAGMA table_info(notes)")) {

            while (result.next()) {
                if (column.equalsIgnoreCase(result.getString("name"))) {
                    exists = true;
                    break;
                }
            }
        }

        if (!exists) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "ALTER TABLE notes ADD COLUMN "
                                + column + " " + definition
                );
            }
        }
    }
}
