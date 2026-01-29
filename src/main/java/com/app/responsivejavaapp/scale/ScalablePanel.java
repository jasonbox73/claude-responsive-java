package com.app.responsivejavaapp.scale;

import net.miginfocom.swing.MigLayout;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JPanel;
import java.awt.LayoutManager;

/**
 * Base JPanel subclass with built-in DPI scaling support for MigLayout.
 * <p>
 * ScalablePanel automatically:
 * <ul>
 *   <li>Registers as a ScaleChangeListener</li>
 *   <li>Stores original MigLayout constraints for re-scaling</li>
 *   <li>Re-applies scaled constraints when DPI changes</li>
 *   <li>Provides lifecycle hooks for subclass customization</li>
 * </ul>
 * <p>
 * Usage:
 * <pre>
 * public class MyPanel extends ScalablePanel {
 *     public MyPanel() {
 *         super("insets 10", "[grow]", "[grow]");
 *         // Add components...
 *     }
 *
 *     &#64;Override
 *     protected void onScaleChanged(double newScale) {
 *         // Custom handling after scale change
 *     }
 * }
 * </pre>
 */
public class ScalablePanel extends JPanel implements ScaleChangeListener {

    private static final Logger logger = LogManager.getLogger(ScalablePanel.class);

    /** Original (unscaled) layout constraints at 96 DPI */
    private String originalLayoutConstraints;

    /** Original (unscaled) column constraints at 96 DPI */
    private String originalColumnConstraints;

    /** Original (unscaled) row constraints at 96 DPI */
    private String originalRowConstraints;

    /** Scale factor at last layout application */
    private double lastAppliedScale;

    /** Whether this panel is using MigLayout */
    private boolean usingMigLayout;

    /**
     * Creates a ScalablePanel with default FlowLayout.
     */
    public ScalablePanel() {
        super();
        this.usingMigLayout = false;
        registerListener();
    }

    /**
     * Creates a ScalablePanel with the specified layout manager.
     * <p>
     * Note: For automatic constraint scaling, use the MigLayout constructor instead.
     *
     * @param layout the layout manager
     */
    public ScalablePanel(LayoutManager layout) {
        super(layout);
        this.usingMigLayout = layout instanceof MigLayout;
        registerListener();
    }

    /**
     * Creates a ScalablePanel with MigLayout using the specified constraints.
     * The constraints will be automatically scaled based on the current DPI.
     *
     * @param layoutConstraints the layout constraints (e.g., "insets 10, gap 5")
     */
    public ScalablePanel(String layoutConstraints) {
        this(layoutConstraints, "", "");
    }

    /**
     * Creates a ScalablePanel with MigLayout using the specified constraints.
     * The constraints will be automatically scaled based on the current DPI.
     *
     * @param layoutConstraints the layout constraints
     * @param columnConstraints the column constraints
     */
    public ScalablePanel(String layoutConstraints, String columnConstraints) {
        this(layoutConstraints, columnConstraints, "");
    }

    /**
     * Creates a ScalablePanel with MigLayout using the specified constraints.
     * The constraints will be automatically scaled based on the current DPI.
     *
     * @param layoutConstraints the layout constraints
     * @param columnConstraints the column constraints
     * @param rowConstraints    the row constraints
     */
    public ScalablePanel(String layoutConstraints, String columnConstraints, String rowConstraints) {
        super();

        // Store original constraints
        this.originalLayoutConstraints = layoutConstraints != null ? layoutConstraints : "";
        this.originalColumnConstraints = columnConstraints != null ? columnConstraints : "";
        this.originalRowConstraints = rowConstraints != null ? rowConstraints : "";
        this.usingMigLayout = true;

        // Apply scaled layout
        applyScaledLayout();

        registerListener();
    }

    /**
     * Registers this panel as a scale change listener.
     */
    private void registerListener() {
        ScaleManager.getInstance().addScaleChangeListener(this);
        logger.debug("ScalablePanel registered for scale changes");
    }

    /**
     * Applies the MigLayout with scaled constraints.
     */
    private void applyScaledLayout() {
        if (!usingMigLayout) {
            return;
        }

        double currentScale = ScaleManager.getInstance().getScaleFactor();

        String scaledLayout = MigConstraintScaler.scaleLayout(originalLayoutConstraints);
        String scaledColumns = MigConstraintScaler.scaleColumn(originalColumnConstraints);
        String scaledRows = MigConstraintScaler.scaleRow(originalRowConstraints);

        MigLayout layout = new MigLayout(scaledLayout, scaledColumns, scaledRows);
        setLayout(layout);

        lastAppliedScale = currentScale;
        logger.debug("Applied scaled MigLayout at {}x", currentScale);
    }

    @Override
    public void onScaleChanged(double oldScale, double newScale) {
        // Call pre-change hook
        onScaleChanging(newScale);

        // Re-apply scaled layout
        if (usingMigLayout) {
            applyScaledLayout();
        }

        // Revalidate and repaint
        revalidate();
        repaint();

        // Call post-change hook
        onScaleChanged(newScale);

        logger.debug("ScalablePanel responded to scale change: {} -> {}", oldScale, newScale);
    }

    /**
     * Called before the scale change is applied.
     * Override to perform custom pre-scaling actions.
     *
     * @param newScale the new scale factor
     */
    protected void onScaleChanging(double newScale) {
        // Default: no action. Override in subclasses.
    }

    /**
     * Called after the scale change has been applied.
     * Override to perform custom post-scaling actions such as
     * resizing custom-painted components.
     *
     * @param newScale the new scale factor
     */
    protected void onScaleChanged(double newScale) {
        // Default: no action. Override in subclasses.
    }

    /**
     * Returns the original (unscaled) layout constraints.
     *
     * @return the layout constraints at 96 DPI
     */
    public String getOriginalLayoutConstraints() {
        return originalLayoutConstraints;
    }

    /**
     * Returns the original (unscaled) column constraints.
     *
     * @return the column constraints at 96 DPI
     */
    public String getOriginalColumnConstraints() {
        return originalColumnConstraints;
    }

    /**
     * Returns the original (unscaled) row constraints.
     *
     * @return the row constraints at 96 DPI
     */
    public String getOriginalRowConstraints() {
        return originalRowConstraints;
    }

    /**
     * Updates the layout constraints and re-applies scaled layout.
     *
     * @param layoutConstraints the new layout constraints at 96 DPI
     */
    public void setLayoutConstraints(String layoutConstraints) {
        this.originalLayoutConstraints = layoutConstraints != null ? layoutConstraints : "";
        if (usingMigLayout) {
            applyScaledLayout();
            revalidate();
        }
    }

    /**
     * Updates the column constraints and re-applies scaled layout.
     *
     * @param columnConstraints the new column constraints at 96 DPI
     */
    public void setColumnConstraints(String columnConstraints) {
        this.originalColumnConstraints = columnConstraints != null ? columnConstraints : "";
        if (usingMigLayout) {
            applyScaledLayout();
            revalidate();
        }
    }

    /**
     * Updates the row constraints and re-applies scaled layout.
     *
     * @param rowConstraints the new row constraints at 96 DPI
     */
    public void setRowConstraints(String rowConstraints) {
        this.originalRowConstraints = rowConstraints != null ? rowConstraints : "";
        if (usingMigLayout) {
            applyScaledLayout();
            revalidate();
        }
    }

    /**
     * Updates all constraints and re-applies scaled layout.
     *
     * @param layoutConstraints the new layout constraints at 96 DPI
     * @param columnConstraints the new column constraints at 96 DPI
     * @param rowConstraints    the new row constraints at 96 DPI
     */
    public void setAllConstraints(String layoutConstraints, String columnConstraints, String rowConstraints) {
        this.originalLayoutConstraints = layoutConstraints != null ? layoutConstraints : "";
        this.originalColumnConstraints = columnConstraints != null ? columnConstraints : "";
        this.originalRowConstraints = rowConstraints != null ? rowConstraints : "";
        if (usingMigLayout) {
            applyScaledLayout();
            revalidate();
        }
    }

    /**
     * Returns the scale factor that was last applied to this panel's layout.
     *
     * @return the last applied scale factor
     */
    public double getLastAppliedScale() {
        return lastAppliedScale;
    }

    /**
     * Checks if the panel's layout needs to be rescaled.
     *
     * @return true if the current scale differs from the last applied scale
     */
    public boolean needsRescaling() {
        double currentScale = ScaleManager.getInstance().getScaleFactor();
        return Math.abs(currentScale - lastAppliedScale) > 0.001;
    }

    /**
     * Forces a rescale of the layout, even if the scale factor hasn't changed.
     */
    public void forceRescale() {
        if (usingMigLayout) {
            applyScaledLayout();
            revalidate();
            repaint();
        }
    }

    /**
     * Returns whether this panel is using MigLayout with automatic scaling.
     *
     * @return true if using MigLayout
     */
    public boolean isUsingMigLayout() {
        return usingMigLayout;
    }

    /**
     * Convenience method to add a component with scaled constraints.
     * The constraints will be scaled before being applied.
     *
     * @param comp        the component to add
     * @param constraints the MigLayout constraints at 96 DPI
     */
    public void addScaled(java.awt.Component comp, String constraints) {
        String scaledConstraints = MigConstraintScaler.scaleComponent(constraints);
        add(comp, scaledConstraints);
    }

    /**
     * Unregisters this panel from scale change notifications.
     * Call this when the panel is being disposed to prevent memory leaks
     * (though weak references should handle this automatically).
     */
    public void dispose() {
        ScaleManager.getInstance().removeScaleChangeListener(this);
        logger.debug("ScalablePanel disposed and unregistered from scale changes");
    }
}
