package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Timer;

/**
 * Monitors for runtime DPI/scaling changes at the operating system level.
 * <p>
 * When the user changes display scaling in OS settings (e.g., Windows Settings →
 * Display → Scale), this class detects the change and updates the ScaleManager,
 * which then notifies all registered listeners.
 * <p>
 * Platform-specific detection:
 * <ul>
 *   <li><b>Windows:</b> Listens for "win.defaultDPI" property changes</li>
 *   <li><b>macOS:</b> Changes detected automatically via GraphicsConfiguration</li>
 *   <li><b>Linux:</b> Polls for changes (GSettings/KDE changes not directly observable)</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * // Start monitoring (typically at application startup)
 * RuntimeDpiMonitor.getInstance().start();
 *
 * // Stop monitoring (typically at application shutdown)
 * RuntimeDpiMonitor.getInstance().stop();
 * </pre>
 */
public final class RuntimeDpiMonitor {

    private static final Logger logger = LogManager.getLogger(RuntimeDpiMonitor.class);

    /** Windows DPI property name */
    private static final String WIN_DPI_PROPERTY = "win.defaultDPI";

    /** Desktop property names that may indicate DPI changes */
    private static final String[] DESKTOP_PROPERTIES = {
            "win.defaultDPI",
            "gnome.Xft/DPI",
            "sun.java2d.uiScale"
    };

    /** Polling interval for platforms without property change notifications (ms) */
    private static final int POLL_INTERVAL_MS = 2000;

    /** Singleton instance */
    private static volatile RuntimeDpiMonitor instance;

    private final ScaleManager scaleManager;
    private final PropertyChangeListener propertyListener;
    private final Timer pollTimer;

    private boolean started;
    private double lastKnownScale;

    /**
     * Returns the singleton instance.
     *
     * @return the RuntimeDpiMonitor instance
     */
    public static RuntimeDpiMonitor getInstance() {
        if (instance == null) {
            synchronized (RuntimeDpiMonitor.class) {
                if (instance == null) {
                    instance = new RuntimeDpiMonitor();
                }
            }
        }
        return instance;
    }

    /**
     * Private constructor - use {@link #getInstance()}.
     */
    private RuntimeDpiMonitor() {
        this.scaleManager = ScaleManager.getInstance();
        this.lastKnownScale = scaleManager.getScaleFactor();
        this.started = false;

        // Create property change listener for desktop properties
        this.propertyListener = this::handlePropertyChange;

        // Create polling timer for platforms without property notifications
        this.pollTimer = new Timer(POLL_INTERVAL_MS, e -> pollForDpiChange());
        this.pollTimer.setRepeats(true);

        logger.debug("RuntimeDpiMonitor initialized, current scale: {}x", lastKnownScale);
    }

    /**
     * Starts monitoring for DPI changes.
     * Call this at application startup.
     */
    public synchronized void start() {
        if (started) {
            logger.debug("RuntimeDpiMonitor already started");
            return;
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Register property change listeners for known DPI-related properties
        for (String property : DESKTOP_PROPERTIES) {
            try {
                toolkit.addPropertyChangeListener(property, propertyListener);
                logger.debug("Registered listener for property: {}", property);
            } catch (Exception e) {
                logger.debug("Could not register listener for property {}: {}", property, e.getMessage());
            }
        }

        // Start polling timer as fallback (especially for Linux)
        if (isLinux()) {
            pollTimer.start();
            logger.debug("Started polling timer for Linux DPI detection");
        }

        started = true;
        logger.info("RuntimeDpiMonitor started");
    }

    /**
     * Stops monitoring for DPI changes.
     * Call this at application shutdown.
     */
    public synchronized void stop() {
        if (!started) {
            return;
        }

        Toolkit toolkit = Toolkit.getDefaultToolkit();

        // Unregister property change listeners
        for (String property : DESKTOP_PROPERTIES) {
            try {
                toolkit.removePropertyChangeListener(property, propertyListener);
            } catch (Exception e) {
                logger.debug("Could not remove listener for property {}: {}", property, e.getMessage());
            }
        }

        // Stop polling timer
        pollTimer.stop();

        started = false;
        logger.info("RuntimeDpiMonitor stopped");
    }

    /**
     * Handles property change events from the Toolkit.
     */
    private void handlePropertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        logger.debug("Desktop property changed: {} = {}", propertyName, evt.getNewValue());

        // Trigger a scale factor refresh
        checkForScaleChange();
    }

    /**
     * Polls for DPI changes (used on platforms without property notifications).
     */
    private void pollForDpiChange() {
        checkForScaleChange();
    }

    /**
     * Checks if the scale factor has changed and updates ScaleManager if so.
     */
    private void checkForScaleChange() {
        scaleManager.refreshScaleFactor();
        double currentScale = scaleManager.getScaleFactor();

        if (Math.abs(currentScale - lastKnownScale) > 0.001) {
            logger.info("Runtime DPI change detected: {}x -> {}x", lastKnownScale, currentScale);
            lastKnownScale = currentScale;
        }
    }

    /**
     * Forces an immediate check for DPI changes.
     */
    public void forceCheck() {
        checkForScaleChange();
    }

    /**
     * Returns whether the monitor is currently active.
     *
     * @return true if monitoring is active
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * Checks if running on Linux.
     */
    private static boolean isLinux() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("linux") || os.contains("nix") || os.contains("nux");
    }

    /**
     * Checks if running on Windows.
     */
    private static boolean isWindows() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("win");
    }

    /**
     * Checks if running on macOS.
     */
    private static boolean isMacOS() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return os.contains("mac");
    }
}
