package com.knowbot;

import com.knowbot.db.Database;
import com.knowbot.ui.DashboardFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                        UIManager.getSystemLookAndFeelClassName());

                Database.initialize();

                DashboardFrame dashboard = new DashboardFrame();
                dashboard.setVisible(true);

            } catch (Exception exception) {
                exception.printStackTrace();

                JOptionPane.showMessageDialog(
                        null,
                        "KnowBot AI could not start.\n"
                                + exception.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}
