package com.example.util

import kotlin.math.min

object DifficultyCurve {
    /**
     * Maps a level number to an appropriate maze grid size (width and height).
     * Level 1-10: 5x5 to 9x9 (small onboarding grids)
     * Level 11-30: 11x11 to 21x21 (medium branching grids)
     * Level 31-60: 25x25 to 41x41 (large panning/zooming grids)
     * Level 61-100+: 45x45 to 80x80 (massive viewports requiring navigation)
     */
    fun getGridSize(level: Int): Int {
        val rawSize = when {
            level in 1..10 -> 5 + (level - 1) * 1 // 5 to 14
            level in 11..30 -> {
                val ratio = (level - 11) / 19f
                11 + (ratio * 10).toInt() // 11 to 21
            }
            level in 31..60 -> {
                val ratio = (level - 31) / 29f
                23 + (ratio * 18).toInt() // 23 to 41
            }
            level in 61..100 -> {
                val ratio = (level - 61) / 39f
                43 + (ratio * 12).toInt() // 43 to 55
            }
            else -> {
                // endless scaling up to 80
                min(55 + ((level - 100) * 0.4).toInt(), 80)
            }
        }
        // Force odd size to make maze generation and entry/exit mapping look symmetrically perfect
        return if (rawSize % 2 == 0) rawSize + 1 else rawSize
    }

    /**
     * Returns the visual theme and difficulty tier (1-4) for a given level.
     */
    fun getTier(level: Int): Int {
        return when {
            level <= 10 -> 1
            level <= 30 -> 2
            level <= 60 -> 3
            else -> 4
        }
    }
    
    /**
     * Get the time threshold in seconds to earn stars for a level.
     */
    fun getStarThresholds(level: Int): Pair<Int, Int> {
        val size = getGridSize(level)
        val cellsCount = size * size
        val threeStarSeconds = min(15 + cellsCount / 4, 300) // generous but challenging
        val twoStarSeconds = threeStarSeconds * 2
        return Pair(threeStarSeconds, twoStarSeconds)
    }
}
