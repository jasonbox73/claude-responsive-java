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
| `ScalableIcon` | Strategy pattern. Multi-resolution icon wrapper, selects best resolution for current scale |
| `MigConstraintScaler` | Utility. Regex-based transformation of MigLayout pixel constraints |
| `ScalablePanel` | Template method. Base class for DPI-aware panels with lifecycle hooks |
| `ScaleChangeListener` | Observer interface. Notified when DPI/scale factor changes |

### Design Patterns

- **Singleton**: ScaleManager, FontManager (centralized state)
- **Observer**: ScaleChangeListener (react to DPI changes)
- **Strategy**: ScalableIcon (resolution selection algorithm)
- **Template Method**: ScalablePanel (scaling lifecycle)

### Icon Resolutions

Store icons at multiple resolutions:
```
resources/icons/
  icon-name@1x.png    # 96 DPI baseline
  icon-name@1.5x.png  # 144 DPI
  icon-name@2x.png    # 192 DPI
  icon-name@3x.png    # 288 DPI
```

### DPI/Scale Factor Reference

| Scale | DPI | Windows Setting |
|-------|-----|-----------------|
| 1.0x  | 96  | 100% |
| 1.25x | 120 | 125% |
| 1.5x  | 144 | 150% |
| 2.0x  | 192 | 200% |
| 3.0x  | 288 | 300% |

## Project Reference

See `Java_Swing_DPI_Scaling_PRD.pdf` for detailed requirements, implementation phases, and testing strategy.
