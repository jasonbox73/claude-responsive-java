package com.app.responsivejavaapp.scale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for transforming MigLayout constraint strings to scale pixel-based values.
 * <p>
 * MigLayout constraints fall into two categories:
 * <ul>
 *   <li><b>DPI-Resilient</b> (unchanged): percentages, grow, shrink, fill, push</li>
 *   <li><b>DPI-Sensitive</b> (scaled): pixel gaps, insets, absolute sizes, min/max constraints</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * String scaled = MigConstraintScaler.scaleLayout("insets 10, gap 5");
 * // At 2x scale: "insets 20, gap 10"
 * </pre>
 */
public final class MigConstraintScaler {

    private static final Logger logger = LogManager.getLogger(MigConstraintScaler.class);

    // Pattern for numeric values with optional unit (captures: value, optional unit)
    // Matches: 10, 10px, 10pt, 10!, 10:20:30, etc.
    private static final Pattern NUMERIC_PATTERN = Pattern.compile(
            "(\\d+(?:\\.\\d+)?)(px|pt|!|:)?");

    // Pattern for gap constraints: gap, gapx, gapy, gaptop, gapbottom, gapleft, gapright
    // followed by 1-2 numeric values
    private static final Pattern GAP_PATTERN = Pattern.compile(
            "\\b(gap(?:x|y|top|bottom|left|right|before|after)?)\\s+(\\d+(?:\\.\\d+)?(?:\\s+\\d+(?:\\.\\d+)?)?)");

    // Pattern for insets: insets followed by 1-4 numeric values
    private static final Pattern INSETS_PATTERN = Pattern.compile(
            "\\b(insets)\\s+(\\d+(?:\\.\\d+)?(?:\\s+\\d+(?:\\.\\d+)?){0,3})");

    // Pattern for size constraints: width, height, w, h, wmin, wmax, hmin, hmax
    // Handles: width 100, width 100!, width 100:150:200, w 100::200, width 50%
    private static final Pattern SIZE_PATTERN = Pattern.compile(
            "\\b(width|height|wmin|wmax|hmin|hmax|w|h)\\s+([\\d:!.%]+)");

    // Pattern for padding: pad followed by 1-4 numeric values
    private static final Pattern PAD_PATTERN = Pattern.compile(
            "\\b(pad)\\s+(\\d+(?:\\.\\d+)?(?:\\s+\\d+(?:\\.\\d+)?){0,3})");

    // Pattern for min/max constraints in column/row specs: [min:pref:max] or [size!]
    private static final Pattern BRACKET_SIZE_PATTERN = Pattern.compile(
            "\\[(\\d+(?::\\d+)*!?)\\]");

    // Pattern to detect percentage values (should NOT be scaled)
    private static final Pattern PERCENT_PATTERN = Pattern.compile("\\d+(?:\\.\\d+)?%");

    // Pattern for split constraint
    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\bsplit\\s+(\\d+)");

    // Pattern for span constraint
    private static final Pattern SPAN_PATTERN = Pattern.compile("\\bspan(?:x|y)?\\s+(\\d+)(?:\\s+(\\d+))?");

    private MigConstraintScaler() {
        // Utility class - no instantiation
    }

    /**
     * Scales all pixel-based values in a MigLayout layout constraint string.
     * <p>
     * Example: "insets 10 20, gap 5" at 2x becomes "insets 20 40, gap 10"
     *
     * @param constraints the layout constraints string
     * @return the scaled constraints string
     */
    public static String scaleLayout(String constraints) {
        return scaleConstraints(constraints);
    }

    /**
     * Scales all pixel-based values in a MigLayout column constraint string.
     * <p>
     * Example: "[100][grow, fill]" at 2x becomes "[200][grow, fill]"
     *
     * @param constraints the column constraints string
     * @return the scaled constraints string
     */
    public static String scaleColumn(String constraints) {
        return scaleConstraints(constraints);
    }

    /**
     * Scales all pixel-based values in a MigLayout row constraint string.
     *
     * @param constraints the row constraints string
     * @return the scaled constraints string
     */
    public static String scaleRow(String constraints) {
        return scaleConstraints(constraints);
    }

    /**
     * Scales all pixel-based values in a MigLayout component constraint string.
     * <p>
     * Example: "width 100!, gap 10" at 2x becomes "width 200!, gap 20"
     *
     * @param constraints the component constraints string
     * @return the scaled constraints string
     */
    public static String scaleComponent(String constraints) {
        return scaleConstraints(constraints);
    }

    /**
     * Scales a single constraint value.
     *
     * @param constraint the constraint to scale
     * @return the scaled constraint
     */
    public static String scale(String constraint) {
        return scaleConstraints(constraint);
    }

    /**
     * Core scaling logic - transforms all pixel-based values in a constraint string.
     */
    private static String scaleConstraints(String constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return constraints;
        }

        ScaleManager scaleManager = ScaleManager.getInstance();
        double scaleFactor = scaleManager.getScaleFactor();

        // Skip scaling if at 1x
        if (Math.abs(scaleFactor - 1.0) < 0.001) {
            return constraints;
        }

        String result = constraints;

        // Scale gap constraints
        result = scaleGaps(result, scaleFactor);

        // Scale insets
        result = scaleInsets(result, scaleFactor);

        // Scale size constraints (width, height, etc.)
        result = scaleSizes(result, scaleFactor);

        // Scale padding
        result = scalePadding(result, scaleFactor);

        // Scale bracket size specs [100] or [100:150:200]
        result = scaleBracketSizes(result, scaleFactor);

        logger.debug("Scaled constraints: '{}' -> '{}'", constraints, result);
        return result;
    }

    /**
     * Scales gap constraint values.
     */
    private static String scaleGaps(String constraints, double scaleFactor) {
        Matcher matcher = GAP_PATTERN.matcher(constraints);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String gapType = matcher.group(1);
            String values = matcher.group(2);
            String scaledValues = scaleSpaceSeparatedValues(values, scaleFactor);
            matcher.appendReplacement(sb, gapType + " " + scaledValues);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Scales insets constraint values.
     */
    private static String scaleInsets(String constraints, double scaleFactor) {
        Matcher matcher = INSETS_PATTERN.matcher(constraints);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String values = matcher.group(2);
            String scaledValues = scaleSpaceSeparatedValues(values, scaleFactor);
            matcher.appendReplacement(sb, "insets " + scaledValues);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Scales size constraint values (width, height, wmin, wmax, etc.).
     */
    private static String scaleSizes(String constraints, double scaleFactor) {
        Matcher matcher = SIZE_PATTERN.matcher(constraints);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String sizeType = matcher.group(1);
            String value = matcher.group(2);

            // Check if this is a percentage - don't scale
            if (value.contains("%")) {
                continue;
            }

            String scaledValue = scaleRangeValue(value, scaleFactor);
            matcher.appendReplacement(sb, sizeType + " " + scaledValue);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Scales padding constraint values.
     */
    private static String scalePadding(String constraints, double scaleFactor) {
        Matcher matcher = PAD_PATTERN.matcher(constraints);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String values = matcher.group(2);
            String scaledValues = scaleSpaceSeparatedValues(values, scaleFactor);
            matcher.appendReplacement(sb, "pad " + scaledValues);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Scales size values within brackets [100] or [100:150:200].
     */
    private static String scaleBracketSizes(String constraints, double scaleFactor) {
        Matcher matcher = BRACKET_SIZE_PATTERN.matcher(constraints);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String content = matcher.group(1);

            // Skip if it looks like a percentage or contains non-numeric chars other than : and !
            if (content.contains("%") || content.matches(".*[a-zA-Z].*")) {
                continue;
            }

            String scaledContent = scaleRangeValue(content, scaleFactor);
            matcher.appendReplacement(sb, "[" + scaledContent + "]");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Scales space-separated numeric values.
     * Example: "10 20 10 20" at 2x becomes "20 40 20 40"
     */
    private static String scaleSpaceSeparatedValues(String values, double scaleFactor) {
        String[] parts = values.trim().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(scaleNumericValue(parts[i], scaleFactor));
        }

        return result.toString();
    }

    /**
     * Scales a range value like "100:150:200" or "100!" or just "100".
     */
    private static String scaleRangeValue(String value, double scaleFactor) {
        // Handle the "!" suffix (forced size)
        boolean hasForce = value.endsWith("!");
        String workValue = hasForce ? value.substring(0, value.length() - 1) : value;

        // Handle range format min:pref:max
        if (workValue.contains(":")) {
            String[] parts = workValue.split(":");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (i > 0) {
                    result.append(":");
                }
                if (!parts[i].isEmpty()) {
                    result.append(scaleNumericValue(parts[i], scaleFactor));
                }
            }
            if (hasForce) {
                result.append("!");
            }
            return result.toString();
        }

        // Simple value
        String scaled = scaleNumericValue(workValue, scaleFactor);
        return hasForce ? scaled + "!" : scaled;
    }

    /**
     * Scales a single numeric value.
     */
    private static String scaleNumericValue(String value, double scaleFactor) {
        if (value == null || value.isEmpty()) {
            return value;
        }

        // Don't scale percentages
        if (value.endsWith("%")) {
            return value;
        }

        // Extract numeric part and optional suffix
        String suffix = "";
        String numPart = value;

        if (value.endsWith("px") || value.endsWith("pt")) {
            suffix = value.substring(value.length() - 2);
            numPart = value.substring(0, value.length() - 2);
        }

        try {
            double num = Double.parseDouble(numPart);
            int scaled = (int) Math.round(num * scaleFactor);
            return scaled + suffix;
        } catch (NumberFormatException e) {
            // Not a number, return as-is
            return value;
        }
    }

    /**
     * Checks if a constraint string contains any pixel-based values that would be scaled.
     *
     * @param constraints the constraint string to check
     * @return true if the string contains scalable values
     */
    public static boolean hasScalableValues(String constraints) {
        if (constraints == null || constraints.isEmpty()) {
            return false;
        }

        return GAP_PATTERN.matcher(constraints).find()
                || INSETS_PATTERN.matcher(constraints).find()
                || SIZE_PATTERN.matcher(constraints).find()
                || PAD_PATTERN.matcher(constraints).find()
                || BRACKET_SIZE_PATTERN.matcher(constraints).find();
    }

    /**
     * Returns the current scale factor being used for transformations.
     *
     * @return the current scale factor
     */
    public static double getCurrentScaleFactor() {
        return ScaleManager.getInstance().getScaleFactor();
    }
}
