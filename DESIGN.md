# Todo App Design System (Material 3)

This document outlines the design principles, color palettes, and typography used in the Todo App, following the Material 3 (M3) standards.

## 1. Color Strategy

The app uses a dual-palette strategy that supports both brand-specific custom colors and system-adaptive **Dynamic Color** (Android 12+).

### Color Selection Logic
The `TodoTheme` composable selects the color scheme based on the following priority:
1.  **Dynamic Color (API 31+):** If supported, colors are derived from the user's wallpaper for maximum system integration.
2.  **Custom Dark Mode:** If Dynamic Color is unavailable or the user has a system-wide Dark Theme enabled.
3.  **Custom Light Mode:** The default fallback for Light Theme environments.

### Custom Color Palette

| Role | Light Hex | Dark Hex | Description |
| :--- | :--- | :--- | :--- |
| **Primary** | `#0061A4` | `#9ECAFF` | Main brand color for key actions. |
| **On Primary** | `#FFFFFF` | `#003258` | Text/icons on top of Primary. |
| **Primary Container** | `#D1E4FF` | `#00497D` | Prominent background for primary elements. |
| **Secondary** | `#535F70` | `#BBC7DB` | Less prominent actions and accents. |
| **Tertiary** | `#6B5778` | `#D6BEE4` | Contrasting accents for specific features. |
| **Surface** | `#FDFCFF` | `#1A1C1E` | Background for cards, sheets, and menus. |
| **Outline** | `#73777F` | `#8D9199` | Dividers and low-emphasis borders. |
| **Error** | `#BA1A1A` | `#FFFFB4AB` | Destructive actions and error states. |

## 2. Typography

The typography system uses the default system font family but scales across five distinct roles to create a clear information hierarchy.

| Role | Weight | Size | Line Height |
| :--- | :--- | :--- | :--- |
| **Display (L/M/S)** | Normal (W400) | 57 / 45 / 36 sp | 64 / 52 / 44 sp |
| **Headline (L/M/S)**| Normal (W400) | 32 / 28 / 24 sp | 40 / 36 / 32 sp |
| **Title (L/M/S)** | Medium (W500) | 22 / 16 / 14 sp | 28 / 24 / 20 sp |
| **Body (L/M/S)** | Normal (W400) | 16 / 14 / 12 sp | 24 / 20 / 16 sp |
| **Label (L/M/S)** | Medium (W500) | 14 / 12 / 11 sp | 20 / 16 / 16 sp |

*   **Body Large:** Used for main list content and primary text inputs.
*   **Title Medium:** Used for section headers and card titles.
*   **Label Small:** Used for metadata, tags, and tertiary information.

## 3. Shapes & Surfaces

*   **Extra Small (4dp):** Used for Tags and small badges.
*   **Small (8dp):** Used for input fields and small buttons.
*   **Medium (12dp):** Used for Cards and Mars Photo previews.
*   **Large (16dp):** Used for main layout containers.
*   **Extra Large (28dp):** Used for Bottom Sheets and Dialogs.

## 4. Dark Mode Principles

*   **Tonal Elevation:** Instead of shadows, the app uses primary-tinted overlays on surfaces to indicate depth.
*   **Reduced Contrast:** Primary brand colors are desaturated in Dark Mode to reduce eye strain.
*   **Surface Color:** Pure black is avoided in favor of `#1A1C1E` to maintain visibility of shadows and depth indicators.
