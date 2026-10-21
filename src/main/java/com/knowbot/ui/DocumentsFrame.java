package com.knowbot.ui;

import com.knowbot.dao.DocumentDao;
import com.knowbot.service.DocumentService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class DocumentsFrame extends JFrame {

    private final DocumentDao dao = new DocumentDao();
    private final DocumentService service = new DocumentService();

    private final DefaultListModel<DocumentDao.Document> listModel =
            new DefaultListModel<>();
    private final JList<DocumentDao.Document> documentList =
            new JList<>(listModel);
    private final JTextField searchField = new JTextField();
    private final JTextArea previewArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Ready");

    private final Color background = new Color(245, 247, 244);
    private final Color green = new Color(34, 110, 70);
    private final Color dark = new Color(36, 44, 39);
    private final Color border = new Color(205, 218, 207);

    public DocumentsFrame() {
        setTitle("KnowBot AI | Documents");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1050, 700);
        setMinimumSize(new Dimension(800, 550));
        setLocationRelativeTo(null);
        getContentPane().setBackground(background);
        setLayout(new BorderLayout(16, 16));

        JPanel main = new JPanel(new BorderLayout(12, 16));
        main.setBackground(background);
        main.setBorder(new EmptyBorder(24, 24, 18, 24));

        JLabel heading = new JLabel("My Documents");
        heading.setFont(new Font("SansSerif", Font.BOLD, 27));
        heading.setForeground(dark);

        JLabel subtitle = new JLabel(
                "Import, search, preview and manage your PDF and text files."
        );
        subtitle.setForeground(new Color(105, 115, 107));

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.add(heading);
        titlePanel.add(Box.createVerticalStrut(7));
        titlePanel.add(subtitle);

        JButton importButton = new JButton("Import PDF / TXT");
        importButton.setForeground(new java.awt.Color(255, 255, 255));
        importButton.setBackground(new java.awt.Color(39, 115, 79));
        importButton.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 14));
        importButton.setOpaque(true);
        importButton.setContentAreaFilled(true);
        importButton.setBorderPainted(false);
        importButton.setFocusPainted(false);
    
        importButton.setBackground(green);
        importButton.setForeground(Color.WHITE);
        importButton.setFocusPainted(false);
        importButton.addActionListener(event -> importDocument());

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(titlePanel, BorderLayout.CENTER);
        top.add(importButton, BorderLayout.EAST);

        searchField.setToolTipText("Search document names and contents");
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border),
                new EmptyBorder(8, 8, 8, 8)
        ));

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(event -> refreshDocuments());

        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(event -> {
            searchField.setText("");
            refreshDocuments();
        });

        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setOpaque(false);
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        searchPanel.add(showAllButton, BorderLayout.WEST);

        JPanel left = new JPanel(new BorderLayout(0, 10));
        left.setBackground(Color.WHITE);
        left.setBorder(new EmptyBorder(14, 14, 14, 14));
        left.setPreferredSize(new Dimension(330, 0));

        JLabel listHeading = new JLabel("IMPORTED FILES");
        listHeading.setForeground(green);
        listHeading.setFont(new Font("SansSerif", Font.BOLD, 13));

        JPanel listTop = new JPanel(new BorderLayout(0, 10));
        listTop.setOpaque(false);
        listTop.add(listHeading, BorderLayout.NORTH);
        listTop.add(searchPanel, BorderLayout.CENTER);

        documentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        documentList.setFont(new Font("SansSerif", Font.PLAIN, 13));
        documentList.setFixedCellHeight(42);
        documentList.setSelectionBackground(new Color(220, 238, 224));
        documentList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean selected, boolean focused) {

                super.getListCellRendererComponent(
                        list, value, index, selected, focused
                );

                if (value instanceof DocumentDao.Document document) {
                    setText("  " + document.filename());
                    setToolTipText(document.filePath());
                }
                return this;
            }
        });

        documentList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showSelectedDocument();
            }
        });

        left.add(listTop, BorderLayout.NORTH);
        left.add(new JScrollPane(documentList), BorderLayout.CENTER);

        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        previewArea.setForeground(dark);
        previewArea.setMargin(new Insets(14, 14, 14, 14));
        previewArea.setText("Import a PDF or TXT file to preview its extracted text.");

        JScrollPane previewScroll = new JScrollPane(previewArea);
        previewScroll.setBorder(BorderFactory.createLineBorder(border));

        JLabel previewHeading = new JLabel("DOCUMENT PREVIEW");
        previewHeading.setForeground(green);
        previewHeading.setFont(new Font("SansSerif", Font.BOLD, 13));

        JButton deleteButton = new JButton("Delete Document");
        deleteButton.setForeground(new Color(160, 45, 45));
        deleteButton.addActionListener(event -> deleteDocument());

        JPanel previewTop = new JPanel(new BorderLayout());
        previewTop.setOpaque(false);
        previewTop.add(previewHeading, BorderLayout.WEST);
        previewTop.add(deleteButton, BorderLayout.EAST);

        JPanel right = new JPanel(new BorderLayout(0, 12));
        right.setBackground(Color.WHITE);
        right.setBorder(new EmptyBorder(14, 14, 14, 14));
        right.add(previewTop, BorderLayout.NORTH);
        right.add(previewScroll, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, left, right
        );
        split.setResizeWeight(0.36);
        split.setDividerLocation(350);
        split.setBorder(null);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(split, BorderLayout.CENTER);

        statusLabel.setForeground(new Color(90, 100, 92));

        main.add(center, BorderLayout.CENTER);
        main.add(statusLabel, BorderLayout.SOUTH);
        add(main, BorderLayout.CENTER);

        searchField.addActionListener(event -> refreshDocuments());

        refreshDocuments();
    }

    private void importDocument() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose a PDF or TXT document");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "PDF and text files", "pdf", "txt"
        ));

        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path path = chooser.getSelectedFile().toPath();

        try {
            String content = service.extractText(path);

            if (content == null || content.isBlank()) {
                JOptionPane.showMessageDialog(
                        this,
                        "No text was extracted. Scanned PDFs may require OCR.",
                        "No Text Found",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            dao.saveDocument(
                    path.getFileName().toString(),
                    path.toAbsolutePath().toString(),
                    content
            );

            statusLabel.setText("Imported: " + path.getFileName());
            refreshDocuments();

        } catch (IOException | SQLException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not import the document:\n" + ex.getMessage(),
                    "Import Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshDocuments() {
        try {
            List<DocumentDao.Document> documents;

            String query = searchField.getText().trim();
            if (query.isEmpty()) {
                documents = dao.findAllDocuments();
            } else {
                documents = dao.searchDocuments(query);
            }

            listModel.clear();
            for (DocumentDao.Document document : documents) {
                listModel.addElement(document);
            }

            if (!documents.isEmpty()) {
                documentList.setSelectedIndex(0);
                statusLabel.setText(documents.size() + " document(s) found.");
            } else {
                previewArea.setText("No matching documents. Import a PDF or TXT file.");
                statusLabel.setText("No documents found.");
            }

        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void showSelectedDocument() {
        DocumentDao.Document document = documentList.getSelectedValue();

        if (document == null) {
            return;
        }

        previewArea.setText(document.content());
        previewArea.setCaretPosition(0);
        statusLabel.setText("Selected: " + document.filename());
    }

    private void deleteDocument() {
        DocumentDao.Document document = documentList.getSelectedValue();

        if (document == null) {
            JOptionPane.showMessageDialog(
                    this,
                    "Select a document first.",
                    "No Document Selected",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                this,
                "Remove this document from KnowBot AI?\n"
                        + "The original file on your Mac will not be deleted.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            dao.deleteDocument(document.id());
            previewArea.setText("");
            refreshDocuments();
            statusLabel.setText("Document removed from KnowBot AI.");
        } catch (SQLException ex) {
            showDatabaseError(ex);
        }
    }

    private void showDatabaseError(SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this,
                "Database error:\n" + ex.getMessage(),
                "KnowBot AI",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
