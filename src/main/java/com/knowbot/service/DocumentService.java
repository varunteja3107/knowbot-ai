package com.knowbot.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class DocumentService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;

    public String extractText(Path path) throws IOException {
        if (path == null || !Files.isRegularFile(path)) {
            throw new IOException("Please select an existing file.");
        }

        if (Files.size(path) > MAX_FILE_SIZE) {
            throw new IOException("Files must be 20 MB or smaller.");
        }

        String filename = path.getFileName().toString()
                .toLowerCase(Locale.ROOT);

        if (filename.endsWith(".txt")) {
            return Files.readString(path, StandardCharsets.UTF_8);
        }

        if (filename.endsWith(".pdf")) {
            try (PDDocument document = Loader.loadPDF(path.toFile())) {
                return new PDFTextStripper().getText(document);
            } catch (IOException exception) {
                throw new IOException(
                        "Could not read this PDF. It may be damaged or encrypted.",
                        exception);
            }
        }

        throw new IOException("Only PDF and TXT files are supported.");
    }
}
