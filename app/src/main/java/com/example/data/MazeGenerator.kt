package com.example.data

import kotlin.random.Random

/**
 * Procedural maze generator utilizing a randomized Depth-First Search (Recursive Backtracking) algorithm.
 * Guarantees a "perfect" maze with a single unique solution path, no isolated loops, and complete connectivity.
 */
object MazeGenerator {

    fun generate(
        width: Int, 
        height: Int, 
        seed: Long = System.currentTimeMillis()
    ): Array<Array<MazeCell>> {
        val grid = Array(width) { x ->
            Array(height) { y ->
                MazeCell(x, y)
            }
        }

        val random = Random(seed)
        val stack = mutableListOf<MazeCell>()
        
        // Begin generation at origin (0, 0)
        val startCell = grid[0][0]
        startCell.visited = true
        stack.add(startCell)

        while (stack.isNotEmpty()) {
            val current = stack.last()
            val neighbors = getUnvisitedNeighbors(current, grid, width, height)

            if (neighbors.isNotEmpty()) {
                val next = neighbors[random.nextInt(neighbors.size)]
                removeWallsBetween(current, next)
                next.visited = true
                stack.add(next)
            } else {
                stack.removeAt(stack.size - 1)
            }
        }

        return grid
    }

    private fun getUnvisitedNeighbors(
        cell: MazeCell, 
        grid: Array<Array<MazeCell>>, 
        width: Int, 
        height: Int
    ): List<MazeCell> {
        val neighbors = mutableListOf<MazeCell>()
        val x = cell.x
        val y = cell.y

        // Top
        if (y > 0 && !grid[x][y - 1].visited) neighbors.add(grid[x][y - 1])
        // Right
        if (x < width - 1 && !grid[x + 1][y].visited) neighbors.add(grid[x + 1][y])
        // Bottom
        if (y < height - 1 && !grid[x][y + 1].visited) neighbors.add(grid[x][y + 1])
        // Left
        if (x > 0 && !grid[x - 1][y].visited) neighbors.add(grid[x - 1][y])

        return neighbors
    }

    private fun removeWallsBetween(a: MazeCell, b: MazeCell) {
        val dx = b.x - a.x
        val dy = b.y - a.y

        if (dx == 1) { // b is right of a
            a.rightWall = false
            b.leftWall = false
        } else if (dx == -1) { // b is left of a
            a.leftWall = false
            b.rightWall = false
        }

        if (dy == 1) { // b is below a
            a.bottomWall = false
            b.topWall = false
        } else if (dy == -1) { // b is above a
            a.topWall = false
            b.bottomWall = false
        }
    }
}
