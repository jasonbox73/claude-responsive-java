package com.app.responsivejavaapp.scale;

import net.miginfocom.swing.MigLayout;
import org.junit.Before;
import org.junit.Test;

import javax.swing.JLabel;
import java.awt.FlowLayout;

import static org.junit.Assert.*;

/**
 * Unit tests for ScalablePanel.
 */
public class ScalablePanelTest {

    @Before
    public void setUp() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
    }

    // ========== Constructor Tests ==========

    @Test
    public void testDefaultConstructor() {
        ScalablePanel panel = new ScalablePanel();
        assertNotNull(panel);
        assertFalse(panel.isUsingMigLayout());
    }

    @Test
    public void testLayoutManagerConstructor() {
        ScalablePanel panel = new ScalablePanel(new FlowLayout());
        assertNotNull(panel);
        assertFalse(panel.isUsingMigLayout());
    }

    @Test
    public void testMigLayoutConstructor_layoutOnly() {
        ScalablePanel panel = new ScalablePanel("insets 10");
        assertNotNull(panel);
        assertTrue(panel.isUsingMigLayout());
        assertEquals("insets 10", panel.getOriginalLayoutConstraints());
        assertEquals("", panel.getOriginalColumnConstraints());
        assertEquals("", panel.getOriginalRowConstraints());
    }

    @Test
    public void testMigLayoutConstructor_layoutAndColumns() {
        ScalablePanel panel = new ScalablePanel("insets 10", "[100][grow]");
        assertTrue(panel.isUsingMigLayout());
        assertEquals("insets 10", panel.getOriginalLayoutConstraints());
        assertEquals("[100][grow]", panel.getOriginalColumnConstraints());
        assertEquals("", panel.getOriginalRowConstraints());
    }

    @Test
    public void testMigLayoutConstructor_allConstraints() {
        ScalablePanel panel = new ScalablePanel("insets 10", "[100][grow]", "[50][]");
        assertTrue(panel.isUsingMigLayout());
        assertEquals("insets 10", panel.getOriginalLayoutConstraints());
        assertEquals("[100][grow]", panel.getOriginalColumnConstraints());
        assertEquals("[50][]", panel.getOriginalRowConstraints());
    }

    @Test
    public void testMigLayoutConstructor_nullConstraints() {
        ScalablePanel panel = new ScalablePanel(null, null, null);
        assertTrue(panel.isUsingMigLayout());
        assertEquals("", panel.getOriginalLayoutConstraints());
        assertEquals("", panel.getOriginalColumnConstraints());
        assertEquals("", panel.getOriginalRowConstraints());
    }

    // ========== Original Constraints Tests ==========

    @Test
    public void testGetOriginalConstraints() {
        ScalablePanel panel = new ScalablePanel("insets 5", "[50]", "[25]");

        assertEquals("insets 5", panel.getOriginalLayoutConstraints());
        assertEquals("[50]", panel.getOriginalColumnConstraints());
        assertEquals("[25]", panel.getOriginalRowConstraints());
    }

    @Test
    public void testOriginalConstraintsUnaffectedByScale() {
        ScalablePanel panel = new ScalablePanel("insets 10", "[100]", "[50]");

        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        panel.onScaleChanged(1.0, 2.0);

        // Original constraints should be unchanged
        assertEquals("insets 10", panel.getOriginalLayoutConstraints());
        assertEquals("[100]", panel.getOriginalColumnConstraints());
        assertEquals("[50]", panel.getOriginalRowConstraints());
    }

    // ========== Constraint Update Tests ==========

    @Test
    public void testSetLayoutConstraints() {
        ScalablePanel panel = new ScalablePanel("insets 10", "", "");
        panel.setLayoutConstraints("insets 20, gap 5");

        assertEquals("insets 20, gap 5", panel.getOriginalLayoutConstraints());
    }

    @Test
    public void testSetColumnConstraints() {
        ScalablePanel panel = new ScalablePanel("", "[100]", "");
        panel.setColumnConstraints("[200][grow]");

        assertEquals("[200][grow]", panel.getOriginalColumnConstraints());
    }

    @Test
    public void testSetRowConstraints() {
        ScalablePanel panel = new ScalablePanel("", "", "[50]");
        panel.setRowConstraints("[100][]");

        assertEquals("[100][]", panel.getOriginalRowConstraints());
    }

    @Test
    public void testSetAllConstraints() {
        ScalablePanel panel = new ScalablePanel("insets 5", "[50]", "[25]");
        panel.setAllConstraints("insets 10", "[100]", "[50]");

        assertEquals("insets 10", panel.getOriginalLayoutConstraints());
        assertEquals("[100]", panel.getOriginalColumnConstraints());
        assertEquals("[50]", panel.getOriginalRowConstraints());
    }

    @Test
    public void testSetConstraints_null() {
        ScalablePanel panel = new ScalablePanel("insets 10", "[100]", "[50]");
        panel.setAllConstraints(null, null, null);

        assertEquals("", panel.getOriginalLayoutConstraints());
        assertEquals("", panel.getOriginalColumnConstraints());
        assertEquals("", panel.getOriginalRowConstraints());
    }

    // ========== Scale Change Tests ==========

    @Test
    public void testScaleChangeUpdatesLayout() {
        ScalablePanel panel = new ScalablePanel("insets 10", "[100]", "[50]");

        // Verify panel uses MigLayout
        assertTrue(panel.getLayout() instanceof MigLayout);

        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        panel.onScaleChanged(1.0, 2.0);

        // Panel should still have MigLayout
        assertTrue(panel.getLayout() instanceof MigLayout);
    }

    @Test
    public void testLastAppliedScale() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
        ScalablePanel panel = new ScalablePanel("insets 10");

        assertEquals(1.0, panel.getLastAppliedScale(), 0.001);

        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        panel.onScaleChanged(1.0, 2.0);

        assertEquals(2.0, panel.getLastAppliedScale(), 0.001);
    }

    @Test
    public void testNeedsRescaling() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
        ScalablePanel panel = new ScalablePanel("insets 10");

        assertFalse(panel.needsRescaling());

        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        // Panel hasn't been notified yet
        assertTrue(panel.needsRescaling());

        panel.onScaleChanged(1.0, 2.0);
        assertFalse(panel.needsRescaling());
    }

    // ========== Force Rescale Test ==========

    @Test
    public void testForceRescale() {
        ScalablePanel panel = new ScalablePanel("insets 10");
        double initialScale = panel.getLastAppliedScale();

        // Force rescale without changing scale factor
        panel.forceRescale();

        // Should still be same scale but no error should occur
        assertEquals(initialScale, panel.getLastAppliedScale(), 0.001);
    }

    // ========== AddScaled Test ==========

    @Test
    public void testAddScaled() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        ScalablePanel panel = new ScalablePanel("", "[grow]", "");

        JLabel label = new JLabel("Test");
        // This should scale "width 100" to "width 200" at 2x
        panel.addScaled(label, "width 100");

        assertEquals(1, panel.getComponentCount());
    }

    // ========== Dispose Test ==========

    @Test
    public void testDispose() {
        ScalablePanel panel = new ScalablePanel("insets 10");

        // Should not throw
        panel.dispose();

        // Panel can still be used after dispose
        assertTrue(panel.isUsingMigLayout());
    }

    // ========== Non-MigLayout Panel Tests ==========

    @Test
    public void testNonMigLayout_scaleChange() {
        ScalablePanel panel = new ScalablePanel(new FlowLayout());

        // Scale change should not throw for non-MigLayout panels
        panel.onScaleChanged(1.0, 2.0);

        assertFalse(panel.isUsingMigLayout());
    }

    @Test
    public void testNonMigLayout_setConstraints() {
        ScalablePanel panel = new ScalablePanel(new FlowLayout());

        // Setting constraints on non-MigLayout panel should not throw
        panel.setLayoutConstraints("insets 10");

        // But it won't be using MigLayout
        assertFalse(panel.isUsingMigLayout());
    }

    // ========== Lifecycle Hook Tests ==========

    @Test
    public void testScaleChangeLifecycleHooks() {
        final boolean[] changing = {false};
        final boolean[] changed = {false};
        final double[] receivedScale = {0};

        ScalablePanel panel = new ScalablePanel("insets 10") {
            @Override
            protected void onScaleChanging(double newScale) {
                changing[0] = true;
                receivedScale[0] = newScale;
            }

            @Override
            protected void onScaleChanged(double newScale) {
                changed[0] = true;
                assertEquals(receivedScale[0], newScale, 0.001);
            }
        };

        panel.onScaleChanged(1.0, 2.0);

        assertTrue("onScaleChanging should be called", changing[0]);
        assertTrue("onScaleChanged should be called", changed[0]);
        assertEquals(2.0, receivedScale[0], 0.001);
    }

    @Test
    public void testScaleChangeHooksCalledInOrder() {
        final StringBuilder order = new StringBuilder();

        ScalablePanel panel = new ScalablePanel("insets 10") {
            @Override
            protected void onScaleChanging(double newScale) {
                order.append("changing,");
            }

            @Override
            protected void onScaleChanged(double newScale) {
                order.append("changed");
            }
        };

        panel.onScaleChanged(1.0, 2.0);

        assertEquals("changing,changed", order.toString());
    }
}
