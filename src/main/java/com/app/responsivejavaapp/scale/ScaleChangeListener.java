package com.app.responsivejavaapp.scale;

/**
 * Callback interface for components that need to respond to DPI/scale factor changes.
 * Implement this interface and register with {@link ScaleManager} to receive notifications
 * when the display scale factor changes.
 */
@FunctionalInterface
public interface ScaleChangeListener {

    /**
     * Called when the display scale factor changes.
     *
     * @param oldScale the previous scale factor
     * @param newScale the new scale factor
     */
    void onScaleChanged(double oldScale, double newScale);
}
