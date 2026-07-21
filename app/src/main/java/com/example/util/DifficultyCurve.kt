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
            level in 11..50 -> {
                val ratio = (level - 11) / 39f
                15 + (ratio * 8).toInt() // 15 to 23
            }
            level in 51..150 -> {
                val ratio = (level - 51) / 99f
                25 + (ratio * 12).toInt() // 25 to 37
            }
            level in 151..400 -> {
                val ratio = (level - 151) / 249f
                39 + (ratio * 14).toInt() // 39 to 53
            }
            level in 401..1000 -> {
                val ratio = (level - 401) / 599f
                55 + (ratio * 20).toInt() // 55 to 75
            }
            else -> {
                // endless scaling beyond 1000 getting harder and harder
                min(75 + ((level - 1000) / 20), 89)
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
            level <= 20 -> 1
            level <= 100 -> 2
            level <= 400 -> 3
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
