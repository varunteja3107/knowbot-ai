package com.knowbot.web;

import com.knowbot.dao.NoteDao;
import com.knowbot.model.Note;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notes")
@CrossOrigin(origins = {
    "http://localhost:5173",
    "http://localhost:5174",
    "http://localhost:5175"
})
public class NotesController {

    private final NoteDao noteDao = new NoteDao();

    public record NoteRequest(
        @NotBlank(message = "Title is required")
        String title,
        String content,
        String category,
        boolean favorite
    ) {}

    @GetMapping
    public List<Note> getNotes(
            @RequestParam(required = false, defaultValue = "") String q)
            throws SQLException {
        return noteDao.searchNotes(q.trim());
    }

    @PostMapping
    public Map<String, Object> createNote(
            @Valid @RequestBody NoteRequest request)
            throws SQLException {

        String content = request.content() == null
                ? "" : request.content();

        String category = request.category() == null
                || request.category().isBlank()
                ? "General" : request.category().trim();

        noteDao.saveNote(
            request.title().trim(),
            content,
            category,
            request.favorite()
        );

        return Map.of(
            "status", "ok",
            "message", "Note saved successfully"
        );
    }

    @PutMapping("/{id}")
    public Map<String, Object> updateNote(
            @PathVariable int id,
            @Valid @RequestBody NoteRequest request)
            throws SQLException {

        String content = request.content() == null
                ? "" : request.content();

        String category = request.category() == null
                || request.category().isBlank()
                ? "General" : request.category().trim();

        noteDao.updateNote(
            id,
            request.title().trim(),
            content,
            category,
            request.favorite()
        );

        return Map.of(
            "status", "ok",
            "message", "Note updated successfully"
        );
    }

    @DeleteMapping("/{id}")
    public Map<String, Object> deleteNote(@PathVariable int id)
            throws SQLException {

        noteDao.deleteNote(id);

        return Map.of(
            "status", "ok",
            "message", "Note deleted successfully"
        );
    }
}
