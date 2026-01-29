package com.app.responsivejavaapp.scale;

import org.junit.Before;
import org.junit.Test;

import java.awt.*;

import static org.junit.Assert.*;

/**
 * Unit tests for ScaleManager.
 * Tests scaling calculations, listener management, and edge cases.
 */
public class ScaleManagerTest {

    private ScaleManager scaleManager;

    @Before
    public void setUp() {
        scaleManager = ScaleManager.getInstance();
        // Reset to known state for testing
        scaleManager.setScaleFactorForTesting(1.0);
    }

    // ========== Singleton Tests ==========

    @Test
    public void testSingletonInstance() {
        ScaleManager instance1 = ScaleManager.getInstance();
        ScaleManager instance2 = ScaleManager.getInstance();
        assertSame("getInstance should return same instance", instance1, instance2);
    }

    // ========== Scale Factor Tests ==========

    @Test
    public void testGetScaleFactor_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(1.0, scaleManager.getScaleFactor(), 0.001);
    }

    @Test
    public void testGetScaleFactor_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(2.0, scaleManager.getScaleFactor(), 0.001);
    }

    @Test
    public void testGetCurrentDpi_at96() {
        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(96, scaleManager.getCurrentDpi());
    }

    @Test
    public void testGetCurrentDpi_at192() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(192, scaleManager.getCurrentDpi());
    }

    @Test
    public void testGetCurrentDpi_at144() {
        scaleManager.setScaleFactorForTesting(1.5);
        assertEquals(144, scaleManager.getCurrentDpi());
    }

    // ========== Integer Scaling Tests ==========

    @Test
    public void testScaleInt_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(100, scaleManager.scale(100));
        assertEquals(0, scaleManager.scale(0));
        assertEquals(1, scaleManager.scale(1));
    }

    @Test
    public void testScaleInt_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(200, scaleManager.scale(100));
        assertEquals(20, scaleManager.scale(10));
        assertEquals(0, scaleManager.scale(0));
    }

    @Test
    public void testScaleInt_at1_5x() {
        scaleManager.setScaleFactorForTesting(1.5);
        assertEquals(150, scaleManager.scale(100));
        assertEquals(15, scaleManager.scale(10));
        // Test rounding: 5 * 1.5 = 7.5 -> 8
        assertEquals(8, scaleManager.scale(5));
    }

    @Test
    public void testScaleInt_at1_25x() {
        scaleManager.setScaleFactorForTesting(1.25);
        assertEquals(125, scaleManager.scale(100));
        // 10 * 1.25 = 12.5 -> 13
        assertEquals(13, scaleManager.scale(10));
    }

    @Test
    public void testScaleInt_rounding() {
        scaleManager.setScaleFactorForTesting(1.5);
        // 3 * 1.5 = 4.5 -> 5 (rounds up)
        assertEquals(5, scaleManager.scale(3));
        // 7 * 1.5 = 10.5 -> 11 (rounds up)
        assertEquals(11, scaleManager.scale(7));
    }

    // ========== Double Scaling Tests ==========

    @Test
    public void testScaleDouble_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(100.0, scaleManager.scale(100.0), 0.001);
    }

    @Test
    public void testScaleDouble_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(200.0, scaleManager.scale(100.0), 0.001);
        assertEquals(25.0, scaleManager.scale(12.5), 0.001);
    }

    @Test
    public void testScaleDouble_at1_5x() {
        scaleManager.setScaleFactorForTesting(1.5);
        assertEquals(150.0, scaleManager.scale(100.0), 0.001);
        assertEquals(7.5, scaleManager.scale(5.0), 0.001);
    }

    // ========== Float Scaling Tests ==========

    @Test
    public void testScaleFloat_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(200.0f, scaleManager.scale(100.0f), 0.001f);
    }

    // ========== Dimension Scaling Tests ==========

    @Test
    public void testScaleDimension_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        Dimension dim = new Dimension(200, 150);
        Dimension scaled = scaleManager.scale(dim);
        assertEquals(200, scaled.width);
        assertEquals(150, scaled.height);
    }

    @Test
    public void testScaleDimension_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        Dimension dim = new Dimension(200, 150);
        Dimension scaled = scaleManager.scale(dim);
        assertEquals(400, scaled.width);
        assertEquals(300, scaled.height);
    }

    @Test
    public void testScaleDimension_at1_5x() {
        scaleManager.setScaleFactorForTesting(1.5);
        Dimension dim = new Dimension(200, 100);
        Dimension scaled = scaleManager.scale(dim);
        assertEquals(300, scaled.width);
        assertEquals(150, scaled.height);
    }

    @Test
    public void testScaleDimension_preservesAspectRatio() {
        scaleManager.setScaleFactorForTesting(2.0);
        Dimension dim = new Dimension(160, 90); // 16:9
        Dimension scaled = scaleManager.scale(dim);
        // Both should scale by same factor
        double originalRatio = (double) dim.width / dim.height;
        double scaledRatio = (double) scaled.width / scaled.height;
        assertEquals(originalRatio, scaledRatio, 0.01);
    }

    @Test
    public void testScaleDimension_null() {
        assertNull(scaleManager.scale((Dimension) null));
    }

    // ========== Insets Scaling Tests ==========

    @Test
    public void testScaleInsets_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        Insets insets = new Insets(10, 20, 10, 20);
        Insets scaled = scaleManager.scale(insets);
        assertEquals(10, scaled.top);
        assertEquals(20, scaled.left);
        assertEquals(10, scaled.bottom);
        assertEquals(20, scaled.right);
    }

    @Test
    public void testScaleInsets_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        Insets insets = new Insets(5, 10, 5, 10);
        Insets scaled = scaleManager.scale(insets);
        assertEquals(10, scaled.top);
        assertEquals(20, scaled.left);
        assertEquals(10, scaled.bottom);
        assertEquals(20, scaled.right);
    }

    @Test
    public void testScaleInsets_null() {
        assertNull(scaleManager.scale((Insets) null));
    }

    // ========== Rectangle Scaling Tests ==========

    @Test
    public void testScaleRectangle_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        Rectangle rect = new Rectangle(10, 20, 100, 50);
        Rectangle scaled = scaleManager.scale(rect);
        assertEquals(20, scaled.x);
        assertEquals(40, scaled.y);
        assertEquals(200, scaled.width);
        assertEquals(100, scaled.height);
    }

    @Test
    public void testScaleRectangle_null() {
        assertNull(scaleManager.scale((Rectangle) null));
    }

    // ========== Point Scaling Tests ==========

    @Test
    public void testScalePoint_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        Point point = new Point(50, 100);
        Point scaled = scaleManager.scale(point);
        assertEquals(100, scaled.x);
        assertEquals(200, scaled.y);
    }

    @Test
    public void testScalePoint_null() {
        assertNull(scaleManager.scale((Point) null));
    }

    // ========== Unscale Tests ==========

    @Test
    public void testUnscaleInt_at1x() {
        scaleManager.setScaleFactorForTesting(1.0);
        assertEquals(100, scaleManager.unscale(100));
    }

    @Test
    public void testUnscaleInt_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(100, scaleManager.unscale(200));
        assertEquals(50, scaleManager.unscale(100));
    }

    @Test
    public void testUnscaleDouble_at2x() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(100.0, scaleManager.unscale(200.0), 0.001);
        assertEquals(50.0, scaleManager.unscale(100.0), 0.001);
    }

    @Test
    public void testScaleUnscale_roundTrip() {
        scaleManager.setScaleFactorForTesting(1.5);
        int original = 100;
        int scaled = scaleManager.scale(original);
        int unscaled = scaleManager.unscale(scaled);
        assertEquals(original, unscaled);
    }

    // ========== Listener Tests ==========

    @Test
    public void testAddScaleChangeListener() {
        int initialCount = scaleManager.getListenerCount();
        TestListener listener = new TestListener();
        scaleManager.addScaleChangeListener(listener);
        assertEquals(initialCount + 1, scaleManager.getListenerCount());
        // Cleanup
        scaleManager.removeScaleChangeListener(listener);
    }

    @Test
    public void testAddScaleChangeListener_null() {
        int initialCount = scaleManager.getListenerCount();
        scaleManager.addScaleChangeListener(null);
        assertEquals(initialCount, scaleManager.getListenerCount());
    }

    @Test
    public void testAddScaleChangeListener_duplicate() {
        int initialCount = scaleManager.getListenerCount();
        TestListener listener = new TestListener();
        scaleManager.addScaleChangeListener(listener);
        scaleManager.addScaleChangeListener(listener); // Add again
        assertEquals(initialCount + 1, scaleManager.getListenerCount()); // Should still be 1
        // Cleanup
        scaleManager.removeScaleChangeListener(listener);
    }

    @Test
    public void testRemoveScaleChangeListener() {
        TestListener listener = new TestListener();
        scaleManager.addScaleChangeListener(listener);
        int countAfterAdd = scaleManager.getListenerCount();
        scaleManager.removeScaleChangeListener(listener);
        assertEquals(countAfterAdd - 1, scaleManager.getListenerCount());
    }

    @Test
    public void testRemoveScaleChangeListener_null() {
        int initialCount = scaleManager.getListenerCount();
        scaleManager.removeScaleChangeListener(null);
        assertEquals(initialCount, scaleManager.getListenerCount());
    }

    @Test
    public void testListenerNotification() {
        TestListener listener = new TestListener();
        scaleManager.addScaleChangeListener(listener);

        scaleManager.setScaleFactorForTesting(1.0);
        scaleManager.updateScaleFactor(2.0);

        assertTrue("Listener should be notified", listener.wasNotified);
        assertEquals(1.0, listener.oldScale, 0.001);
        assertEquals(2.0, listener.newScale, 0.001);

        // Cleanup
        scaleManager.removeScaleChangeListener(listener);
    }

    @Test
    public void testListenerNotNotified_whenScaleUnchanged() {
        scaleManager.setScaleFactorForTesting(1.5);
        TestListener listener = new TestListener();
        scaleManager.addScaleChangeListener(listener);

        scaleManager.updateScaleFactor(1.5); // Same scale

        assertFalse("Listener should not be notified when scale unchanged", listener.wasNotified);

        // Cleanup
        scaleManager.removeScaleChangeListener(listener);
    }

    // ========== Update Scale Factor Tests ==========

    @Test
    public void testUpdateScaleFactor_clampsMin() {
        scaleManager.updateScaleFactor(0.5); // Below MIN_SCALE
        assertEquals(ScaleManager.MIN_SCALE, scaleManager.getScaleFactor(), 0.001);
    }

    @Test
    public void testUpdateScaleFactor_clampsMax() {
        scaleManager.updateScaleFactor(5.0); // Above MAX_SCALE
        assertEquals(ScaleManager.MAX_SCALE, scaleManager.getScaleFactor(), 0.001);
    }

    @Test
    public void testUpdateScaleFactor_validValue() {
        scaleManager.updateScaleFactor(1.75);
        assertEquals(1.75, scaleManager.getScaleFactor(), 0.001);
    }

    // ========== Constants Tests ==========

    @Test
    public void testBaseDpi() {
        assertEquals(96, ScaleManager.BASE_DPI);
    }

    @Test
    public void testMinScale() {
        assertEquals(0.75, ScaleManager.MIN_SCALE, 0.001);
    }

    @Test
    public void testMaxScale() {
        assertEquals(3.0, ScaleManager.MAX_SCALE, 0.001);
    }

    // ========== Edge Cases ==========

    @Test
    public void testScaleZero() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(0, scaleManager.scale(0));
    }

    @Test
    public void testScaleNegative() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(-200, scaleManager.scale(-100));
    }

    @Test
    public void testScaleLargeValue() {
        scaleManager.setScaleFactorForTesting(2.0);
        assertEquals(20000, scaleManager.scale(10000));
    }

    // ========== Helper Classes ==========

    /**
     * Test implementation of ScaleChangeListener for verifying notifications.
     */
    private static class TestListener implements ScaleChangeListener {
        boolean wasNotified = false;
        double oldScale;
        double newScale;

        @Override
        public void onScaleChanged(double oldScale, double newScale) {
            this.wasNotified = true;
            this.oldScale = oldScale;
            this.newScale = newScale;
        }
    }
}
