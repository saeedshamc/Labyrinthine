package com.example.data

/**
 * Data class representing a single cell in the maze grid.
 * Keeps track of whether walls are active on each side, and if the cell has been visited.
 */
data class MazeCell(
    val x: Int,
    val y: Int,
    var topWall: Boolean = true,
    var rightWall: Boolean = true,
    var bottomWall: Boolean = true,
    var leftWall: Boolean = true,
    var visited: Boolean = false
)
