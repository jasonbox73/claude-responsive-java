package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton providing centralized font management with DPI-aware sizing.
 * <p>
 * FontManager delivers consistently styled fonts based on logical sizes,
 * handling font caching and scale change invalidation automatically.
 * <p>
 * Usage:
 * <pre>
 * Font bodyFont = FontManager.getInstance().getFont(LogicalSize.NORMAL);
 * Font headerFont = FontManager.getInstance().getFont(LogicalSize.LARGE, Font.BOLD);
 * </pre>
 */
public final class FontManager implements ScaleChangeListener {

    private static final Logger logger = LogManager.getLogger(FontManager.class);

    /** Default font family */
    private static final String DEFAULT_FAMILY = Font.SANS_SERIF;

    /** Monospaced font family */
    private static final String MONOSPACED_FAMILY = Font.MONOSPACED;

    /** Cache key separator */
    private static final String CACHE_KEY_SEP = "|";

    /** Font cache: key = "family|size|style", value = Font */
    private final Map<String, Font> fontCache;

    /** Reference to ScaleManager for scale factor queries */
    private final ScaleManager scaleManager;

    /**
     * Holder pattern for thread-safe lazy initialization.
     */
    private static class Holder {
        private static final FontManager INSTANCE = new FontManager();
    }

    /**
     * Private constructor - use {@link #getInstance()}.
     */
    private FontManager() {
        this.fontCache = new ConcurrentHashMap<>();
        this.scaleManager = ScaleManager.getInstance();
        this.scaleManager.addScaleChangeListener(this);
        logger.info("FontManager initialized");
    }

    /**
     * Returns the singleton instance.
     *
     * @return the FontManager instance
     */
    public static FontManager getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Returns a plain font at the specified logical size using the default family.
     *
     * @param size the logical font size
     * @return the font
     */
    public Font getFont(LogicalSize size) {
        return getFont(DEFAULT_FAMILY, size, Font.PLAIN);
    }

    /**
     * Returns a font at the specified logical size and style using the default family.
     *
     * @param size  the logical font size
     * @param style the font style (Font.PLAIN, Font.BOLD, Font.ITALIC, or Font.BOLD | Font.ITALIC)
     * @return the font
     */
    public Font getFont(LogicalSize size, int style) {
        return getFont(DEFAULT_FAMILY, size, style);
    }

    /**
     * Returns a font with the specified family, logical size, and style.
     *
     * @param family the font family name
     * @param size   the logical font size
     * @param style  the font style
     * @return the font
     */
    public Font getFont(String family, LogicalSize size, int style) {
        final LogicalSize effectiveSize = (size != null) ? size : LogicalSize.NORMAL;
        final String effectiveFamily = (family != null && !family.isEmpty()) ? family : DEFAULT_FAMILY;

        int pointSize = effectiveSize.getBasePointSize();
        String cacheKey = buildCacheKey(effectiveFamily, pointSize, style);

        return fontCache.computeIfAbsent(cacheKey, key -> {
            Font font = new Font(effectiveFamily, style, pointSize);
            logger.debug("Created font: {} {} {}pt", effectiveFamily, getStyleName(style), pointSize);
            return font;
        });
    }

    /**
     * Returns a bold font at the specified logical size.
     *
     * @param size the logical font size
     * @return the bold font
     */
    public Font getBoldFont(LogicalSize size) {
        return getFont(DEFAULT_FAMILY, size, Font.BOLD);
    }

    /**
     * Returns an italic font at the specified logical size.
     *
     * @param size the logical font size
     * @return the italic font
     */
    public Font getItalicFont(LogicalSize size) {
        return getFont(DEFAULT_FAMILY, size, Font.ITALIC);
    }

    /**
     * Returns a bold italic font at the specified logical size.
     *
     * @param size the logical font size
     * @return the bold italic font
     */
    public Font getBoldItalicFont(LogicalSize size) {
        return getFont(DEFAULT_FAMILY, size, Font.BOLD | Font.ITALIC);
    }

    /**
     * Returns a monospaced font at the specified logical size.
     * Useful for code display, data tables, and fixed-width text.
     *
     * @param size the logical font size
     * @return the monospaced font
     */
    public Font getMonospacedFont(LogicalSize size) {
        return getFont(MONOSPACED_FAMILY, size, Font.PLAIN);
    }

    /**
     * Returns a monospaced font with the specified logical size and style.
     *
     * @param size  the logical font size
     * @param style the font style
     * @return the monospaced font
     */
    public Font getMonospacedFont(LogicalSize size, int style) {
        return getFont(MONOSPACED_FAMILY, size, style);
    }

    /**
     * Creates a scaled version of an existing font for the current DPI.
     * <p>
     * This method is useful when you have a base font and need to create
     * a version that's appropriately sized for the current scale factor.
     *
     * @param baseFont the base font at 96 DPI
     * @return a new font with the size adjusted for current DPI
     */
    public Font deriveScaled(Font baseFont) {
        if (baseFont == null) {
            return null;
        }
        int scaledSize = scaleManager.scale(baseFont.getSize());
        return baseFont.deriveFont((float) scaledSize);
    }

    /**
     * Returns a font with a specific point size (not logical size).
     * <p>
     * Use this method only when you need precise control over point size.
     * For most cases, prefer using {@link #getFont(LogicalSize)} with logical sizes.
     *
     * @param family    the font family
     * @param pointSize the point size
     * @param style     the font style
     * @return the font
     */
    public Font getFontByPointSize(String family, int pointSize, int style) {
        final String effectiveFamily = (family != null && !family.isEmpty()) ? family : DEFAULT_FAMILY;
        String cacheKey = buildCacheKey(effectiveFamily, pointSize, style);
        return fontCache.computeIfAbsent(cacheKey, key -> new Font(effectiveFamily, style, pointSize));
    }

    /**
     * Checks if a font family is available on this system.
     *
     * @param family the font family name to check
     * @return true if the font family is available
     */
    public boolean isFontAvailable(String family) {
        if (family == null || family.isEmpty()) {
            return false;
        }
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        for (String fontName : fontNames) {
            if (fontName.equalsIgnoreCase(family)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the list of available font family names on this system.
     *
     * @return array of font family names
     */
    public String[] getAvailableFontFamilies() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        return ge.getAvailableFontFamilyNames();
    }

    /**
     * Clears the font cache.
     * Call this when fonts need to be recreated (e.g., after a scale change).
     */
    public void clearCache() {
        fontCache.clear();
        logger.debug("Font cache cleared");
    }

    /**
     * Returns the current cache size.
     *
     * @return number of cached fonts
     */
    public int getCacheSize() {
        return fontCache.size();
    }

    @Override
    public void onScaleChanged(double oldScale, double newScale) {
        // Clear cache so fonts are recreated with new scale factor if needed
        clearCache();
        logger.debug("Font cache cleared due to scale change: {} -> {}", oldScale, newScale);
    }

    /**
     * Builds a cache key for font lookup.
     */
    private String buildCacheKey(String family, int size, int style) {
        return family + CACHE_KEY_SEP + size + CACHE_KEY_SEP + style;
    }

    /**
     * Returns a human-readable style name.
     */
    private String getStyleName(int style) {
        switch (style) {
            case Font.BOLD:
                return "Bold";
            case Font.ITALIC:
                return "Italic";
            case Font.BOLD | Font.ITALIC:
                return "BoldItalic";
            default:
                return "Plain";
        }
    }
}
