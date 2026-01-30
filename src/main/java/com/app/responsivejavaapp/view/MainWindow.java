package com.app.responsivejavaapp.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.app.responsivejavaapp.scale.FontManager;
import com.app.responsivejavaapp.scale.LogicalSize;
import com.app.responsivejavaapp.scale.ScalablePanel;
import com.app.responsivejavaapp.scale.ScaleChangeListener;
import com.app.responsivejavaapp.scale.ScaleManager;

/**
 * Main application window with DPI-aware UI components.
 * <p>
 * Uses the scaling infrastructure to automatically adapt to different DPI settings:
 * <ul>
 *   <li>ScalablePanel for DPI-aware layout with MigLayout</li>
 *   <li>FontManager for consistent font scaling</li>
 *   <li>ScaleManager for window size scaling</li>
 * </ul>
 */
public class MainWindow extends JFrame implements ScaleChangeListener {
    private static final Logger logger = LogManager.getLogger(MainWindow.class);

    /** Base window dimensions at 96 DPI */
    private static final int BASE_WIDTH = 600;
    private static final int BASE_HEIGHT = 400;

    private final ScaleManager scaleManager;
    private final FontManager fontManager;

    private ScalablePanel mainPanel;
    private JLabel statusLabel;
    private JButton actionButton;
    private JTextArea textArea;

    public MainWindow() {
        this.scaleManager = ScaleManager.getInstance();
        this.fontManager = FontManager.getInstance();

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        registerForScaleChanges();

        logger.debug("MainWindow initialized at {}x scale", scaleManager.getScaleFactor());
    }

    /**
     * Initialize UI components with DPI-aware sizing.
     */
    private void initializeComponents() {
        setTitle("Responsive Java App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Scale window size based on current DPI
        Dimension scaledSize = scaleManager.scale(new Dimension(BASE_WIDTH, BASE_HEIGHT));
        setSize(scaledSize);
        setLocationRelativeTo(null);

        // Use FontManager for consistent font scaling
        statusLabel = new JLabel("Welcome to Responsive Java App!");
        statusLabel.setFont(fontManager.getFont(LogicalSize.LARGE, Font.BOLD));

        actionButton = new JButton("Click Me");
        actionButton.setFont(fontManager.getFont(LogicalSize.NORMAL));

        textArea = new JTextArea(10, 40);
        textArea.setEditable(false);
        textArea.setFont(fontManager.getFont(LogicalSize.NORMAL));
        textArea.setText("Application started successfully.\nClick the button to test logging.");
    }

    /**
     * Layout components using ScalablePanel with automatic constraint scaling.
     */
    private void layoutComponents() {
        // ScalablePanel automatically scales pixel-based MigLayout constraints
        mainPanel = new ScalablePanel(
            "fill, insets 10",      // Fill panel, 10px insets (scaled automatically)
            "[grow, fill]",          // One column that grows and fills
            "[][grow, fill][]"       // Three rows: header, growing center, footer
        );

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
     * Setup event handlers for UI components.
     */
    private void setupEventHandlers() {
        actionButton.addActionListener(this::handleButtonClick);
    }

    /**
     * Register this window to receive scale change notifications.
     */
    private void registerForScaleChanges() {
        scaleManager.addScaleChangeListener(this);
    }

    @Override
    public void onScaleChanged(double oldScale, double newScale) {
        logger.info("Scale changed from {}x to {}x", oldScale, newScale);

        // Update fonts to new scale
        statusLabel.setFont(fontManager.getFont(LogicalSize.LARGE, Font.BOLD));
        actionButton.setFont(fontManager.getFont(LogicalSize.NORMAL));
        textArea.setFont(fontManager.getFont(LogicalSize.NORMAL));

        // Revalidate and repaint (ScalablePanel handles its own layout scaling)
        revalidate();
        repaint();

        textArea.append("\nDPI scale changed to " + newScale + "x");
    }

    /**
     * Handle button click event.
     */
    private void handleButtonClick(ActionEvent e) {
        logger.info("Action button clicked");
        String message = "Button clicked at " + java.time.LocalTime.now() +
                " (scale: " + scaleManager.getScaleFactor() + "x)";
        textArea.append("\n" + message);
        statusLabel.setText("Action performed!");

        // Reset status after 2 seconds
        Timer timer = new Timer(2000, evt -> statusLabel.setText("Ready"));
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Clean up resources when window is disposed.
     */
    @Override
    public void dispose() {
        scaleManager.removeScaleChangeListener(this);
        mainPanel.dispose();
        super.dispose();
        logger.debug("MainWindow disposed");
    }

    /**
     * Get the status label (for testing).
     */
    public JLabel getStatusLabel() {
        return statusLabel;
    }

    /**
     * Get the action button (for testing).
     */
    public JButton getActionButton() {
        return actionButton;
    }

    /**
     * Get the main panel (for testing).
     */
    public ScalablePanel getMainPanel() {
        return mainPanel;
    }
}
