package com.app.responsivejavaapp.scale;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for MigConstraintScaler.
 */
public class MigConstraintScalerTest {

    @Before
    public void setUp() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
    }

    // ========== Null/Empty Handling ==========

    @Test
    public void testScaleLayout_null() {
        assertNull(MigConstraintScaler.scaleLayout(null));
    }

    @Test
    public void testScaleLayout_empty() {
        assertEquals("", MigConstraintScaler.scaleLayout(""));
    }

    @Test
    public void testScaleLayout_at1x() {
        // At 1x scale, constraints should be unchanged
        ScaleManager.getInstance().setScaleFactorForTesting(1.0);
        String constraints = "insets 10, gap 5";
        assertEquals(constraints, MigConstraintScaler.scaleLayout(constraints));
    }

    // ========== Gap Scaling Tests ==========

    @Test
    public void testScaleGap_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gap 20", MigConstraintScaler.scaleLayout("gap 10"));
    }

    @Test
    public void testScaleGapWithTwoValues_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gap 20 10", MigConstraintScaler.scaleLayout("gap 10 5"));
    }

    @Test
    public void testScaleGapx_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gapx 20", MigConstraintScaler.scaleLayout("gapx 10"));
    }

    @Test
    public void testScaleGapy_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gapy 20", MigConstraintScaler.scaleLayout("gapy 10"));
    }

    @Test
    public void testScaleGaptop_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gaptop 20", MigConstraintScaler.scaleLayout("gaptop 10"));
    }

    @Test
    public void testScaleGapbottom_at1_5x() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.5);
        assertEquals("gapbottom 15", MigConstraintScaler.scaleLayout("gapbottom 10"));
    }

    @Test
    public void testScaleGapleft_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gapleft 20", MigConstraintScaler.scaleLayout("gapleft 10"));
    }

    @Test
    public void testScaleGapright_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gapright 20", MigConstraintScaler.scaleLayout("gapright 10"));
    }

    // ========== Insets Scaling Tests ==========

    @Test
    public void testScaleInsets_singleValue_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("insets 20", MigConstraintScaler.scaleLayout("insets 10"));
    }

    @Test
    public void testScaleInsets_twoValues_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("insets 20 40", MigConstraintScaler.scaleLayout("insets 10 20"));
    }

    @Test
    public void testScaleInsets_fourValues_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("insets 10 20 30 40", MigConstraintScaler.scaleLayout("insets 5 10 15 20"));
    }

    @Test
    public void testScaleInsets_at1_5x() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.5);
        assertEquals("insets 15 30 15 30", MigConstraintScaler.scaleLayout("insets 10 20 10 20"));
    }

    // ========== Size Constraint Scaling Tests ==========

    @Test
    public void testScaleWidth_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("width 200", MigConstraintScaler.scaleLayout("width 100"));
    }

    @Test
    public void testScaleWidthForced_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("width 200!", MigConstraintScaler.scaleLayout("width 100!"));
    }

    @Test
    public void testScaleWidthRange_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("width 200:400:600", MigConstraintScaler.scaleLayout("width 100:200:300"));
    }

    @Test
    public void testScaleHeight_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("height 100", MigConstraintScaler.scaleLayout("height 50"));
    }

    @Test
    public void testScaleWmin_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("wmin 200", MigConstraintScaler.scaleLayout("wmin 100"));
    }

    @Test
    public void testScaleWmax_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("wmax 600", MigConstraintScaler.scaleLayout("wmax 300"));
    }

    @Test
    public void testScaleHmin_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("hmin 100", MigConstraintScaler.scaleLayout("hmin 50"));
    }

    @Test
    public void testScaleHmax_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("hmax 400", MigConstraintScaler.scaleLayout("hmax 200"));
    }

    // ========== Padding Scaling Tests ==========

    @Test
    public void testScalePad_singleValue_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("pad 10", MigConstraintScaler.scaleLayout("pad 5"));
    }

    @Test
    public void testScalePad_fourValues_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("pad 10 20 10 20", MigConstraintScaler.scaleLayout("pad 5 10 5 10"));
    }

    // ========== Bracket Size Scaling Tests ==========

    @Test
    public void testScaleBracketSize_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("[200]", MigConstraintScaler.scaleColumn("[100]"));
    }

    @Test
    public void testScaleBracketSizeRange_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("[200:400:600]", MigConstraintScaler.scaleColumn("[100:200:300]"));
    }

    @Test
    public void testScaleBracketSizeForced_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("[200!]", MigConstraintScaler.scaleColumn("[100!]"));
    }

    // ========== Mixed Constraint Tests ==========

    @Test
    public void testScaleMixedConstraints_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "insets 10, gap 5";
        String expected = "insets 20, gap 10";
        assertEquals(expected, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testScaleComplexConstraints_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "insets 5 10 5 10, gap 10 5";
        String expected = "insets 10 20 10 20, gap 20 10";
        assertEquals(expected, MigConstraintScaler.scaleLayout(input));
    }

    // ========== DPI-Resilient Constraints (should NOT change) ==========

    @Test
    public void testPercentagesUnchanged() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "width 50%";
        assertEquals(input, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testGrowUnchanged() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "grow";
        assertEquals(input, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testFillUnchanged() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "fill";
        assertEquals(input, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testPushUnchanged() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "push";
        assertEquals(input, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testWrapUnchanged() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "wrap";
        assertEquals(input, MigConstraintScaler.scaleLayout(input));
    }

    @Test
    public void testGrowFillMixed() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        String input = "[grow, fill]";
        // grow and fill shouldn't be scaled
        assertEquals("[grow, fill]", MigConstraintScaler.scaleColumn(input));
    }

    // ========== Column/Row Constraint Tests ==========

    @Test
    public void testScaleColumn_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("[200][grow, fill]", MigConstraintScaler.scaleColumn("[100][grow, fill]"));
    }

    @Test
    public void testScaleRow_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("[100][grow][]", MigConstraintScaler.scaleRow("[50][grow][]"));
    }

    // ========== Component Constraint Tests ==========

    @Test
    public void testScaleComponent_at2x() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("width 200!, gap 20", MigConstraintScaler.scaleComponent("width 100!, gap 10"));
    }

    // ========== Utility Method Tests ==========

    @Test
    public void testHasScalableValues_true() {
        assertTrue(MigConstraintScaler.hasScalableValues("gap 10"));
        assertTrue(MigConstraintScaler.hasScalableValues("insets 5"));
        assertTrue(MigConstraintScaler.hasScalableValues("width 100"));
        assertTrue(MigConstraintScaler.hasScalableValues("pad 5"));
        assertTrue(MigConstraintScaler.hasScalableValues("[100]"));
    }

    @Test
    public void testHasScalableValues_false() {
        assertFalse(MigConstraintScaler.hasScalableValues(null));
        assertFalse(MigConstraintScaler.hasScalableValues(""));
        assertFalse(MigConstraintScaler.hasScalableValues("grow"));
        assertFalse(MigConstraintScaler.hasScalableValues("fill"));
        assertFalse(MigConstraintScaler.hasScalableValues("wrap"));
    }

    @Test
    public void testGetCurrentScaleFactor() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.5);
        assertEquals(1.5, MigConstraintScaler.getCurrentScaleFactor(), 0.001);
    }

    // ========== Fractional Scaling Tests ==========

    @Test
    public void testScaling_at1_25x() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.25);
        // 10 * 1.25 = 12.5 -> 13
        assertEquals("gap 13", MigConstraintScaler.scaleLayout("gap 10"));
    }

    @Test
    public void testScaling_at1_75x() {
        ScaleManager.getInstance().setScaleFactorForTesting(1.75);
        // 10 * 1.75 = 17.5 -> 18
        assertEquals("gap 18", MigConstraintScaler.scaleLayout("gap 10"));
    }

    // ========== Scale Method Alias Test ==========

    @Test
    public void testScale_alias() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);
        assertEquals("gap 20", MigConstraintScaler.scale("gap 10"));
    }
}
