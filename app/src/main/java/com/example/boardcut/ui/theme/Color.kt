package com.example.boardcut.ui.theme

import androidx.compose.ui.graphics.Color

// Woodworking Brand Palette
val WoodPrimary = Color(0xFF8B4513)        // Saddle Brown / Warm Timber
val WoodPrimaryDark = Color(0xFFD49B6A)    // Warm Oak for dark mode
val WoodSecondary = Color(0xFFB45309)      // Amber Wood
val WoodTertiary = Color(0xFF5D4037)       // Deep Walnut

// Neutral - Dark Mode (Workshop Charcoal)
val WorkshopCharcoal = Color(0xFF141618)
val WorkshopSurfaceDark = Color(0xFF1D2024)
val WorkshopCardDark = Color(0xFF262A2E)
val WorkshopBorderDark = Color(0xFF383D43)
val TextPrimaryDark = Color(0xFFECEFF1)
val TextSecondaryDark = Color(0xFFA0A6AC)

// Neutral - Light Mode (Warm White / Maple Workshop)
val WarmWhiteBg = Color(0xFFF9F7F3)
val WarmWhiteSurface = Color(0xFFFFFFFF)
val WarmWhiteCard = Color(0xFFF2ECE4)
val WarmWhiteBorder = Color(0xFFDFD7CB)
val TextPrimaryLight = Color(0xFF1E2124)
val TextSecondaryLight = Color(0xFF6B7280)

// Visual Diagram Board & Cut Colors
val KerfCutRed = Color(0xFFE53935)
val TrimAllowanceOrange = Color(0xFFF57C00)
val ReusableOffcutGreen = Color(0xFF2E7D32)
val ScrapWasteSlate = Color(0xFF78909C)

// Distinct Piece Fill Colors for Diagrams
val PieceColors = listOf(
    Color(0xFFD49B6A), // Golden Oak
    Color(0xFF8D5B4C), // Walnut
    Color(0xFFC07A46), // Cedar
    Color(0xFF5B8266), // Forest Pine
    Color(0xFFB5704D), // Cherry
    Color(0xFF627264), // Weathered Teak
    Color(0xFFC48B5E), // Beech
    Color(0xFF795548), // Mahogany
    Color(0xFFA67C52), // Elm
    Color(0xFF9E6B55)  // Chestnut
)

fun getPieceColor(index: Int): Color {
    return PieceColors[index % PieceColors.size]
}
