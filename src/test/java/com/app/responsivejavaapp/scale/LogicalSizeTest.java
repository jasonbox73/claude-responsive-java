package com.app.responsivejavaapp.scale;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for LogicalSize enum.
 */
public class LogicalSizeTest {

    @Test
    public void testTinyBasePointSize() {
        assertEquals(9, LogicalSize.TINY.getBasePointSize());
    }

    @Test
    public void testSmallBasePointSize() {
        assertEquals(10, LogicalSize.SMALL.getBasePointSize());
    }

    @Test
    public void testNormalBasePointSize() {
        assertEquals(12, LogicalSize.NORMAL.getBasePointSize());
    }

    @Test
    public void testMediumBasePointSize() {
        assertEquals(14, LogicalSize.MEDIUM.getBasePointSize());
    }

    @Test
    public void testLargeBasePointSize() {
        assertEquals(16, LogicalSize.LARGE.getBasePointSize());
    }

    @Test
    public void testXLargeBasePointSize() {
        assertEquals(18, LogicalSize.XLARGE.getBasePointSize());
    }

    @Test
    public void testHugeBasePointSize() {
        assertEquals(24, LogicalSize.HUGE.getBasePointSize());
    }

    @Test
    public void testScaledPointSize_at1x() {
        assertEquals(12, LogicalSize.NORMAL.getScaledPointSize(1.0));
        assertEquals(24, LogicalSize.HUGE.getScaledPointSize(1.0));
    }

    @Test
    public void testScaledPointSize_at2x() {
        assertEquals(24, LogicalSize.NORMAL.getScaledPointSize(2.0));
        assertEquals(48, LogicalSize.HUGE.getScaledPointSize(2.0));
    }

    @Test
    public void testScaledPointSize_at1_5x() {
        assertEquals(18, LogicalSize.NORMAL.getScaledPointSize(1.5));
        assertEquals(36, LogicalSize.HUGE.getScaledPointSize(1.5));
    }

    @Test
    public void testScaledPointSize_rounding() {
        // 9 * 1.5 = 13.5 -> 14
        assertEquals(14, LogicalSize.TINY.getScaledPointSize(1.5));
        // 10 * 1.5 = 15.0 -> 15
        assertEquals(15, LogicalSize.SMALL.getScaledPointSize(1.5));
    }

    @Test
    public void testAllValuesExist() {
        LogicalSize[] values = LogicalSize.values();
        assertEquals(7, values.length);
    }

    @Test
    public void testValueOf() {
        assertEquals(LogicalSize.NORMAL, LogicalSize.valueOf("NORMAL"));
        assertEquals(LogicalSize.HUGE, LogicalSize.valueOf("HUGE"));
    }

    @Test
    public void testSizesAreOrdered() {
        assertTrue(LogicalSize.TINY.getBasePointSize() < LogicalSize.SMALL.getBasePointSize());
        assertTrue(LogicalSize.SMALL.getBasePointSize() < LogicalSize.NORMAL.getBasePointSize());
        assertTrue(LogicalSize.NORMAL.getBasePointSize() < LogicalSize.MEDIUM.getBasePointSize());
        assertTrue(LogicalSize.MEDIUM.getBasePointSize() < LogicalSize.LARGE.getBasePointSize());
        assertTrue(LogicalSize.LARGE.getBasePointSize() < LogicalSize.XLARGE.getBasePointSize());
        assertTrue(LogicalSize.XLARGE.getBasePointSize() < LogicalSize.HUGE.getBasePointSize());
    }
}
