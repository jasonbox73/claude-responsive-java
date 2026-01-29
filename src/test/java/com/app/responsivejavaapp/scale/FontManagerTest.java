package com.app.responsivejavaapp.scale;

import org.junit.Before;
import org.junit.Test;

import java.awt.Font;

import static org.junit.Assert.*;

/**
 * Unit tests for FontManager.
 */
public class FontManagerTest {

    private FontManager fontManager;

    @Before
    public void setUp() {
        fontManager = FontManager.getInstance();
        fontManager.clearCache();
    }

    // ========== Singleton Tests ==========

    @Test
    public void testSingletonInstance() {
        FontManager instance1 = FontManager.getInstance();
        FontManager instance2 = FontManager.getInstance();
        assertSame("getInstance should return same instance", instance1, instance2);
    }

    // ========== Basic Font Tests ==========

    @Test
    public void testGetFont_normal() {
        Font font = fontManager.getFont(LogicalSize.NORMAL);
        assertNotNull(font);
        assertEquals(12, font.getSize());
        assertEquals(Font.PLAIN, font.getStyle());
    }

    @Test
    public void testGetFont_tiny() {
        Font font = fontManager.getFont(LogicalSize.TINY);
        assertNotNull(font);
        assertEquals(9, font.getSize());
    }

    @Test
    public void testGetFont_huge() {
        Font font = fontManager.getFont(LogicalSize.HUGE);
        assertNotNull(font);
        assertEquals(24, font.getSize());
    }

    @Test
    public void testGetFont_allSizes() {
        for (LogicalSize size : LogicalSize.values()) {
            Font font = fontManager.getFont(size);
            assertNotNull("Font should exist for " + size, font);
            assertEquals(size.getBasePointSize(), font.getSize());
        }
    }

    // ========== Styled Font Tests ==========

    @Test
    public void testGetFont_bold() {
        Font font = fontManager.getFont(LogicalSize.NORMAL, Font.BOLD);
        assertNotNull(font);
        assertEquals(Font.BOLD, font.getStyle());
        assertEquals(12, font.getSize());
    }

    @Test
    public void testGetFont_italic() {
        Font font = fontManager.getFont(LogicalSize.NORMAL, Font.ITALIC);
        assertNotNull(font);
        assertEquals(Font.ITALIC, font.getStyle());
    }

    @Test
    public void testGetFont_boldItalic() {
        Font font = fontManager.getFont(LogicalSize.NORMAL, Font.BOLD | Font.ITALIC);
        assertNotNull(font);
        assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
    }

    @Test
    public void testGetBoldFont() {
        Font font = fontManager.getBoldFont(LogicalSize.LARGE);
        assertNotNull(font);
        assertEquals(Font.BOLD, font.getStyle());
        assertEquals(16, font.getSize());
    }

    @Test
    public void testGetItalicFont() {
        Font font = fontManager.getItalicFont(LogicalSize.MEDIUM);
        assertNotNull(font);
        assertEquals(Font.ITALIC, font.getStyle());
        assertEquals(14, font.getSize());
    }

    @Test
    public void testGetBoldItalicFont() {
        Font font = fontManager.getBoldItalicFont(LogicalSize.SMALL);
        assertNotNull(font);
        assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
        assertEquals(10, font.getSize());
    }

    // ========== Monospaced Font Tests ==========

    @Test
    public void testGetMonospacedFont() {
        Font font = fontManager.getMonospacedFont(LogicalSize.NORMAL);
        assertNotNull(font);
        assertEquals(Font.MONOSPACED, font.getFamily());
        assertEquals(12, font.getSize());
    }

    @Test
    public void testGetMonospacedFont_styled() {
        Font font = fontManager.getMonospacedFont(LogicalSize.NORMAL, Font.BOLD);
        assertNotNull(font);
        assertEquals(Font.MONOSPACED, font.getFamily());
        assertEquals(Font.BOLD, font.getStyle());
    }

    // ========== Custom Family Tests ==========

    @Test
    public void testGetFont_customFamily() {
        Font font = fontManager.getFont(Font.SERIF, LogicalSize.NORMAL, Font.PLAIN);
        assertNotNull(font);
        assertEquals(Font.SERIF, font.getFamily());
    }

    @Test
    public void testGetFont_nullFamily() {
        Font font = fontManager.getFont(null, LogicalSize.NORMAL, Font.PLAIN);
        assertNotNull(font);
        assertEquals(Font.SANS_SERIF, font.getFamily());
    }

    @Test
    public void testGetFont_emptyFamily() {
        Font font = fontManager.getFont("", LogicalSize.NORMAL, Font.PLAIN);
        assertNotNull(font);
        assertEquals(Font.SANS_SERIF, font.getFamily());
    }

    // ========== Null Size Handling ==========

    @Test
    public void testGetFont_nullSize() {
        Font font = fontManager.getFont(null);
        assertNotNull(font);
        assertEquals(LogicalSize.NORMAL.getBasePointSize(), font.getSize());
    }

    // ========== Cache Tests ==========

    @Test
    public void testFontCaching() {
        Font font1 = fontManager.getFont(LogicalSize.NORMAL);
        Font font2 = fontManager.getFont(LogicalSize.NORMAL);
        assertSame("Same font should be returned from cache", font1, font2);
    }

    @Test
    public void testFontCaching_differentSizes() {
        Font font1 = fontManager.getFont(LogicalSize.NORMAL);
        Font font2 = fontManager.getFont(LogicalSize.LARGE);
        assertNotSame("Different sizes should return different fonts", font1, font2);
    }

    @Test
    public void testFontCaching_differentStyles() {
        Font font1 = fontManager.getFont(LogicalSize.NORMAL, Font.PLAIN);
        Font font2 = fontManager.getFont(LogicalSize.NORMAL, Font.BOLD);
        assertNotSame("Different styles should return different fonts", font1, font2);
    }

    @Test
    public void testClearCache() {
        fontManager.getFont(LogicalSize.NORMAL);
        fontManager.getFont(LogicalSize.LARGE);
        assertTrue(fontManager.getCacheSize() > 0);

        fontManager.clearCache();
        assertEquals(0, fontManager.getCacheSize());
    }

    @Test
    public void testGetCacheSize() {
        fontManager.clearCache();
        assertEquals(0, fontManager.getCacheSize());

        fontManager.getFont(LogicalSize.NORMAL);
        assertEquals(1, fontManager.getCacheSize());

        fontManager.getFont(LogicalSize.LARGE);
        assertEquals(2, fontManager.getCacheSize());

        // Same font shouldn't increase cache size
        fontManager.getFont(LogicalSize.NORMAL);
        assertEquals(2, fontManager.getCacheSize());
    }

    // ========== DeriveScaled Tests ==========

    @Test
    public void testDeriveScaled() {
        ScaleManager.getInstance().setScaleFactorForTesting(2.0);

        Font baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        Font scaled = fontManager.deriveScaled(baseFont);

        assertNotNull(scaled);
        assertEquals(24, scaled.getSize());
        assertEquals(baseFont.getStyle(), scaled.getStyle());
    }

    @Test
    public void testDeriveScaled_null() {
        assertNull(fontManager.deriveScaled(null));
    }

    // ========== Point Size Tests ==========

    @Test
    public void testGetFontByPointSize() {
        Font font = fontManager.getFontByPointSize(Font.SANS_SERIF, 20, Font.BOLD);
        assertNotNull(font);
        assertEquals(20, font.getSize());
        assertEquals(Font.BOLD, font.getStyle());
    }

    // ========== Font Availability Tests ==========

    @Test
    public void testIsFontAvailable_sansSerif() {
        // Sans Serif should always be available as it's a logical font
        assertTrue(fontManager.isFontAvailable(Font.SANS_SERIF));
    }

    @Test
    public void testIsFontAvailable_null() {
        assertFalse(fontManager.isFontAvailable(null));
    }

    @Test
    public void testIsFontAvailable_empty() {
        assertFalse(fontManager.isFontAvailable(""));
    }

    @Test
    public void testGetAvailableFontFamilies() {
        String[] families = fontManager.getAvailableFontFamilies();
        assertNotNull(families);
        assertTrue(families.length > 0);
    }

    // ========== Scale Change Listener Tests ==========

    @Test
    public void testScaleChangeListener() {
        fontManager.getFont(LogicalSize.NORMAL);
        assertTrue(fontManager.getCacheSize() > 0);

        // Simulate scale change
        fontManager.onScaleChanged(1.0, 2.0);

        assertEquals("Cache should be cleared on scale change", 0, fontManager.getCacheSize());
    }
}
