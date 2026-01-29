package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Singleton providing centralized access to all scaling operations.
 * <p>
 * ScaleManager detects the current display DPI, provides scaling utilities,
 * and notifies registered listeners when the scale factor changes.
 * <p>
 * Thread-safe singleton using the holder pattern.
 */
public final class ScaleManager {

    private static final Logger logger = LogManager.getLogger(ScaleManager.class);

    /** Base DPI (96 = Windows 100% scaling) */
    public static final int BASE_DPI = 96;

    /** Minimum supported scale factor (75% = 72 DPI) */
    public static final double MIN_SCALE = 0.75;

    /** Maximum supported scale factor (300% = 288 DPI) */
    public static final double MAX_SCALE = 3.0;

    /** Current scale factor (1.0 = 96 DPI baseline) */
    private volatile double scaleFactor;

    /** Listeners registered for scale change notifications (weak references) */
    private final List<WeakReference<ScaleChangeListener>> listeners;

    /**
     * Holder pattern for thread-safe lazy initialization.
     */
    private static class Holder {
        private static final ScaleManager INSTANCE = new ScaleManager();
    }

    /**
     * Private constructor - use {@link #getInstance()}.
     */
    private ScaleManager() {
        this.listeners = new CopyOnWriteArrayList<>();
        this.scaleFactor = detectScaleFactor();
        logger.info("ScaleManager initialized with scale factor: {}", scaleFactor);
    }

    /**
     * Returns the singleton instance.
     *
     * @return the ScaleManager instance
     */
    public static ScaleManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns the current scale factor relative to 96 DPI baseline.
     *
     * @return scale factor (1.0 = 96 DPI, 2.0 = 192 DPI, etc.)
     */
    public double getScaleFactor() {
        return scaleFactor;
    }

    /**
     * Returns the current DPI value.
     *
     * @return DPI value (96, 120, 144, 192, etc.)
     */
    public int getCurrentDpi() {
        return (int) Math.round(scaleFactor * BASE_DPI);
    }

    /**
     * Scales an integer value by the current scale factor.
     *
     * @param value the base value at 96 DPI
     * @return the scaled value, rounded to nearest integer
     */
    public int scale(int value) {
        return (int) Math.round(value * scaleFactor);
    }

    /**
     * Scales a double value by the current scale factor.
     *
     * @param value the base value at 96 DPI
     * @return the scaled value
     */
    public double scale(double value) {
        return value * scaleFactor;
    }

    /**
     * Scales a float value by the current scale factor.
     *
     * @param value the base value at 96 DPI
     * @return the scaled value
     */
    public float scale(float value) {
        return (float) (value * scaleFactor);
    }

    /**
     * Scales a Dimension by the current scale factor.
     *
     * @param dim the base dimension at 96 DPI
     * @return a new Dimension with scaled width and height
     */
    public Dimension scale(Dimension dim) {
        if (dim == null) {
            return null;
        }
        return new Dimension(scale(dim.width), scale(dim.height));
    }

    /**
     * Scales Insets by the current scale factor.
     *
     * @param insets the base insets at 96 DPI
     * @return new Insets with all values scaled
     */
    public Insets scale(Insets insets) {
        if (insets == null) {
            return null;
        }
        return new Insets(
                scale(insets.top),
                scale(insets.left),
                scale(insets.bottom),
                scale(insets.right)
        );
    }

    /**
     * Scales a Rectangle by the current scale factor.
     *
     * @param rect the base rectangle at 96 DPI
     * @return a new Rectangle with scaled position and size
     */
    public Rectangle scale(Rectangle rect) {
        if (rect == null) {
            return null;
        }
        return new Rectangle(
                scale(rect.x),
                scale(rect.y),
                scale(rect.width),
                scale(rect.height)
        );
    }

    /**
     * Scales a Point by the current scale factor.
     *
     * @param point the base point at 96 DPI
     * @return a new Point with scaled coordinates
     */
    public Point scale(Point point) {
        if (point == null) {
            return null;
        }
        return new Point(scale(point.x), scale(point.y));
    }

    /**
     * Reverses scaling to convert from scaled coordinates to base coordinates.
     *
     * @param scaledValue the value at current DPI
     * @return the equivalent value at 96 DPI
     */
    public int unscale(int scaledValue) {
        return (int) Math.round(scaledValue / scaleFactor);
    }

    /**
     * Reverses scaling for a double value.
     *
     * @param scaledValue the value at current DPI
     * @return the equivalent value at 96 DPI
     */
    public double unscale(double scaledValue) {
        return scaledValue / scaleFactor;
    }

    /**
     * Registers a listener for scale change notifications.
     * Uses weak references to prevent memory leaks.
     *
     * @param listener the listener to register
     */
    public void addScaleChangeListener(ScaleChangeListener listener) {
        if (listener == null) {
            return;
        }
        // Check if already registered
        for (WeakReference<ScaleChangeListener> ref : listeners) {
            if (ref.get() == listener) {
                return;
            }
        }
        listeners.add(new WeakReference<>(listener));
        logger.debug("Scale change listener added, total listeners: {}", listeners.size());
    }

    /**
     * Unregisters a listener from scale change notifications.
     *
     * @param listener the listener to unregister
     */
    public void removeScaleChangeListener(ScaleChangeListener listener) {
        if (listener == null) {
            return;
        }
        // CopyOnWriteArrayList doesn't support iterator.remove(), use removeIf instead
        listeners.removeIf(ref -> {
            ScaleChangeListener l = ref.get();
            return l == null || l == listener;
        });
        logger.debug("Scale change listener removed, remaining listeners: {}", listeners.size());
    }

    /**
     * Notifies all registered listeners of a scale change.
     * Also cleans up any garbage-collected listener references.
     *
     * @param oldScale the previous scale factor
     * @param newScale the new scale factor
     */
    private void notifyListeners(double oldScale, double newScale) {
        // Clean up dead references first
        listeners.removeIf(ref -> ref.get() == null);

        // Notify all remaining listeners
        for (WeakReference<ScaleChangeListener> ref : listeners) {
            ScaleChangeListener listener = ref.get();
            if (listener != null) {
                try {
                    listener.onScaleChanged(oldScale, newScale);
                } catch (Exception e) {
                    logger.error("Error notifying scale change listener", e);
                }
            }
        }
    }

    /**
     * Updates the scale factor and notifies listeners if it changed.
     * Call this method when detecting a DPI change at runtime.
     *
     * @param newScaleFactor the new scale factor
     */
    public void updateScaleFactor(double newScaleFactor) {
        // Clamp to supported range
        newScaleFactor = Math.max(MIN_SCALE, Math.min(MAX_SCALE, newScaleFactor));

        if (Math.abs(newScaleFactor - scaleFactor) > 0.001) {
            double oldScale = scaleFactor;
            scaleFactor = newScaleFactor;
            logger.info("Scale factor changed from {} to {}", oldScale, newScaleFactor);
            notifyListeners(oldScale, newScaleFactor);
        }
    }

    /**
     * Forces re-detection of the scale factor from the system.
     * Call this when the display configuration may have changed.
     */
    public void refreshScaleFactor() {
        double detected = detectScaleFactor();
        updateScaleFactor(detected);
    }

    /**
     * Detects the current scale factor using platform-specific methods.
     *
     * @return the detected scale factor
     */
    private double detectScaleFactor() {
        double scale = 1.0;

        try {
            // Method 1: Check sun.java2d.uiScale system property (can be set for testing)
            String uiScaleProp = System.getProperty("sun.java2d.uiScale");
            if (uiScaleProp != null && !uiScaleProp.isEmpty()) {
                try {
                    scale = Double.parseDouble(uiScaleProp);
                    logger.debug("Scale factor from sun.java2d.uiScale: {}", scale);
                    return clampScale(scale);
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse sun.java2d.uiScale: {}", uiScaleProp);
                }
            }

            // Method 2: Check GDK_SCALE environment variable (Linux)
            String gdkScale = System.getenv("GDK_SCALE");
            if (gdkScale != null && !gdkScale.isEmpty()) {
                try {
                    scale = Double.parseDouble(gdkScale);
                    logger.debug("Scale factor from GDK_SCALE: {}", scale);
                    return clampScale(scale);
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse GDK_SCALE: {}", gdkScale);
                }
            }

            // Method 3: Use AffineTransform from GraphicsConfiguration
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            if (!ge.isHeadlessInstance()) {
                GraphicsDevice gd = ge.getDefaultScreenDevice();
                GraphicsConfiguration gc = gd.getDefaultConfiguration();
                AffineTransform tx = gc.getDefaultTransform();
                scale = tx.getScaleX();
                logger.debug("Scale factor from AffineTransform: {}", scale);
                if (scale > 0) {
                    return clampScale(scale);
                }
            }

            // Method 4: Fallback to Toolkit screen resolution
            if (!GraphicsEnvironment.isHeadless()) {
                int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                scale = (double) dpi / BASE_DPI;
                logger.debug("Scale factor from Toolkit ({}DPI): {}", dpi, scale);
                return clampScale(scale);
            }

        } catch (HeadlessException e) {
            logger.debug("Running in headless mode, using default scale factor");
        } catch (Exception e) {
            logger.warn("Error detecting scale factor, using default", e);
        }

        return 1.0;
    }

    /**
     * Clamps a scale factor to the supported range.
     *
     * @param scale the scale factor to clamp
     * @return the clamped scale factor
     */
    private double clampScale(double scale) {
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, scale));
    }

    /**
     * Returns the number of currently registered listeners.
     * Useful for testing and debugging.
     *
     * @return the listener count
     */
    public int getListenerCount() {
        // Clean up dead references first
        listeners.removeIf(ref -> ref.get() == null);
        return listeners.size();
    }

    /**
     * Package-private method for testing purposes.
     * Allows setting scale factor directly without detection.
     *
     * @param scaleFactor the scale factor to set
     */
    void setScaleFactorForTesting(double scaleFactor) {
        this.scaleFactor = clampScale(scaleFactor);
    }
}
