package com.app.responsivejavaapp.scale;

/**
 * Enumeration of logical font sizes for DPI-independent text rendering.
 * <p>
 * Instead of using hardcoded point sizes, use these logical sizes to ensure
 * consistent text appearance across different DPI settings. The actual point
 * size is determined by the current scale factor.
 */
public enum LogicalSize {

    /** Tooltips, minor labels (9pt at 96 DPI) */
    TINY(9),

    /** Secondary text, captions (10pt at 96 DPI) */
    SMALL(10),

    /** Body text, form fields (12pt at 96 DPI) */
    NORMAL(12),

    /** Emphasized text (14pt at 96 DPI) */
    MEDIUM(14),

    /** Section headers (16pt at 96 DPI) */
    LARGE(16),

    /** Dialog titles (18pt at 96 DPI) */
    XLARGE(18),

    /** Main titles (24pt at 96 DPI) */
    HUGE(24);

    private final int basePointSize;

    LogicalSize(int basePointSize) {
        this.basePointSize = basePointSize;
    }

    /**
     * Returns the base point size at 96 DPI (1.0x scale).
     *
     * @return the base point size
     */
    public int getBasePointSize() {
        return basePointSize;
    }

    /**
     * Returns the point size for a given scale factor.
     * <p>
     * Note: Java's font rendering is inherently DPI-aware when using point sizes,
     * so we typically don't need to scale the point size itself. However, this
     * method is useful when calculating line heights or component sizing based
     * on font metrics.
     *
     * @param scaleFactor the current scale factor
     * @return the scaled point size
     */
    public int getScaledPointSize(double scaleFactor) {
        return (int) Math.round(basePointSize * scaleFactor);
    }
}
