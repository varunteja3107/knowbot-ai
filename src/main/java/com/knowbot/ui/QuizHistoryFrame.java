package com.knowbot.ui;

import com.knowbot.dao.QuizHistoryDao;
import com.knowbot.dao.QuizHistoryDao.Attempt;
import com.knowbot.dao.QuizHistoryDao.Stats;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class QuizHistoryFrame extends JFrame {

    private final QuizHistoryDao dao = new QuizHistoryDao();
    private final JLabel attemptsLabel = new JLabel("Attempts: 0");
    private final JLabel averageLabel = new JLabel("Average: 0%");
    private final JLabel bestLabel = new JLabel("Best: 0%");
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"Attempt", "Score", "Percentage", "Completed"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final Color green = new Color(39, 115, 79);

    public QuizHistoryFrame() {
        setTitle("KnowBot AI | Quiz History");
        setSize(780, 520);
        setMinimumSize(new Dimension(650, 420));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBorder(new EmptyBorder(22, 22, 22, 22));
        root.setBackground(new Color(245, 247, 244));
        setContentPane(root);

        JLabel title = new JLabel("Quiz Performance");
        title.setFont(new Font("SansSerif", Font.BOLD, 26));

        JLabel subtitle = new JLabel(
                "Your saved quiz attempts and progress at a glance.");
        subtitle.setForeground(new Color(90, 100, 93));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(subtitle);

        JPanel stats = new JPanel(new GridLayout(1, 3, 12, 0));
        stats.setOpaque(false);
        styleStat(attemptsLabel);
        styleStat(averageLabel);
        styleStat(bestLabel);
        stats.add(attemptsLabel);
        stats.add(averageLabel);
        stats.add(bestLabel);

        JPanel top = new JPanel(new BorderLayout(0, 18));
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(stats, BorderLayout.SOUTH);
        root.add(top, BorderLayout.NORTH);

        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        root.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = new JButton("Refresh");
        refresh.setBackground(green);
        refresh.setForeground(Color.WHITE);
        refresh.setOpaque(true);
        refresh.setBorderPainted(false);
        refresh.addActionListener(e -> loadHistory());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setOpaque(false);
        bottom.add(refresh);
        root.add(bottom, BorderLayout.SOUTH);

        loadHistory();
    }

    private void styleStat(JLabel label) {
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setForeground(green);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 225, 216)),
                new EmptyBorder(14, 12, 14, 12)));
        label.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void loadHistory() {
        try {
            Stats stats = dao.getStats();
            attemptsLabel.setText("Attempts: " + stats.attempts());
            averageLabel.setText(
                    String.format("Average: %.1f%%", stats.average()));
            bestLabel.setText(
                    String.format("Best: %.1f%%", stats.best()));

            model.setRowCount(0);
            List<Attempt> attempts = dao.findAll();

            for (Attempt attempt : attempts) {
                model.addRow(new Object[]{
                        attempt.id(),
                        attempt.score() + " / " + attempt.totalQuestions(),
                        String.format("%.1f%%", attempt.percentage()),
                        attempt.completedAt()
                });
            }

            if (attempts.isEmpty()) {
                model.addRow(new Object[]{
                        "-", "No attempts yet", "-", "Complete a study quiz"
                });
            }
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load quiz history:\n" + exception.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
