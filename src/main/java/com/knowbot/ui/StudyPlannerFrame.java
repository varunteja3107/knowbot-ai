package com.knowbot.ui;

import com.knowbot.dao.StudyTaskDao;
import com.knowbot.dao.StudyTaskDao.StudyTask;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class StudyPlannerFrame extends JFrame {

    private final StudyTaskDao dao = new StudyTaskDao();
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[]{"ID", "Task", "Subject", "Priority",
                    "Due Date", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    private final JTable table = new JTable(model);
    private final JLabel summary = new JLabel("Loading study tasks...");

    private final JTextField titleField = new JTextField(18);
    private final JTextField subjectField = new JTextField("General", 12);
    private final JTextField dateField =
            new JTextField(LocalDate.now().plusDays(1).toString(), 10);
    private final JComboBox<String> priorityBox =
            new JComboBox<>(new String[]{"High", "Medium", "Low"});

    private final Color green = new Color(57, 105, 77);
    private final Color background = new Color(244, 246, 242);

    public StudyPlannerFrame() {
        setTitle("KnowBot AI | Study Planner");
        setSize(900, 570);
        setMinimumSize(new Dimension(760, 480));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        getContentPane().setBackground(background);

        JPanel root = new JPanel(new BorderLayout(0, 18));
        root.setBackground(background);
        root.setBorder(new EmptyBorder(22, 24, 22, 24));
        setContentPane(root);

        JLabel heading = new JLabel("Smart Study Planner");
        heading.setFont(new Font("SansSerif", Font.BOLD, 26));
        heading.setForeground(green);

        summary.setFont(new Font("SansSerif", Font.PLAIN, 14));

        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        header.add(heading, BorderLayout.NORTH);
        header.add(summary, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createTitledBorder("Add a study task"));

        form.add(new JLabel("Task"));
        form.add(titleField);
        form.add(new JLabel("Subject"));
        form.add(subjectField);
        form.add(new JLabel("Priority"));
        form.add(priorityBox);
        form.add(new JLabel("Due (YYYY-MM-DD)"));
        form.add(dateField);

        JButton addButton = new JButton("Add Task");
        addButton.setBackground(green);
        addButton.setForeground(Color.WHITE);
        addButton.addActionListener(e -> addTask());
        form.add(addButton);

        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(230);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(
                new Color(220, 226, 218)));

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        JButton completeButton = new JButton("Mark Complete / Pending");
        JButton deleteButton = new JButton("Delete Selected");
        JButton refreshButton = new JButton("Refresh");

        completeButton.addActionListener(e -> toggleTask());
        deleteButton.addActionListener(e -> deleteTask());
        refreshButton.addActionListener(e -> refreshTasks());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(refreshButton);
        actions.add(completeButton);
        actions.add(deleteButton);
        root.add(actions, BorderLayout.SOUTH);

        refreshTasks();
    }

    private void addTask() {
        String title = titleField.getText().trim();
        String subject = subjectField.getText().trim();
        String due = dateField.getText().trim();

        if (title.isEmpty()) {
            showError("Enter a task title.");
            return;
        }

        if (subject.isEmpty()) {
            subject = "General";
        }

        try {
            LocalDate.parse(due);
        } catch (Exception ex) {
            showError("Enter a valid date in YYYY-MM-DD format.");
            return;
        }

        try {
            dao.addTask(title, subject,
                    (String) priorityBox.getSelectedItem(), due);
            titleField.setText("");
            refreshTasks();
        } catch (SQLException ex) {
            showError("Could not save task: " + ex.getMessage());
        }
    }

    private void toggleTask() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a task first.");
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        boolean completed = "Pending".equals(model.getValueAt(row, 5));

        try {
            dao.setCompleted(id, completed);
            refreshTasks();
        } catch (SQLException ex) {
            showError("Could not update task: " + ex.getMessage());
        }
    }

    private void deleteTask() {
        int row = table.getSelectedRow();
        if (row < 0) {
            showError("Select a task to delete.");
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this, "Delete this study task?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        int id = (int) model.getValueAt(row, 0);
        try {
            dao.deleteTask(id);
            refreshTasks();
        } catch (SQLException ex) {
            showError("Could not delete task: " + ex.getMessage());
        }
    }

    private void refreshTasks() {
        try {
            List<StudyTask> tasks = dao.findAll();
            model.setRowCount(0);

            int completed = 0;
            int overdue = 0;
            LocalDate today = LocalDate.now();

            for (StudyTask task : tasks) {
                String status = task.completed() ? "Completed" : "Pending";

                if (task.completed()) {
                    completed++;
                } else {
                    try {
                        if (LocalDate.parse(task.dueDate()).isBefore(today)) {
                            overdue++;
                            status = "Overdue";
                        }
                    } catch (Exception ignored) {
                        status = "Pending";
                    }
                }

                model.addRow(new Object[]{
                        task.id(), task.title(), task.subject(),
                        task.priority(), task.dueDate(), status
                });
            }

            int pending = tasks.size() - completed;
            summary.setText("Total: " + tasks.size()
                    + "    |    Completed: " + completed
                    + "    |    Pending: " + pending
                    + "    |    Overdue: " + overdue);
        } catch (SQLException ex) {
            showError("Could not load study tasks: " + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
                this, message, "Study Planner",
                JOptionPane.WARNING_MESSAGE);
    }
}
