package com.app.responsivejavaapp.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Utility class for generating sample multi-resolution icons.
 * <p>
 * Creates simple geometric icons at standard DPI scale factors (1x, 1.5x, 2x, 3x)
 * for testing and demonstration of the ScalableIcon system.
 * <p>
 * Usage:
 * <pre>
 * // Generate all sample icons to resources/icons
 * IconGenerator.generateAllIcons("src/main/resources/icons");
 * </pre>
 */
public final class IconGenerator {

    private static final Logger logger = LogManager.getLogger(IconGenerator.class);

    /** Standard scale factors for icon generation */
    private static final double[] SCALE_FACTORS = {1.0, 1.5, 2.0, 3.0};

    /** Scale factor suffixes for filenames */
    private static final String[] SCALE_SUFFIXES = {"", "@1.5x", "@2x", "@3x"};

    private IconGenerator() {
        // Utility class
    }

    /**
     * Generates all sample icons to the specified directory.
     *
     * @param outputDir the output directory path
     * @throws IOException if icon generation fails
     */
    public static void generateAllIcons(String outputDir) throws IOException {
        Path dir = Path.of(outputDir);
        Files.createDirectories(dir);

        // Generate action button icon (play symbol)
        generateIcon(outputDir, "action", 24, IconGenerator::drawPlaySymbol,
                new Color(0x4CAF50)); // Green

        // Generate settings icon (gear symbol)
        generateIcon(outputDir, "settings", 24, IconGenerator::drawGearSymbol,
                new Color(0x607D8B)); // Blue-grey

        // Generate info icon (i symbol)
        generateIcon(outputDir, "info", 24, IconGenerator::drawInfoSymbol,
                new Color(0x2196F3)); // Blue

        // Generate warning icon (triangle)
        generateIcon(outputDir, "warning", 24, IconGenerator::drawWarningSymbol,
                new Color(0xFF9800)); // Orange

        // Generate error icon (X symbol)
        generateIcon(outputDir, "error", 24, IconGenerator::drawErrorSymbol,
                new Color(0xF44336)); // Red

        logger.info("Generated sample icons in {}", outputDir);
    }

    /**
     * Generates an icon at all standard scale factors.
     *
     * @param outputDir   output directory
     * @param name        base name for the icon
     * @param baseSize    base size at 1x scale
     * @param drawer      function to draw the icon content
     * @param color       primary color for the icon
     * @throws IOException if file writing fails
     */
    public static void generateIcon(String outputDir, String name, int baseSize,
                                    IconDrawer drawer, Color color) throws IOException {
        for (int i = 0; i < SCALE_FACTORS.length; i++) {
            double scale = SCALE_FACTORS[i];
            String suffix = SCALE_SUFFIXES[i];
            int size = (int) Math.round(baseSize * scale);

            BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();

            // Enable antialiasing
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            // Draw the icon
            drawer.draw(g2d, size, color);

            g2d.dispose();

            // Save to file
            String filename = name + suffix + ".png";
            File outputFile = new File(outputDir, filename);
            ImageIO.write(image, "PNG", outputFile);

            logger.debug("Generated icon: {} ({}x{})", filename, size, size);
        }
    }

    /**
     * Functional interface for icon drawing.
     */
    @FunctionalInterface
    public interface IconDrawer {
        void draw(Graphics2D g, int size, Color color);
    }

    /**
     * Draws a play symbol (triangle pointing right).
     */
    private static void drawPlaySymbol(Graphics2D g, int size, Color color) {
        int margin = size / 6;
        int[] xPoints = {margin, size - margin, margin};
        int[] yPoints = {margin, size / 2, size - margin};

        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);
    }

    /**
     * Draws a gear/settings symbol.
     */
    private static void drawGearSymbol(Graphics2D g, int size, Color color) {
        int center = size / 2;
        int outerRadius = size / 2 - size / 8;
        int innerRadius = size / 4;
        int teethCount = 8;

        g.setColor(color);

        // Draw gear teeth as a polygon
        int points = teethCount * 2;
        int[] xPoints = new int[points];
        int[] yPoints = new int[points];

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points - Math.PI / 2;
            int radius = (i % 2 == 0) ? outerRadius : (int) (outerRadius * 0.7);
            xPoints[i] = center + (int) (radius * Math.cos(angle));
            yPoints[i] = center + (int) (radius * Math.sin(angle));
        }

        g.fillPolygon(xPoints, yPoints, points);

        // Draw center hole
        g.setColor(new Color(0, 0, 0, 0)); // Transparent
        g.setComposite(AlphaComposite.Clear);
        g.fillOval(center - innerRadius, center - innerRadius, innerRadius * 2, innerRadius * 2);
        g.setComposite(AlphaComposite.SrcOver);
    }

    /**
     * Draws an info symbol (circle with 'i').
     */
    private static void drawInfoSymbol(Graphics2D g, int size, Color color) {
        int margin = size / 8;
        int diameter = size - margin * 2;

        // Draw circle
        g.setColor(color);
        g.fillOval(margin, margin, diameter, diameter);

        // Draw 'i' in white
        g.setColor(Color.WHITE);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, size / 2);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        String text = "i";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = (size + fm.getAscent() - fm.getDescent()) / 2;
        g.drawString(text, x, y);
    }

    /**
     * Draws a warning symbol (triangle with '!').
     */
    private static void drawWarningSymbol(Graphics2D g, int size, Color color) {
        int margin = size / 8;
        int[] xPoints = {size / 2, size - margin, margin};
        int[] yPoints = {margin, size - margin, size - margin};

        // Draw triangle
        g.setColor(color);
        g.fillPolygon(xPoints, yPoints, 3);

        // Draw '!' in white
        g.setColor(Color.WHITE);
        Font font = new Font(Font.SANS_SERIF, Font.BOLD, size / 3);
        g.setFont(font);
        FontMetrics fm = g.getFontMetrics();
        String text = "!";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = size - margin - size / 6;
        g.drawString(text, x, y);
    }

    /**
     * Draws an error symbol (circle with 'X').
     */
    private static void drawErrorSymbol(Graphics2D g, int size, Color color) {
        int margin = size / 8;
        int diameter = size - margin * 2;

        // Draw circle
        g.setColor(color);
        g.fillOval(margin, margin, diameter, diameter);

        // Draw 'X' in white
        g.setColor(Color.WHITE);
        int strokeWidth = Math.max(2, size / 8);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int offset = size / 4;
        g.drawLine(offset, offset, size - offset, size - offset);
        g.drawLine(size - offset, offset, offset, size - offset);
    }

    /**
     * Main method to generate icons from command line.
     */
    public static void main(String[] args) {
        String outputDir = args.length > 0 ? args[0] : "src/main/resources/icons";
        try {
            generateAllIcons(outputDir);
            System.out.println("Icons generated successfully in: " + outputDir);
        } catch (IOException e) {
            System.err.println("Failed to generate icons: " + e.getMessage());
            System.exit(1);
        }
    }
}
