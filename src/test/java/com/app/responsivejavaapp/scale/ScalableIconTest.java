package com.app.responsivejavaapp.scale;

import org.junit.Before;
import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

/**
 * Unit tests for ScalableIcon.
 */
public class ScalableIconTest {

    private ScaleManager scaleManager;

    @Before
    public void setUp() {
        scaleManager = ScaleManager.getInstance();
        scaleManager.setScaleFactorForTesting(1.0);
    }

    // ========== Constructor Tests ==========

    @Test
    public void testConstructor_withDimensions() {
        ScalableIcon icon = new ScalableIcon("/icons/nonexistent", 16, 16);
        assertEquals(16, icon.getBaseWidth());
        assertEquals(16, icon.getBaseHeight());
    }

    @Test
    public void testConstructor_autoDetectSize() {
        // When no icons are found, default size should be used
        ScalableIcon icon = new ScalableIcon("/icons/nonexistent");
        assertEquals(16, icon.getBaseWidth());
        assertEquals(16, icon.getBaseHeight());
    }

    // ========== Dimension Tests ==========

    @Test
    public void testGetIconWidth_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        ScalableIcon icon = new ScalableIcon("/icons/test", 24, 24);
        assertEquals(24, icon.getIconWidth());
    }

    @Test
    public void testGetIconWidth_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        ScalableIcon icon = new ScalableIcon("/icons/test", 24, 24);
        assertEquals(48, icon.getIconWidth());
    }

    @Test
    public void testGetIconHeight_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 24);
        assertEquals(24, icon.getIconHeight());
    }

    @Test
    public void testGetIconHeight_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 24);
        assertEquals(48, icon.getIconHeight());
    }

    @Test
    public void testGetIconDimensions_at1_5x() {
        scaleManager.setScaleFactorForTesting(1.5);
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 16);
        assertEquals(24, icon.getIconWidth());
        assertEquals(24, icon.getIconHeight());
    }

    // ========== Base Dimension Tests ==========

    @Test
    public void testGetBaseWidth() {
        ScalableIcon icon = new ScalableIcon("/icons/test", 32, 24);
        assertEquals(32, icon.getBaseWidth());
    }

    @Test
    public void testGetBaseHeight() {
        ScalableIcon icon = new ScalableIcon("/icons/test", 32, 24);
        assertEquals(24, icon.getBaseHeight());
    }

    @Test
    public void testBaseDimensionsUnaffectedByScale() {
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 16);

        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(16, icon.getBaseWidth());
        assertEquals(16, icon.getBaseHeight());

        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(16, icon.getBaseWidth());
        assertEquals(16, icon.getBaseHeight());
    }

    // ========== Variant Tests ==========

    @Test
    public void testHasVariants_noIcons() {
        ScalableIcon icon = new ScalableIcon("/icons/nonexistent", 16, 16);
        assertFalse(icon.hasVariants());
    }

    @Test
    public void testGetVariantCount_noIcons() {
        ScalableIcon icon = new ScalableIcon("/icons/nonexistent", 16, 16);
        assertEquals(0, icon.getVariantCount());
    }

    @Test
    public void testGetAvailableScales_noIcons() {
        ScalableIcon icon = new ScalableIcon("/icons/nonexistent", 16, 16);
        double[] scales = icon.getAvailableScales();
        assertNotNull(scales);
        assertEquals(0, scales.length);
    }

    // ========== Scale Change Listener Tests ==========

    @Test
    public void testScaleChangeInvalidatesCache() {
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 16);

        // Simulate scale change
        icon.onScaleChanged(1.0, 2.0);

        // Icon should handle scale change gracefully
        // (cache invalidation is internal, we just verify no exceptions)
        assertEquals(16, icon.getBaseWidth());
    }

    // ========== Static Utility Tests ==========

    @Test
    public void testScaleImage() {
        BufferedImage source = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        BufferedImage scaled = ScalableIcon.scaleImage(source, 32, 32);

        assertNotNull(scaled);
        assertEquals(32, scaled.getWidth());
        assertEquals(32, scaled.getHeight());
    }

    @Test
    public void testScaleImage_downscale() {
        BufferedImage source = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        BufferedImage scaled = ScalableIcon.scaleImage(source, 16, 16);

        assertNotNull(scaled);
        assertEquals(16, scaled.getWidth());
        assertEquals(16, scaled.getHeight());
    }

    @Test
    public void testScaleImage_nonSquare() {
        BufferedImage source = new BufferedImage(32, 16, BufferedImage.TYPE_INT_ARGB);
        BufferedImage scaled = ScalableIcon.scaleImage(source, 64, 32);

        assertNotNull(scaled);
        assertEquals(64, scaled.getWidth());
        assertEquals(32, scaled.getHeight());
    }

    // ========== Scaling Consistency Tests ==========

    @Test
    public void testScalingConsistency() {
        ScalableIcon icon = new ScalableIcon("/icons/test", 16, 16);

        // At various scales, dimensions should be consistent
        double[] scales = {1.0, 1.25, 1.5, 2.0, 2.5, 3.0};
        for (double scale : scales) {
            scaleManager.setScaleFactorForTesting(scale);
            int expectedSize = (int) Math.round(16 * scale);
            assertEquals("Width at " + scale + "x", expectedSize, icon.getIconWidth());
            assertEquals("Height at " + scale + "x", expectedSize, icon.getIconHeight());
        }
    }
}
