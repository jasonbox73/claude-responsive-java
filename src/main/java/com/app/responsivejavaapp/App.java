package com.app.responsivejavaapp;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.app.responsivejavaapp.view.MainWindow;

/**
 * Main application entry point for Responsive Java App.
 * Initializes the Swing GUI on the Event Dispatch Thread.
 */
public class App {
    private static final Logger logger = LogManager.getLogger(App.class);

    public static void main(String[] args) {
        logger.info("Starting Responsive Java App...");

        // Set system look and feel for native appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            logger.debug("System look and feel set successfully");
        } catch (Exception e) {
            logger.warn("Could not set system look and feel: {}", e.getMessage());
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                MainWindow window = new MainWindow();
                window.setVisible(true);
                logger.info("Application window initialized");
            } catch (Exception e) {
                logger.error("Failed to initialize application window", e);
                System.exit(1);
            }
        });
    }
}
