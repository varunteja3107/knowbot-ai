package com.knowbot.dao;

import com.knowbot.db.Database;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentDaoTest {

    @TempDir
    Path tempDir;

    @Test
    void savesSearchesAndDeletesDocument() throws Exception {
        String previousPath = System.getProperty("knowbot.db.path");
        Path testDatabase = tempDir.resolve("document-test.db");

        try {
            System.setProperty("knowbot.db.path", testDatabase.toString());

            Database.initialize();

            DocumentDao dao = new DocumentDao();

            int id = dao.saveDocument(
                    "java-basics.txt",
                    "/test/java-basics.txt",
                    "Java supports object-oriented programming and JDBC."
            );

            assertTrue(id > 0);

            List<DocumentDao.Document> all = dao.findAllDocuments();
            assertEquals(1, all.size());
            assertEquals("java-basics.txt", all.get(0).filename());

            List<DocumentDao.Document> found =
                    dao.searchDocuments("JDBC");

            assertEquals(1, found.size());
            assertTrue(found.get(0).content().contains("JDBC"));

            assertTrue(dao.deleteDocument(id));
            assertTrue(dao.findAllDocuments().isEmpty());

            System.out.println(
                    "Document storage test passed: save, search, delete."
            );
        } finally {
            if (previousPath == null) {
                System.clearProperty("knowbot.db.path");
            } else {
                System.setProperty("knowbot.db.path", previousPath);
            }
        }
    }
}
