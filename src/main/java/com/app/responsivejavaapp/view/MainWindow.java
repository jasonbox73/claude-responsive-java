package com.app.responsivejavaapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main application window with basic UI components using MigLayout.
 */
public class MainWindow extends JFrame {
    private static final Logger logger = LogManager.getLogger(MainWindow.class);

    private JLabel statusLabel;
    private JButton actionButton;
    private JTextArea textArea;

    public MainWindow() {
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        logger.debug("MainWindow initialized");
    }

    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        setTitle("Responsive Java App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null); // Center on screen

        statusLabel = new JLabel("Welcome to Responsive Java App!");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        actionButton = new JButton("Click Me");

        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        textArea.setText("Application started successfully.\nClick the button to test logging.");
    }

    /**
     * Layout components using MigLayout
     */
    private void layoutComponents() {
        // MigLayout: "layout constraints", "column constraints", "row constraints"
        JPanel mainPanel = new JPanel(new MigLayout(
            "fill, insets 10",           // Fill panel, 10px insets
            "[grow, fill]",               // One column that grows and fills
            "[][grow, fill][]"            // Three rows: header, growing center, footer
        ));

        // Top panel with status label
        mainPanel.add(statusLabel, "wrap, center");

        // Center panel with text area in scroll pane
        JScrollPane scrollPane = new JScrollPane(textArea);
        mainPanel.add(scrollPane, "wrap, grow");

        // Bottom panel with button
        mainPanel.add(actionButton, "center");

        setContentPane(mainPanel);
    }

    /**
     * Setup event handlers for UI components
     */
    private void setupEventHandlers() {
        actionButton.addActionListener(this::handleButtonClick);
    }

    /**
     * Handle button click event
     */
    private void handleButtonClick(ActionEvent e) {
        logger.info("Action button clicked");
        String message = "Button clicked at " + java.time.LocalTime.now();
        textArea.append("\n" + message);
        statusLabel.setText("Action performed!");

        // Reset status after 2 seconds
        Timer timer = new Timer(2000, evt -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Get the status label (for testing)
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * Get the action button (for testing)
     */
    public JButton getActionButton() {
        return actionButton;
    }
}
