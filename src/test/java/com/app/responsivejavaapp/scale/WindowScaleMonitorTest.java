package com.app.responsivejavaapp.scale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;

import static org.junit.Assert.*;

/**
 * Unit tests for WindowScaleMonitor.
 */
public class WindowScaleMonitorTest {

    private JFrame testFrame;
    private WindowScaleMonitor monitor;

    @Before
    public void setUp() {
        // Reset scale factor for consistent tests
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);

        // Create a test frame (not visible)
        testFrame = new JFrame("Test Frame");
        testFrame.setSize(400, 300);
    }

    @After
    public void tearDown() {
        if (monitor != null) {
            monitor.dispose();
            monitor = null;
        }
        if (testFrame != null) {
            testFrame.dispose();
            testFrame = null;
        }
        // Reset scale factor
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
    }

    // ========== Constructor Tests ==========

    @Test(expected = IllegalArgumentException.class)
    public void testConstructor_nullWindow_throwsException() {
        new WindowScaleMonitor(null);
    }

    @Test
    public void testConstructor_validWindow_createsMonitor() {
        monitor = new WindowScaleMonitor(testFrame);

        assertNotNull(monitor);
        assertEquals(testFrame, monitor.getWindow());
    }

    @Test
    public void testConstructor_customDebounce() {
        monitor = new WindowScaleMonitor(testFrame, 200);

        assertNotNull(monitor);
        assertEquals(testFrame, monitor.getWindow());
    }

    // ========== getCurrentMonitorScale Tests ==========

    @Test
    public void testGetCurrentMonitorScale_returnsPositiveValue() {
        monitor = new WindowScaleMonitor(testFrame);

        double scale = monitor.getCurrentMonitorScale();

        assertTrue("Scale should be positive", scale > 0);
        assertTrue("Scale should be >= MIN_SCALE", scale >= ScaleManager.MIN_SCALE);
        assertTrue("Scale should be <= MAX_SCALE", scale <= ScaleManager.MAX_SCALE);
    }

    // ========== getWindow Tests ==========

    @Test
    public void testGetWindow_returnsCorrectWindow() {
        monitor = new WindowScaleMonitor(testFrame);

        assertSame(testFrame, monitor.getWindow());
    }

    // ========== forceCheck Tests ==========

    @Test
    public void testForceCheck_doesNotThrow() {
        monitor = new WindowScaleMonitor(testFrame);

        // Should not throw even when window is not visible
        monitor.forceCheck();
    }

    // ========== dispose Tests ==========

    @Test
    public void testDispose_canBeCalledMultipleTimes() {
        monitor = new WindowScaleMonitor(testFrame);

        // Should not throw even when called multiple times
        monitor.dispose();
        monitor.dispose();
    }

    @Test
    public void testDispose_removesListener() {
        monitor = new WindowScaleMonitor(testFrame);

        // Get listener count before
        int listenersBefore = testFrame.getComponentListeners().length;

        monitor.dispose();

        // Listener count should decrease
        int listenersAfter = testFrame.getComponentListeners().length;
        assertTrue("Component listeners should decrease after dispose",
                listenersAfter < listenersBefore);
    }

    // ========== Integration Tests ==========

    @Test
    public void testWindowMove_triggersCheck() throws Exception {
        monitor = new WindowScaleMonitor(testFrame, 50); // Short debounce for testing

        testFrame.setVisible(true);

        // Move the window
        testFrame.setLocation(100, 100);

        // Wait for debounce
        Thread.sleep(100);

        // Should not throw and monitor should still be functional
        monitor.forceCheck();

        testFrame.setVisible(false);
    }

    @Test
    public void testWindowResize_triggersCheck() throws Exception {
        monitor = new WindowScaleMonitor(testFrame, 50); // Short debounce for testing

        testFrame.setVisible(true);

        // Resize the window
        testFrame.setSize(500, 400);

        // Wait for debounce
        Thread.sleep(100);

        // Should not throw and monitor should still be functional
        monitor.forceCheck();

        testFrame.setVisible(false);
    }

    // ========== Scale Change Notification Tests ==========

    @Test
    public void testScaleChangeListener_receivesNotification() {
        monitor = new WindowScaleMonitor(testFrame);

        // Create a listener to track notifications
        final boolean[] notified = {false};
        final double[] oldScale = {0};
        final double[] newScale = {0};

        ScaleChangeListener listener = (old, newer) -> {
            notified[0] = true;
            oldScale[0] = old;
            newScale[0] = newer;
        };

        ScaleManager.getInstance().addScaleChangeListener(listener);

        try {
            // Manually trigger a scale update (simulates monitor change)
            ScaleManager.getInstance().updateScaleFactor(2.0);

            assertTrue("Listener should be notified", notified[0]);
            assertEquals(1.0, oldScale[0], 0.001);
            assertEquals(2.0, newScale[0], 0.001);
        } finally {
            ScaleManager.getInstance().removeScaleChangeListener(listener);
            ScaleManager.getInstance().setScaleFactorForTesting(1.0);
        }
    }
}
