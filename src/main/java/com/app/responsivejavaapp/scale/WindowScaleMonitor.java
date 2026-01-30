package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.Timer;

/**
 * Monitors a window's position and detects when it moves to a monitor with a different DPI.
 * <p>
 * When the window moves to a monitor with a different scale factor, this class
 * updates the ScaleManager and triggers scale change notifications to all listeners.
 * <p>
 * Features:
 * <ul>
 *   <li>Debounces rapid location changes during window dragging</li>
 *   <li>Detects the monitor containing the majority of the window area</li>
 *   <li>Automatically updates ScaleManager when DPI changes</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * JFrame frame = new JFrame();
 * WindowScaleMonitor monitor = new WindowScaleMonitor(frame);
 * // Later, when disposing:
 * monitor.dispose();
 * </pre>
 */
public class WindowScaleMonitor {

    private static final Logger logger = LogManager.getLogger(WindowScaleMonitor.class);

    /** Default debounce delay in milliseconds */
    private static final int DEFAULT_DEBOUNCE_MS = 100;

    private final Window window;
    private final ScaleManager scaleManager;
    private final ComponentAdapter componentListener;
    private final Timer debounceTimer;

    /** Last known GraphicsConfiguration to detect monitor changes */
    private GraphicsConfiguration lastGraphicsConfig;

    /** Last known scale factor for the current monitor */
    private double lastMonitorScale;

    /**
     * Creates a WindowScaleMonitor for the specified window with default debounce delay.
     *
     * @param window the window to monitor
     */
    public WindowScaleMonitor(Window window) {
        this(window, DEFAULT_DEBOUNCE_MS);
    }

    /**
     * Creates a WindowScaleMonitor for the specified window.
     *
     * @param window      the window to monitor
     * @param debounceMs  debounce delay in milliseconds for move events
     */
    public WindowScaleMonitor(Window window, int debounceMs) {
        if (window == null) {
            throw new IllegalArgumentException("Window cannot be null");
        }

        this.window = window;
        this.scaleManager = ScaleManager.getInstance();
        this.lastGraphicsConfig = window.getGraphicsConfiguration();
        this.lastMonitorScale = scaleManager.detectScaleFactorFor(lastGraphicsConfig);

        // Create debounce timer (does not repeat)
        this.debounceTimer = new Timer(debounceMs, e -> checkMonitorChange());
        this.debounceTimer.setRepeats(false);

        // Create component listener for window movement
        this.componentListener = new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                // Restart debounce timer on each move
                debounceTimer.restart();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                // Also check on resize as window may span monitors differently
                debounceTimer.restart();
            }
        };

        // Register listener
        window.addComponentListener(componentListener);

        logger.debug("WindowScaleMonitor attached to window, initial scale: {}x", lastMonitorScale);
    }

    /**
     * Checks if the window has moved to a monitor with a different scale factor.
     */
    private void checkMonitorChange() {
        GraphicsConfiguration currentConfig = getWindowGraphicsConfiguration();
        if (currentConfig == null) {
            return;
        }

        // Check if monitor changed
        if (currentConfig != lastGraphicsConfig) {
            double newScale = scaleManager.detectScaleFactorFor(currentConfig);

            logger.debug("Window moved to new monitor, scale: {}x -> {}x", lastMonitorScale, newScale);

            lastGraphicsConfig = currentConfig;

            // Update ScaleManager if scale factor changed
            if (Math.abs(newScale - lastMonitorScale) > 0.001) {
                lastMonitorScale = newScale;
                scaleManager.updateScaleFactor(newScale);
                logger.info("DPI scale updated to {}x after monitor change", newScale);
            }
        }
    }

    /**
     * Determines the GraphicsConfiguration for the monitor containing the majority
     * of the window's area.
     *
     * @return the GraphicsConfiguration for the primary monitor, or null if not determinable
     */
    private GraphicsConfiguration getWindowGraphicsConfiguration() {
        Rectangle windowBounds = window.getBounds();

        // If window is not visible or has no size, use current config
        if (windowBounds.width <= 0 || windowBounds.height <= 0) {
            return window.getGraphicsConfiguration();
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] devices = ge.getScreenDevices();

        GraphicsConfiguration bestConfig = null;
        int maxOverlapArea = 0;

        for (GraphicsDevice device : devices) {
            GraphicsConfiguration config = device.getDefaultConfiguration();
            Rectangle screenBounds = config.getBounds();

            // Calculate overlap area
            Rectangle overlap = windowBounds.intersection(screenBounds);
            int overlapArea = overlap.width * overlap.height;

            if (overlapArea > maxOverlapArea) {
                maxOverlapArea = overlapArea;
                bestConfig = config;
            }
        }

        return bestConfig != null ? bestConfig : window.getGraphicsConfiguration();
    }

    /**
     * Forces an immediate check for monitor/scale changes.
     * Useful after programmatically moving the window.
     */
    public void forceCheck() {
        debounceTimer.stop();
        checkMonitorChange();
    }

    /**
     * Returns the current scale factor for the monitor containing the window.
     *
     * @return the current monitor's scale factor
     */
    public double getCurrentMonitorScale() {
        return lastMonitorScale;
    }

    /**
     * Returns the window being monitored.
     *
     * @return the monitored window
     */
    public Window getWindow() {
        return window;
    }

    /**
     * Stops monitoring and releases resources.
     * Call this when the window is being disposed.
     */
    public void dispose() {
        debounceTimer.stop();
        window.removeComponentListener(componentListener);
        logger.debug("WindowScaleMonitor disposed");
    }
}
