package com.knowbot.ui;

import com.knowbot.dao.DocumentDao;
import com.knowbot.dao.QuizHistoryDao;
import com.knowbot.service.OllamaService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudyQuizFrame extends JFrame {

    private final DocumentDao documentDao = new DocumentDao();
    private final OllamaService ollama = new OllamaService();

    private final JLabel status = new JLabel("Generate a quiz from your imported documents.");
    private final JLabel questionLabel = new JLabel("Your quiz will appear here.");
    private final JLabel progressLabel = new JLabel("No quiz generated");
    private final JTextArea explanationArea = new JTextArea(4, 35);
    private final JButton generateButton = new JButton("Generate Quiz");
    private final JButton checkButton = new JButton("Check Answer");
    private final JButton nextButton = new JButton("Next Question");
    private final JButton restartButton = new JButton("Restart Quiz");

    private final ButtonGroup answerGroup = new ButtonGroup();
    private final JRadioButton[] options = new JRadioButton[4];

    private List<QuizQuestion> questions = new ArrayList<>();
    private int currentIndex = 0;
    private int score = 0;
    private boolean answered = false;

    private final Color green = new Color(39, 115, 79);
    private final Color background = new Color(245, 247, 245);

    public StudyQuizFrame() {
        setTitle("KnowBot AI | Study Quizzes");
        setSize(760, 620);
        setMinimumSize(new Dimension(650, 520));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(15, 15));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(24, 24, 24, 24));
        setContentPane(root);

        JLabel heading = new JLabel("Study Quizzes");
        heading.setFont(new Font("SansSerif", Font.BOLD, 27));

        JLabel subtitle = new JLabel(
                "Turn your imported PDFs and text files into practice questions."
        );
        subtitle.setForeground(new Color(95, 105, 98));

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.add(heading);
        header.add(Box.createVerticalStrut(8));
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        JPanel quizPanel = new JPanel();
        quizPanel.setLayout(new BoxLayout(quizPanel, BoxLayout.Y_AXIS));
        quizPanel.setBackground(Color.WHITE);
        quizPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 227, 221)),
                new EmptyBorder(22, 22, 22, 22)
        ));

        progressLabel.setForeground(green);
        progressLabel.setFont(new Font("SansSerif", Font.BOLD, 13));

        questionLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        questionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        quizPanel.add(progressLabel);
        quizPanel.add(Box.createVerticalStrut(18));
        quizPanel.add(questionLabel);
        quizPanel.add(Box.createVerticalStrut(20));

        for (int i = 0; i < options.length; i++) {
            options[i] = new JRadioButton();
            options[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            options[i].setBackground(Color.WHITE);
            options[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            options[i].setFocusPainted(false);
            answerGroup.add(options[i]);
            quizPanel.add(options[i]);
            quizPanel.add(Box.createVerticalStrut(12));
        }

        explanationArea.setEditable(false);
        explanationArea.setLineWrap(true);
        explanationArea.setWrapStyleWord(true);
        explanationArea.setFont(new Font("SansSerif", Font.PLAIN, 13));
        explanationArea.setBackground(new Color(244, 248, 244));
        explanationArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        explanationArea.setText("Answer explanations will appear here.");

        JScrollPane explanationScroll = new JScrollPane(explanationArea);
        explanationScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        explanationScroll.setPreferredSize(new Dimension(650, 90));
        explanationScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        quizPanel.add(Box.createVerticalStrut(8));
        quizPanel.add(explanationScroll);

        root.add(quizPanel, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout(10, 10));
        footer.setOpaque(false);

        status.setForeground(new Color(80, 90, 82));
        footer.add(status, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.setOpaque(false);

        stylePrimary(generateButton);
        stylePrimary(checkButton);
        styleSecondary(nextButton);
        styleSecondary(restartButton);

        buttons.add(generateButton);
        buttons.add(checkButton);
        buttons.add(nextButton);
        buttons.add(restartButton);

        footer.add(buttons, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        generateButton.addActionListener(e -> generateQuiz());
        checkButton.addActionListener(e -> checkAnswer());
        nextButton.addActionListener(e -> nextQuestion());
        restartButton.addActionListener(e -> restartQuiz());

        setQuizControls(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(false);
        restartButton.setEnabled(false);
    }

    private void stylePrimary(JButton button) {
        button.setBackground(green);
        button.setForeground(Color.WHITE);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void styleSecondary(JButton button) {
        button.setBackground(new Color(231, 238, 232));
        button.setForeground(new Color(35, 75, 52));
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void generateQuiz() {
        generateButton.setEnabled(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(false);
        status.setText("Reading documents and generating questions...");
        explanationArea.setText("");

        new SwingWorker<List<QuizQuestion>, Void>() {
            @Override
            protected List<QuizQuestion> doInBackground() throws Exception {
                List<DocumentDao.Document> documents =
                        documentDao.findAllDocuments();

                if (documents.isEmpty()) {
                    throw new IllegalStateException(
                            "No documents found. Import a PDF or TXT file first."
                    );
                }

                StringBuilder source = new StringBuilder();
                int remaining = 14000;

                for (DocumentDao.Document doc : documents) {
                    if (remaining <= 0) {
                        break;
                    }

                    String content = doc.content();
                    if (content == null || content.isBlank()) {
                        continue;
                    }

                    int length = Math.min(content.length(), remaining);
                    source.append("\nDOCUMENT: ")
                            .append(doc.filename())
                            .append("\n")
                            .append(content, 0, length)
                            .append("\n");

                    remaining -= length;
                }

                if (source.toString().isBlank()) {
                    throw new IllegalStateException(
                            "Your imported documents contain no readable text."
                    );
                }

                String prompt = """
                        Create exactly 5 multiple-choice study questions using
                        ONLY the source material below. Do not invent facts.

                        Output each question in this exact format:
                        Q1: question text
                        A: first option
                        B: second option
                        C: third option
                        D: fourth option
                        ANSWER: A
                        EXPLANATION: brief explanation grounded in the source

                        Continue with Q2, Q3, Q4, and Q5.
                        ANSWER must be exactly one letter: A, B, C, or D.
                        Keep each question and option on one line.
                        Do not use Markdown code fences.

                        SOURCE MATERIAL:
                        """ + source;

                String response = ollama.chat(prompt);
                List<QuizQuestion> parsed = parseQuestions(response);

                if (parsed.isEmpty()) {
                    throw new IllegalStateException(
                            "The AI response could not be read as a quiz. "
                            + "Please click Generate Quiz again."
                    );
                }

                return parsed;
            }

            @Override
            protected void done() {
                generateButton.setEnabled(true);

                try {
                    questions = get();
                    currentIndex = 0;
                    score = 0;
                    showQuestion();
                    checkButton.setEnabled(true);
                    nextButton.setEnabled(true);
                    restartButton.setEnabled(true);
                    status.setText("Quiz ready. Choose one answer.");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null
                            ? ex.getCause() : ex;

                    JOptionPane.showMessageDialog(
                            StudyQuizFrame.this,
                            cause.getMessage() == null
                                    ? cause.toString() : cause.getMessage(),
                            "Could not generate quiz",
                            JOptionPane.ERROR_MESSAGE
                    );
                    status.setText("Quiz generation failed.");
                }
            }
        }.execute();
    }

    private List<QuizQuestion> parseQuestions(String response) {
        List<QuizQuestion> result = new ArrayList<>();

        Pattern blockPattern = Pattern.compile(
                "(?ims)^\\s*Q\\s*\\d+\\s*:\\s*(.*?)(?=^\\s*Q\\s*\\d+\\s*:|\\z)"
        );
        Matcher matcher = blockPattern.matcher(response);

        while (matcher.find()) {
            String block = matcher.group(1);
            String question = field(block, "(?im)^\\s*(.+?)\\s*$", null);

            String a = field(block, "(?im)^\\s*A\\s*:\\s*(.+)$", null);
            String b = field(block, "(?im)^\\s*B\\s*:\\s*(.+)$", null);
            String c = field(block, "(?im)^\\s*C\\s*:\\s*(.+)$", null);
            String d = field(block, "(?im)^\\s*D\\s*:\\s*(.+)$", null);
            String answer = field(block,
                    "(?im)^\\s*ANSWER\\s*:\\s*([ABCD])\\b.*$", null);
            String explanation = field(block,
                    "(?im)^\\s*EXPLANATION\\s*:\\s*(.+)$",
                    "No explanation provided.");

            // The first line is the question. Remove option/answer lines.
            String[] lines = block.split("\\R");
            question = "";
            for (String line : lines) {
                if (line.matches("(?i)^\\s*[ABCD]\\s*:.*")
                        || line.matches("(?i)^\\s*ANSWER\\s*:.*")
                        || line.matches("(?i)^\\s*EXPLANATION\\s*:.*")
                        || line.isBlank()) {
                    continue;
                }
                question = line.replaceFirst("(?i)^\\s*QUESTION\\s*:\\s*", "")
                        .trim();
                break;
            }

            if (!question.isBlank() && a != null && b != null
                    && c != null && d != null && answer != null) {
                result.add(new QuizQuestion(
                        question, a.trim(), b.trim(), c.trim(), d.trim(),
                        answer.trim().toUpperCase(),
                        explanation.trim()
                ));
            }
        }

        return result;
    }

    private String field(String text, String regex, String fallback) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? matcher.group(1).trim() : fallback;
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        answered = false;
        QuizQuestion q = questions.get(currentIndex);
        progressLabel.setText(
                "QUESTION " + (currentIndex + 1) + " OF " + questions.size()
        );
        questionLabel.setText("<html><div style='width:560px'>"
                + escapeHtml(q.question) + "</div></html>");

        String[] values = {q.a, q.b, q.c, q.d};
        for (int i = 0; i < options.length; i++) {
            options[i].setText((char) ('A' + i) + ". " + values[i]);
            options[i].setEnabled(true);
            options[i].setSelected(false);
        }

        answerGroup.clearSelection();
        explanationArea.setText("Choose an answer, then click Check Answer.");
        checkButton.setEnabled(true);
        nextButton.setEnabled(false);
        setQuizControls(true);
        status.setText("Score: " + score);
    }

    private void checkAnswer() {
        if (questions.isEmpty() || answered) {
            return;
        }

        int selected = -1;
        for (int i = 0; i < options.length; i++) {
            if (options[i].isSelected()) {
                selected = i;
                break;
            }
        }

        if (selected == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select an answer first.",
                    "Choose an answer", JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        QuizQuestion q = questions.get(currentIndex);
        String selectedLetter = String.valueOf((char) ('A' + selected));
        boolean correct = selectedLetter.equalsIgnoreCase(q.answer);

        if (correct) {
            score++;
        }

        answered = true;
        for (JRadioButton option : options) {
            option.setEnabled(false);
        }

        explanationArea.setText(
                (correct ? "Correct!" : "Not quite. Correct answer: " + q.answer)
                + "\n\n" + q.explanation
        );
        status.setText("Score: " + score + " / " + questions.size());
        checkButton.setEnabled(false);
        nextButton.setEnabled(true);
        nextButton.setText(currentIndex == questions.size() - 1
                ? "View Results" : "Next Question");
    }

    private void nextQuestion() {
        if (!answered) {
            JOptionPane.showMessageDialog(
                    this, "Check your answer before moving on.",
                    "Answer first", JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }
        currentIndex++;
        if (currentIndex >= questions.size()) {
            finishQuiz();
        } else {
            nextButton.setText("Next Question");
            showQuestion();
        }
    }

    private void finishQuiz() {
        try {
            new QuizHistoryDao().saveAttempt(score, questions.size());
        } catch (java.sql.SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Quiz completed, but the result could not be saved:\\n"
                            + exception.getMessage(),
                    "Quiz History",
                    JOptionPane.WARNING_MESSAGE
            );
        }

        setQuizControls(false);
        checkButton.setEnabled(false);
        nextButton.setEnabled(false);
        restartButton.setEnabled(true);
        progressLabel.setText("QUIZ COMPLETE");
        questionLabel.setText("You have completed the quiz!");
        explanationArea.setText(
                "Final score: " + score + " / " + questions.size()
                + "\n\n" + (score == questions.size()
                    ? "Perfect score!"
                    : "Review the explanations and try again.")
        );
        status.setText("Quiz complete.");
        nextButton.setText("Next Question");
    }

    private void restartQuiz() {
        if (questions.isEmpty()) {
            generateQuiz();
            return;
        }
        currentIndex = 0;
        score = 0;
        showQuestion();
    }

    private void setQuizControls(boolean enabled) {
        for (JRadioButton option : options) {
            option.setEnabled(enabled);
        }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static class QuizQuestion {
        final String question, a, b, c, d, answer, explanation;

        QuizQuestion(String question, String a, String b, String c, String d,
                     String answer, String explanation) {
            this.question = question;
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
            this.answer = answer;
            this.explanation = explanation;
        }
    }
}
