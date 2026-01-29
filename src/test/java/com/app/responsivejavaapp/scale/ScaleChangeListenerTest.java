package com.app.responsivejavaapp.scale;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for ScaleChangeListener interface.
 */
public class ScaleChangeListenerTest {

    @Test
    public void testFunctionalInterface() {
        // ScaleChangeListener is a functional interface, can be used with lambda
        final double[] captured = new double[2];

        ScaleChangeListener listener = (oldScale, newScale) -> {
            captured[0] = oldScale;
            captured[1] = newScale;
        };

        listener.onScaleChanged(1.0, 2.0);

        assertEquals(1.0, captured[0], 0.001);
        assertEquals(2.0, captured[1], 0.001);
    }

    @Test
    public void testMethodReference() {
        TestHandler handler = new TestHandler();

        ScaleChangeListener listener = handler::handleScaleChange;
        listener.onScaleChanged(1.5, 2.5);

        assertTrue(handler.wasCalled);
        assertEquals(1.5, handler.oldScale, 0.001);
        assertEquals(2.5, handler.newScale, 0.001);
    }

    @Test
    public void testAnonymousClass() {
        final boolean[] called = {false};

        ScaleChangeListener listener = new ScaleChangeListener() {
            @Override
            public void onScaleChanged(double oldScale, double newScale) {
                called[0] = true;
            }
        };

        listener.onScaleChanged(1.0, 1.5);
        assertTrue(called[0]);
    }

    private static class TestHandler {
        boolean wasCalled = false;
        double oldScale;
        double newScale;

        void handleScaleChange(double oldScale, double newScale) {
            this.wasCalled = true;
            this.oldScale = oldScale;
            this.newScale = newScale;
        }
    }
}
