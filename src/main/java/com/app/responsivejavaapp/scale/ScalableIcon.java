package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

/**
 * Icon implementation that automatically selects the appropriate resolution
 * based on the current display scale factor.
 * <p>
 * ScalableIcon loads multiple resolution variants of an icon (1x, 1.5x, 2x, 3x)
 * and renders the most appropriate version for crisp display at any DPI.
 * <p>
 * Icon naming convention:
 * <ul>
 *   <li>Base: iconname.png (1x, 96 DPI)</li>
 *   <li>1.5x: iconname@1.5x.png (144 DPI)</li>
 *   <li>2x: iconname@2x.png (192 DPI)</li>
 *   <li>3x: iconname@3x.png (288 DPI)</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * ScalableIcon icon = new ScalableIcon("/icons/save", 16, 16);
 * JButton button = new JButton(icon);
 * </pre>
 */
public class ScalableIcon implements Icon, ScaleChangeListener {

    private static final Logger logger = LogManager.getLogger(ScalableIcon.class);

    /** Supported scale factors for icon variants */
    private static final double[] SCALE_VARIANTS = {1.0, 1.5, 2.0, 3.0};

    /** File extension for icon files */
    private static final String DEFAULT_EXTENSION = ".png";

    /** Base path without extension */
    private final String basePath;

    /** Base width at 96 DPI */
    private final int baseWidth;

    /** Base height at 96 DPI */
    private final int baseHeight;

    /** Available icon images keyed by scale factor */
    private final TreeMap<Double, Image> imageVariants;

    /** Currently rendered image for the active scale factor */
    private Image currentImage;

    /** Scale factor when currentImage was rendered */
    private double currentImageScale;

    /** Reference to ScaleManager */
    private final ScaleManager scaleManager;

    /**
     * Creates a ScalableIcon that loads variants from the specified base path.
     * <p>
     * The base path should not include the file extension or scale suffix.
     * Example: "/icons/save" will load save.png, save@1.5x.png, save@2x.png, save@3x.png
     *
     * @param basePath   the base resource path without extension
     * @param baseWidth  the icon width at 96 DPI (1x scale)
     * @param baseHeight the icon height at 96 DPI (1x scale)
     */
    public ScalableIcon(String basePath, int baseWidth, int baseHeight) {
        this.basePath = basePath;
        this.baseWidth = baseWidth;
        this.baseHeight = baseHeight;
        this.imageVariants = new TreeMap<>();
        this.scaleManager = ScaleManager.getInstance();
        this.currentImageScale = -1; // Force initial load

        loadImageVariants();
        scaleManager.addScaleChangeListener(this);
    }

    /**
     * Creates a ScalableIcon that auto-detects the base size from the 1x image.
     *
     * @param basePath the base resource path without extension
     */
    public ScalableIcon(String basePath) {
        this.basePath = basePath;
        this.imageVariants = new TreeMap<>();
        this.scaleManager = ScaleManager.getInstance();
        this.currentImageScale = -1;

        loadImageVariants();

        // Determine base size from 1x variant
        Image baseImage = imageVariants.get(1.0);
        if (baseImage != null) {
            this.baseWidth = baseImage.getWidth(null);
            this.baseHeight = baseImage.getHeight(null);
        } else if (!imageVariants.isEmpty()) {
            // Use first available and calculate base size
            Map.Entry<Double, Image> first = imageVariants.firstEntry();
            this.baseWidth = (int) Math.round(first.getValue().getWidth(null) / first.getKey());
            this.baseHeight = (int) Math.round(first.getValue().getHeight(null) / first.getKey());
        } else {
            this.baseWidth = 16;
            this.baseHeight = 16;
            logger.warn("No icon variants found for {}, using default size", basePath);
        }

        scaleManager.addScaleChangeListener(this);
    }

    /**
     * Loads all available resolution variants of the icon.
     */
    private void loadImageVariants() {
        // Try to load base image (1x)
        loadVariant(1.0, basePath + DEFAULT_EXTENSION);

        // Try to load scaled variants
        for (double scale : SCALE_VARIANTS) {
            if (scale != 1.0) {
                String suffix = "@" + formatScale(scale) + "x";
                loadVariant(scale, basePath + suffix + DEFAULT_EXTENSION);
            }
        }

        if (imageVariants.isEmpty()) {
            logger.warn("No icon images found for base path: {}", basePath);
        } else {
            logger.debug("Loaded {} icon variants for {}", imageVariants.size(), basePath);
        }
    }

    /**
     * Attempts to load a single variant.
     */
    private void loadVariant(double scale, String resourcePath) {
        URL url = getClass().getResource(resourcePath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            if (icon.getIconWidth() > 0) {
                imageVariants.put(scale, icon.getImage());
                logger.debug("Loaded icon variant: {} ({}x)", resourcePath, scale);
            }
        }
    }

    /**
     * Formats a scale factor for filename suffix (1.5 -> "1.5", 2.0 -> "2").
     */
    private String formatScale(double scale) {
        if (scale == Math.floor(scale)) {
            return String.valueOf((int) scale);
        }
        return String.valueOf(scale);
    }

    @Override
    public int getIconWidth() {
        return scaleManager.scale(baseWidth);
    }

    @Override
    public int getIconHeight() {
        return scaleManager.scale(baseHeight);
    }

    /**
     * Returns the base (unscaled) width.
     *
     * @return base width at 96 DPI
     */
    public int getBaseWidth() {
        return baseWidth;
    }

    /**
     * Returns the base (unscaled) height.
     *
     * @return base height at 96 DPI
     */
    public int getBaseHeight() {
        return baseHeight;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Image image = getImageForCurrentScale();
        if (image == null) {
            // Draw placeholder
            g.setColor(java.awt.Color.GRAY);
            g.fillRect(x, y, getIconWidth(), getIconHeight());
            return;
        }

        int targetWidth = getIconWidth();
        int targetHeight = getIconHeight();

        // Check if image needs scaling
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        if (imageWidth == targetWidth && imageHeight == targetHeight) {
            // Exact match, draw directly
            g.drawImage(image, x, y, null);
        } else {
            // Need to scale - use high quality interpolation
            Graphics2D g2d = (Graphics2D) g.create();
            try {
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.drawImage(image, x, y, targetWidth, targetHeight, null);
            } finally {
                g2d.dispose();
            }
        }
    }

    /**
     * Returns the best image for the current scale factor.
     * Uses the following selection strategy:
     * <ol>
     *   <li>Exact match for current scale</li>
     *   <li>Next higher resolution (to downscale)</li>
     *   <li>Highest available (if none higher)</li>
     * </ol>
     */
    private Image getImageForCurrentScale() {
        double scale = scaleManager.getScaleFactor();

        // Check cache
        if (currentImage != null && Math.abs(currentImageScale - scale) < 0.001) {
            return currentImage;
        }

        if (imageVariants.isEmpty()) {
            return null;
        }

        // Try exact match
        Image exact = imageVariants.get(scale);
        if (exact != null) {
            currentImage = exact;
            currentImageScale = scale;
            return exact;
        }

        // Find next higher resolution
        Map.Entry<Double, Image> higher = imageVariants.higherEntry(scale);
        if (higher != null) {
            currentImage = higher.getValue();
            currentImageScale = scale;
            return currentImage;
        }

        // Use highest available (will need to upscale, but better than nothing)
        Map.Entry<Double, Image> highest = imageVariants.lastEntry();
        if (highest != null) {
            currentImage = highest.getValue();
            currentImageScale = scale;
            return currentImage;
        }

        return null;
    }

    /**
     * Creates a scaled BufferedImage from the source image.
     * Uses high-quality scaling algorithms.
     *
     * @param source       the source image
     * @param targetWidth  the target width
     * @param targetHeight the target height
     * @return a new scaled BufferedImage
     */
    public static BufferedImage scaleImage(Image source, int targetWidth, int targetHeight) {
        BufferedImage scaled = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = scaled.createGraphics();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(source, 0, 0, targetWidth, targetHeight, null);
        } finally {
            g2d.dispose();
        }
        return scaled;
    }

    @Override
    public void onScaleChanged(double oldScale, double newScale) {
        // Invalidate cached image so it's reselected on next paint
        currentImage = null;
        currentImageScale = -1;
        logger.debug("ScalableIcon cache invalidated for {}", basePath);
    }

    /**
     * Returns whether any icon variants were successfully loaded.
     *
     * @return true if at least one variant is available
     */
    public boolean hasVariants() {
        return !imageVariants.isEmpty();
    }

    /**
     * Returns the number of loaded variants.
     *
     * @return count of loaded image variants
     */
    public int getVariantCount() {
        return imageVariants.size();
    }

    /**
     * Returns the available scale factors for which variants exist.
     *
     * @return array of available scale factors
     */
    public double[] getAvailableScales() {
        return imageVariants.keySet().stream().mapToDouble(Double::doubleValue).toArray();
    }
}
