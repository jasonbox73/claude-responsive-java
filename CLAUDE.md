# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Compile
mvn compile

# Run tests
mvn test

# Run single test class
mvn test -Dtest=ScaleManagerTest

# Run single test method
mvn test -Dtest=ScaleManagerTest#testGetScaleFactor

# Package
mvn package

# Run application
mvn exec:java
```

## JVM Arguments for DPI Scaling

Required for DPI-aware rendering:
```
-Dsun.java2d.uiScale.enabled=true
-Dswing.aatext=true
-Dawt.useSystemAAFontSettings=on
-Dsun.java2d.opengl=true  # Linux only
```

## Architecture

This project implements DPI-awareness and multi-monitor scaling for a Java Swing application.

### Core Components

| Component | Purpose |
|-----------|---------|
| `ScaleManager` | Singleton. Central scaling calculations, listener management, scale factor detection |
| `FontManager` | Singleton. Logical font sizes (TINY through HUGE), scales fonts based on current DPI |
| `ScalableIcon` | Multi-resolution icon wrapper, selects best resolution for current scale |
| `MigConstraintScaler` | Utility. Regex-based transformation of MigLayout pixel constraints |
| `ScalablePanel` | Base JPanel for DPI-aware layouts with automatic constraint scaling |
| `ScaleChangeListener` | Observer interface. Notified when DPI/scale factor changes |
| `WindowScaleMonitor` | Detects window movement between monitors with different DPIs |
| `RuntimeDpiMonitor` | Detects OS-level DPI setting changes without restart |
| `LogicalSize` | Enum defining logical font sizes (TINY, SMALL, NORMAL, MEDIUM, LARGE, XLARGE, HUGE) |
| `IconGenerator` | Utility to generate sample multi-resolution icons |

### Design Patterns

- **Singleton**: ScaleManager, FontManager, RuntimeDpiMonitor
- **Observer**: ScaleChangeListener (react to DPI changes)
- **Strategy**: ScalableIcon (resolution selection algorithm)
- **Template Method**: ScalablePanel (scaling lifecycle hooks)

## Usage Examples

### Creating a DPI-Aware Window

```java
public class MyWindow extends JFrame implements ScaleChangeListener {
    private final ScaleManager scaleManager = ScaleManager.getInstance();
    private final FontManager fontManager = FontManager.getInstance();
    private WindowScaleMonitor windowScaleMonitor;

    public MyWindow() {
        // Scale window size
        Dimension size = scaleManager.scale(new Dimension(800, 600));
        setSize(size);

        // Use ScalablePanel for auto-scaled MigLayout
        ScalablePanel panel = new ScalablePanel(
            "insets 10",      // Automatically scaled
            "[grow]",
            "[grow]"
        );
        setContentPane(panel);

        // Register for scale changes
        scaleManager.addScaleChangeListener(this);
        windowScaleMonitor = new WindowScaleMonitor(this);
    }

    @Override
    public void onScaleChanged(double oldScale, double newScale) {
        // Update fonts, icons, etc.
        revalidate();
        repaint();
    }
}
```

### Using Scaled Fonts

```java
FontManager fm = FontManager.getInstance();

// Get fonts by logical size
label.setFont(fm.getFont(LogicalSize.LARGE));
button.setFont(fm.getFont(LogicalSize.NORMAL, Font.BOLD));
codeArea.setFont(fm.getMonospacedFont(LogicalSize.SMALL));
```

### Using Multi-Resolution Icons

```java
// Icons auto-select best resolution for current DPI
ScalableIcon icon = new ScalableIcon("/icons/settings", 24, 24);
button.setIcon(icon);

// At 1x DPI → uses settings.png (24x24)
// At 2x DPI → uses settings@2x.png (48x48)
```

### Scaling MigLayout Constraints Manually

```java
// For existing panels not using ScalablePanel
String scaled = MigConstraintScaler.scaleLayout("insets 10, gap 5");
// At 2x: "insets 20, gap 10"

JPanel panel = new JPanel(new MigLayout(
    MigConstraintScaler.scaleLayout("insets 10"),
    MigConstraintScaler.scaleColumn("[100][grow]"),
    MigConstraintScaler.scaleRow("[][]")
));
```

## Icon Resolutions

Store icons at multiple resolutions in `resources/icons/`:
```
icon-name.png       # 1x (96 DPI baseline)
icon-name@1.5x.png  # 1.5x (144 DPI)
icon-name@2x.png    # 2x (192 DPI)
icon-name@3x.png    # 3x (288 DPI)
```

Generate sample icons:
```bash
mvn test -Dtest=IconGeneratorTest
```

## DPI/Scale Factor Reference

| Scale | DPI | Windows Setting |
|-------|-----|-----------------|
| 1.0x  | 96  | 100% |
| 1.25x | 120 | 125% |
| 1.5x  | 144 | 150% |
| 2.0x  | 192 | 200% |
| 3.0x  | 288 | 300% |

## Runtime DPI Detection

The application automatically responds to:

1. **Multi-monitor movement** - `WindowScaleMonitor` detects when window moves to a monitor with different DPI
2. **OS setting changes** - `RuntimeDpiMonitor` detects when user changes display scaling in OS settings

Both are initialized automatically in `App.java`.

## Project Reference

See `Java_Swing_DPI_Scaling_PRD.pdf` for detailed requirements, implementation phases, and testing strategy.
