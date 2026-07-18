package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Default color palette definitions
val DarkGrey = Color(0xFF0C101B)
val MintGreen = Color(0xFF10B981)
val WarmGold = Color(0xFFF59E0B)
val NeonPurple = Color(0xFF8B5CF6)
val LavaRed = Color(0xFFEF4444)

// Standard theme colors for fallback Material theme compilation compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

/**
 * Encapsulates the visual tokens used to render the maze background, walls,
 * particle bursts, trails, and typography for a specific level tier.
 */
data class MazeTierPalette(
    val wallColor: Color,
    val wallGlowColor: Color,
    val playerColor: Color,
    val trailColor: Color,
    val exitColor: Color,
    val bgStart: Color,
    val bgEnd: Color,
    val cardBg: Color,
    val accent: Color,
    val text: Color
)

object MazePalettes {
    // Dark mode variants (stunning glowing neon line drawings on deep gradients)
    private val DarkTier1 = MazeTierPalette(
        wallColor = Color(0xFF10B981), // Emerald Mint
        wallGlowColor = Color(0x3310B981),
        playerColor = Color(0xFF34D399), // Glowing mint orb
        trailColor = Color(0x1A10B981),
        exitColor = Color(0xFFFCD34D), // Pulsing gold portal
        bgStart = Color(0xFF040F0A), // Warm deep forest gradient
        bgEnd = Color(0xFF010503),
        cardBg = Color(0xFF0B2117),
        accent = Color(0xFF10B981),
        text = Color(0xFFE6F4EA)
    )

    private val DarkTier2 = MazeTierPalette(
        wallColor = Color(0xFFF59E0B), // Ember Warm Gold
        wallGlowColor = Color(0x33F59E0B),
        playerColor = Color(0xFFFBBF24), // Flame particle
        trailColor = Color(0x1AF59E0B),
        exitColor = Color(0xFFEF4444), // Molten portal
        bgStart = Color(0xFF140B02), // Copper glow
        bgEnd = Color(0xFF080401),
        cardBg = Color(0xFF2E1906),
        accent = Color(0xFFF59E0B),
        text = Color(0xFFFEF3C7)
    )

    private val DarkTier3 = MazeTierPalette(
        wallColor = Color(0xFF6366F1), // Indigo Twilight
        wallGlowColor = Color(0x336366F1),
        playerColor = Color(0xFF818CF8), // Radiant star spark
        trailColor = Color(0x1A6366F1),
        exitColor = Color(0xFFEC4899), // Nebula portal
        bgStart = Color(0xFF080614), // Starry space dusk
        bgEnd = Color(0xFF020208),
        cardBg = Color(0xFF1D163D),
        accent = Color(0xFF6366F1),
        text = Color(0xFFEEF2FF)
    )

    private val DarkTier4 = MazeTierPalette(
        wallColor = Color(0xFFEF4444), // Volcanic Flare
        wallGlowColor = Color(0x33EF4444),
        playerColor = Color(0xFFFBBF24), // Lava ash spark
        trailColor = Color(0x1AEF4444),
        exitColor = Color(0xFFFFFFFF), // Blazing portal
        bgStart = Color(0xFF140303), // Magma chamber
        bgEnd = Color(0xFF050101),
        cardBg = Color(0xFF330C0C),
        accent = Color(0xFFEF4444),
        text = Color(0xFFFEE2E2)
    )

    // Light mode variants (delightful, elegant ink-sketched lines on light backdrops)
    private val LightTier1 = MazeTierPalette(
        wallColor = Color(0xFF047857), // Deep emerald brush
        wallGlowColor = Color(0x11047857),
        playerColor = Color(0xFF10B981),
        trailColor = Color(0x0C047857),
        exitColor = Color(0xFFB45309),
        bgStart = Color(0xFFF0FDF4), // Fresh pastel mint
        bgEnd = Color(0xFFD1FAE5),
        cardBg = Color(0xFFFFFFFF),
        accent = Color(0xFF047857),
        text = Color(0xFF062F1F)
    )

    private val LightTier2 = MazeTierPalette(
        wallColor = Color(0xFFB45309), // Terracotta clay
        wallGlowColor = Color(0x11B45309),
        playerColor = Color(0xFFD97706),
        trailColor = Color(0x0CB45309),
        exitColor = Color(0xFFB91C1C),
        bgStart = Color(0xFFFFFBEB), // Alabaster gold sun
        bgEnd = Color(0xFFFDE68A),
        cardBg = Color(0xFFFFFFFF),
        accent = Color(0xFFB45309),
        text = Color(0xFF451A03)
    )

    private val LightTier3 = MazeTierPalette(
        wallColor = Color(0xFF4338CA), // Midnight royal blue
        wallGlowColor = Color(0x114338CA),
        playerColor = Color(0xFF4F46E5),
        trailColor = Color(0x0C4338CA),
        exitColor = Color(0xFFBE185D),
        bgStart = Color(0xFFEEF2FF), // Soft lavender glow
        bgEnd = Color(0xFFC7D2FE),
        cardBg = Color(0xFFFFFFFF),
        accent = Color(0xFF4338CA),
        text = Color(0xFF1E1B4B)
    )

    private val LightTier4 = MazeTierPalette(
        wallColor = Color(0xFF991B1B), // Rust red obsidian
        wallGlowColor = Color(0x11991B1B),
        playerColor = Color(0xFFDC2626),
        trailColor = Color(0x0C991B1B),
        exitColor = Color(0xFFD97706),
        bgStart = Color(0xFFFEF2F2), // Pumice rose white
        bgEnd = Color(0xFFFEE2E2),
        cardBg = Color(0xFFFFFFFF),
        accent = Color(0xFF991B1B),
        text = Color(0xFF450A0A)
    )

    fun getPalette(tier: Int, isDark: Boolean): MazeTierPalette {
        return if (isDark) {
            when (tier) {
                1 -> DarkTier1
                2 -> DarkTier2
                3 -> DarkTier3
                else -> DarkTier4
            }
        } else {
            when (tier) {
                1 -> LightTier1
                2 -> LightTier2
                3 -> LightTier3
                else -> LightTier4
            }
        }
    }
}
