package com.knowbot.db;

import com.knowbot.dao.NoteDao;
import com.knowbot.model.Note;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseSmokeTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void shouldSaveAndReloadNote() throws Exception {
        String previousPath = System.getProperty("knowbot.db.path");

        try {
            Path testDatabase =
                    temporaryDirectory.resolve("test.db");

            System.setProperty(
                    "knowbot.db.path",
                    testDatabase.toString()
            );

            Database.initialize();

            NoteDao noteDao = new NoteDao();

            noteDao.saveNote(
                    "Java Basics",
                    "Java supports classes, objects and inheritance."
            );

            List<Note> notes = noteDao.findAllNotes();

            assertEquals(1, notes.size());
            assertEquals("Java Basics", notes.get(0).getTitle());
            assertEquals(
                    "Java supports classes, objects and inheritance.",
                    notes.get(0).getContent()
            );
            assertNotNull(notes.get(0).getCreatedAt());

        } finally {
            if (previousPath == null) {
                System.clearProperty("knowbot.db.path");
            } else {
                System.setProperty("knowbot.db.path", previousPath);
            }
        }
    }
}
