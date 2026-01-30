package com.app.responsivejavaapp.util;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Tests for IconGenerator utility.
 * Also used to generate sample icons for the project.
 */
public class IconGeneratorTest {

    private static final String OUTPUT_DIR = "src/main/resources/icons";

    @Test
    public void testGenerateAllIcons() throws IOException {
        // Generate icons
        IconGenerator.generateAllIcons(OUTPUT_DIR);

        // Verify icons were created
        File dir = new File(OUTPUT_DIR);
        assertTrue("Icons directory should exist", dir.exists());
        assertTrue("Icons directory should be a directory", dir.isDirectory());

        // Check that at least some icons exist
        String[] expectedIcons = {"action.png", "action@2x.png", "settings.png", "info.png"};
        for (String icon : expectedIcons) {
            File iconFile = new File(dir, icon);
            assertTrue("Icon should exist: " + icon, iconFile.exists());
            assertTrue("Icon should have content: " + icon, iconFile.length() > 0);
        }
    }

    @Test
    public void testIconResolutions() throws IOException {
        IconGenerator.generateAllIcons(OUTPUT_DIR);

        // Check all resolutions for one icon type
        String[] suffixes = {"", "@1.5x", "@2x", "@3x"};
        for (String suffix : suffixes) {
            File iconFile = new File(OUTPUT_DIR, "action" + suffix + ".png");
            assertTrue("Icon resolution should exist: action" + suffix + ".png", iconFile.exists());
        }
    }
}
