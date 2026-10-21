package com.knowbot.ui;

import com.knowbot.dao.DashboardAnalyticsDao;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdvancedDashboardFrame extends JFrame {

    private static final Color BACKGROUND = new Color(242, 244, 241);
    private static final Color CARD = Color.WHITE;
    private static final Color GREEN = new Color(87, 132, 105);
    private static final Color DARK = new Color(42, 54, 46);
    private static final Color MUTED = new Color(112, 121, 114);
    private static final Color LIGHT_GREEN = new Color(225, 237, 226);

    private final DashboardAnalyticsDao dao = new DashboardAnalyticsDao();

    private final JLabel currentStreakValue = new JLabel("0 days");
    private final JLabel longestStreakValue = new JLabel("0 days");
    private final JLabel taskCompletionValue = new JLabel("0%");
    private final JLabel quizAverageValue = new JLabel("0%");
    private final JLabel quizBestValue = new JLabel("0%");
    private final JLabel taskSummary = new JLabel("Loading tasks...");
    private final JLabel quizSummary = new JLabel("Loading quiz results...");

    private final ChartPanel taskChart = new ChartPanel(
            "Task Completion", new String[]{"Completed", "Pending", "Overdue"},
            new double[]{0, 0, 0}, true);

    private final ChartPanel quizChart = new ChartPanel(
            "Recent Quiz Scores", new String[0], new double[0], false);

    private final DefaultTableModel quizTableModel = new DefaultTableModel(
            new Object[]{"Attempt", "Score", "Percentage", "Completed At"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public AdvancedDashboardFrame() {
        setTitle("KnowBot AI | Advanced Dashboard");
        setSize(1100, 760);
        setMinimumSize(new Dimension(850, 620));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(24, 26, 24, 26));

        root.add(buildHeader(), BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setBackground(BACKGROUND);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(buildStats());
        content.add(Box.createVerticalStrut(18));

        JPanel charts = new JPanel(new GridLayout(1, 2, 18, 0));
        charts.setBackground(BACKGROUND);
        charts.add(taskChart);
        charts.add(quizChart);
        charts.setPreferredSize(new Dimension(1000, 285));
        charts.setMaximumSize(new Dimension(Integer.MAX_VALUE, 285));
        content.add(charts);
        content.add(Box.createVerticalStrut(18));
        content.add(buildRecentQuizzes());

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scrollPane, BorderLayout.CENTER);

        setContentPane(root);
        refreshAnalytics();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 8));
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("Your Learning Overview");
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        title.setForeground(DARK);

        JLabel subtitle = new JLabel(
                "Track your consistency, study tasks, and quiz progress.");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        titles.add(title);
        titles.add(Box.createVerticalStrut(5));
        titles.add(subtitle);

        JButton refresh = new JButton("Refresh");
        refresh.setFocusPainted(false);
        refresh.setBackground(GREEN);
        refresh.setForeground(Color.WHITE);
        refresh.setFont(new Font("SansSerif", Font.BOLD, 13));
        refresh.setBorder(new EmptyBorder(10, 18, 10, 18));
        refresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refresh.addActionListener(e -> refreshAnalytics());

        header.add(titles, BorderLayout.CENTER);
        header.add(refresh, BorderLayout.EAST);
        return header;
    }

    private JPanel buildStats() {
        JPanel grid = new JPanel(new GridLayout(1, 5, 12, 0));
        grid.setBackground(BACKGROUND);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 112));
        grid.setPreferredSize(new Dimension(1000, 112));

        grid.add(statCard("Current Streak", currentStreakValue, "Consecutive quiz days"));
        grid.add(statCard("Longest Streak", longestStreakValue, "Personal best"));
        grid.add(statCard("Tasks Completed", taskCompletionValue, "Completion rate"));
        grid.add(statCard("Average Quiz", quizAverageValue, "Across all attempts"));
        grid.add(statCard("Best Quiz", quizBestValue, "Highest percentage"));

        return grid;
    }

    private JPanel statCard(String heading, JLabel value, String detail) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 231, 225)),
                new EmptyBorder(13, 13, 12, 13)));

        JLabel label = new JLabel(heading);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(MUTED);

        value.setFont(new Font("SansSerif", Font.BOLD, 24));
        value.setForeground(GREEN);

        JLabel description = new JLabel(detail);
        description.setFont(new Font("SansSerif", Font.PLAIN, 10));
        description.setForeground(MUTED);

        card.add(label);
        card.add(Box.createVerticalStrut(8));
        card.add(value);
        card.add(Box.createVerticalStrut(4));
        card.add(description);

        return card;
    }

    private JPanel buildRecentQuizzes() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 231, 225)),
                new EmptyBorder(16, 16, 16, 16)));
        panel.setPreferredSize(new Dimension(1000, 220));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 260));

        JLabel heading = new JLabel("Recent Quiz Attempts");
        heading.setFont(new Font("SansSerif", Font.BOLD, 16));
        heading.setForeground(DARK);

        JTable table = new JTable(quizTableModel);
        table.setRowHeight(27);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(234, 237, 233));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(LIGHT_GREEN);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JPanel summary = new JPanel(new BorderLayout());
        summary.setOpaque(false);
        taskSummary.setFont(new Font("SansSerif", Font.PLAIN, 12));
        taskSummary.setForeground(MUTED);
        quizSummary.setFont(new Font("SansSerif", Font.PLAIN, 12));
        quizSummary.setForeground(MUTED);

        summary.add(taskSummary, BorderLayout.WEST);
        summary.add(quizSummary, BorderLayout.EAST);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(summary, BorderLayout.SOUTH);

        return panel;
    }

    private void refreshAnalytics() {
        try {
            DashboardAnalyticsDao.TaskStats tasks = dao.getTaskStats();
            DashboardAnalyticsDao.QuizStats quizzes = dao.getQuizStats();
            DashboardAnalyticsDao.StudyStreak streak = dao.getStudyStreak();

            currentStreakValue.setText(streak.currentDays() + " days");
            longestStreakValue.setText(streak.longestDays() + " days");
            taskCompletionValue.setText(tasks.completionPercentage() + "%");
            quizAverageValue.setText(String.format("%.1f%%", quizzes.averagePercentage()));
            quizBestValue.setText(String.format("%.1f%%", quizzes.bestPercentage()));

            taskSummary.setText(
                    "Tasks: " + tasks.completed() + " completed / " +
                    tasks.pending() + " pending / " + tasks.overdue() + " overdue");

            quizSummary.setText("Quiz attempts: " + quizzes.attempts());

            taskChart.setData(
                    new String[]{"Completed", "Pending", "Overdue"},
                    new double[]{tasks.completed(), tasks.pending(), tasks.overdue()});

            List<DashboardAnalyticsDao.QuizAttempt> attempts =
                    dao.getRecentQuizzes(5);

            String[] labels = new String[attempts.size()];
            double[] values = new double[attempts.size()];

            quizTableModel.setRowCount(0);

            for (int i = 0; i < attempts.size(); i++) {
                DashboardAnalyticsDao.QuizAttempt attempt = attempts.get(i);
                labels[i] = "Quiz " + (attempts.size() - i);
                values[i] = attempt.percentage();

                quizTableModel.addRow(new Object[]{
                        i + 1,
                        attempt.score() + " / " + attempt.totalQuestions(),
                        String.format("%.1f%%", attempt.percentage()),
                        attempt.completedAt()
                });
            }

            quizChart.setData(labels, values);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not load dashboard analytics.\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ChartPanel extends JPanel {
        private String heading;
        private String[] labels;
        private double[] values;
        private final boolean taskMode;

        ChartPanel(String heading, String[] labels, double[] values, boolean taskMode) {
            this.heading = heading;
            this.labels = labels;
            this.values = values;
            this.taskMode = taskMode;
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 231, 225)),
                    new EmptyBorder(12, 12, 12, 12)));
            setPreferredSize(new Dimension(400, 270));
        }

        void setData(String[] labels, double[] values) {
            this.labels = labels;
            this.values = values;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            g.setColor(DARK);
            g.setFont(new Font("SansSerif", Font.BOLD, 16));
            g.drawString(heading, 16, 27);

            int left = 35;
            int right = getWidth() - 18;
            int top = 53;
            int bottom = getHeight() - 48;
            int chartWidth = Math.max(1, right - left);
            int chartHeight = Math.max(1, bottom - top);

            g.setFont(new Font("SansSerif", Font.PLAIN, 10));

            if (values.length == 0) {
                g.setColor(MUTED);
                g.drawString("No data yet. Complete a task or take a quiz.",
                        left, top + 40);
                g.dispose();
                return;
            }

            double max = taskMode ? 0 : 100;
            for (double value : values) {
                max = Math.max(max, value);
            }
            if (max <= 0) {
                max = 1;
            }

            for (int i = 0; i <= 4; i++) {
                int y = bottom - (chartHeight * i / 4);
                g.setColor(new Color(235, 238, 234));
                g.drawLine(left, y, right, y);
                g.setColor(MUTED);
                g.drawString(String.format("%.0f", max * i / 4),
                        5, y + 4);
            }

            int count = values.length;
            int slot = Math.max(1, chartWidth / count);
            int barWidth = Math.max(12, Math.min(52, slot / 2));

            for (int i = 0; i < count; i++) {
                int x = left + i * slot + (slot - barWidth) / 2;
                int barHeight = (int) Math.round(
                        (values[i] / max) * chartHeight);
                int y = bottom - barHeight;

                g.setColor(taskMode
                        ? (i == 0 ? GREEN : i == 1
                            ? new Color(190, 198, 185)
                            : new Color(195, 116, 100))
                        : GREEN);

                g.fillRoundRect(x, y, barWidth, Math.max(0, barHeight),
                        8, 8);

                g.setColor(DARK);
                g.drawString(String.format("%.0f", values[i]),
                        x + Math.max(0, barWidth / 2 - 4), Math.max(top + 10, y - 5));

                String label = labels[i];
                FontMetrics metrics = g.getFontMetrics();
                int labelX = x + (barWidth - metrics.stringWidth(label)) / 2;
                g.setColor(MUTED);
                g.drawString(label, labelX, bottom + 18);
            }

            g.dispose();
        }
    }
}
