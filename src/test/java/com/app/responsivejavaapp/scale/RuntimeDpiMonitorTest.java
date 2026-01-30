package com.app.responsivejavaapp.scale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for RuntimeDpiMonitor.
 */
public class RuntimeDpiMonitorTest {

    private RuntimeDpiMonitor monitor;

    @Before
    public void setUp() {
        monitor = RuntimeDpiMonitor.getInstance();
        // Ensure stopped state before each test
        monitor.stop();
        // Reset scale factor
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
    }

    @After
    public void tearDown() {
        // Clean up after tests
        monitor.stop();
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
    }

    // ========== Singleton Tests ==========

    @Test
    public void testGetInstance_returnsSameInstance() {
        RuntimeDpiMonitor instance1 = RuntimeDpiMonitor.getInstance();
        RuntimeDpiMonitor instance2 = RuntimeDpiMonitor.getInstance();

        assertSame(instance1, instance2);
    }

    @Test
    public void testGetInstance_notNull() {
        assertNotNull(RuntimeDpiMonitor.getInstance());
    }

    // ========== Start/Stop Tests ==========

    @Test
    public void testStart_setsStartedTrue() {
        assertFalse(monitor.isStarted());

        monitor.start();

        assertTrue(monitor.isStarted());
    }

    @Test
    public void testStop_setsStartedFalse() {
        monitor.start();
        assertTrue(monitor.isStarted());

        monitor.stop();

        assertFalse(monitor.isStarted());
    }

    @Test
    public void testStart_canBeCalledMultipleTimes() {
        monitor.start();
        monitor.start();
        monitor.start();

        assertTrue(monitor.isStarted());
    }

    @Test
    public void testStop_canBeCalledMultipleTimes() {
        monitor.start();
        monitor.stop();
        monitor.stop();
        monitor.stop();

        assertFalse(monitor.isStarted());
    }

    @Test
    public void testStop_canBeCalledWithoutStart() {
        // Should not throw
        monitor.stop();

        assertFalse(monitor.isStarted());
    }

    // ========== forceCheck Tests ==========

    @Test
    public void testForceCheck_doesNotThrowWhenStopped() {
        assertFalse(monitor.isStarted());

        // Should not throw even when not started
        monitor.forceCheck();
    }

    @Test
    public void testForceCheck_doesNotThrowWhenStarted() {
        monitor.start();

        // Should not throw
        monitor.forceCheck();
    }

    // ========== Integration Tests ==========

    @Test
    public void testScaleChangeDetection() {
        monitor.start();

        // Track if listener was notified
        final boolean[] notified = {false};
        ScaleChangeListener listener = (oldScale, newScale) -> notified[0] = true;

        ScaleManager.getInstance().addScaleChangeListener(listener);

        try {
            // Manually change scale factor (simulates OS DPI change detection)
            ScaleManager.getInstance().updateScaleFactor(1.5);

            assertTrue("Listener should be notified of scale change", notified[0]);
        } finally {
            ScaleManager.getInstance().removeScaleChangeListener(listener);
        }
    }

    @Test
    public void testStartStop_cycle() {
        // Test multiple start/stop cycles
        for (int i = 0; i < 3; i++) {
            monitor.start();
            assertTrue("Should be started after start()", monitor.isStarted());

            monitor.stop();
            assertFalse("Should be stopped after stop()", monitor.isStarted());
        }
    }
}
