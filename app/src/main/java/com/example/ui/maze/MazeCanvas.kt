package com.example.ui.maze

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.CenterFocusStrong
import com.example.data.MazeCell
import com.example.ui.theme.MazeTierPalette
import com.example.util.DifficultyCurve
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance, Canvas-based Maze Renderer.
 * Draws custom neon glowing walls, entrance/exit, particles, fading trails,
 * and implements a camera tracking mechanism with manual pinch-zoom and drag/pan overrides.
 */
@Composable
fun MazeCanvas(
    grid: Array<Array<MazeCell>>,
    playerX: Int,
    playerY: Int,
    exitX: Int,
    exitY: Int,
    trail: List<Pair<Int, Int>>,
    palette: MazeTierPalette,
    particles: List<Particle>,
    controlScheme: ControlScheme = ControlScheme.SWIPE,
    onSwipeMove: (dx: Int, dy: Int) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val gridSize = grid.size

    // Smooth physics-based interpolation of player coordinates (spring glide)
    val animPlayerX by animateFloatAsState(
        targetValue = playerX.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playerX"
    )
    val animPlayerY by animateFloatAsState(
        targetValue = playerY.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "playerY"
    )

    // Pulse animation for the glowing Exit portal
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Manual Pan and Zoom Gesture Configuration
    var scale by remember { mutableStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }

    // Auto-adjust scale (zoom) based on difficulty level size
    val autoBaseScale = remember(gridSize) {
        when {
            gridSize <= 10 -> 1.0f // full fit
            gridSize <= 22 -> 0.75f // fit half
            gridSize <= 42 -> 0.45f
            else -> 0.32f
        }
    }

    // Reset offsets when a level changes
    LaunchedEffect(grid) {
        scale = autoBaseScale
        panOffset = Offset.Zero
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .pointerInput(grid, controlScheme) {
                if (controlScheme == ControlScheme.SWIPE) {
                    var accumulatedDrag = Offset.Zero
                    detectDragGestures(
                        onDragStart = {
                            accumulatedDrag = Offset.Zero
                        },
                        onDragEnd = {
                            accumulatedDrag = Offset.Zero
                        },
                        onDragCancel = {
                            accumulatedDrag = Offset.Zero
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            accumulatedDrag += dragAmount
                            val swipeThreshold = 50f // 50 pixels is a perfect standard swipe threshold
                            if (java.lang.Math.abs(accumulatedDrag.x) > swipeThreshold || java.lang.Math.abs(accumulatedDrag.y) > swipeThreshold) {
                                if (java.lang.Math.abs(accumulatedDrag.x) > java.lang.Math.abs(accumulatedDrag.y)) {
                                    if (accumulatedDrag.x > 0) onSwipeMove(1, 0)
                                    else onSwipeMove(-1, 0)
                                } else {
                                    if (accumulatedDrag.y > 0) onSwipeMove(0, 1)
                                    else onSwipeMove(0, -1)
                                }
                                accumulatedDrag = Offset.Zero
                            }
                        }
                    )
                } else {
                    // Support multi-touch pinching to zoom and dragging to pan in Joystick/D-pad mode
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.3f, 3.5f)
                        
                        val limitX = containerSize.width * 0.45f
                        val limitY = containerSize.height * 0.45f
                        val newX = (panOffset.x + pan.x).coerceIn(-limitX, limitX)
                        val newY = (panOffset.y + pan.y).coerceIn(-limitY, limitY)
                        panOffset = Offset(newX, newY)
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = this.size.width
            val canvasHeight = this.size.height
            val centerOffset = Offset(canvasWidth / 2f, canvasHeight / 2f)

            // Let each cell size be dynamically adjusted to fit
            val baseCellSizePx = min(canvasWidth, canvasHeight) / max(gridSize + 2, 8).toFloat()
            val cellSize = baseCellSizePx * scale

            // Auto-centering mechanism: centers the viewport on the player orb
            // If the user has dragged (panOffset != Zero), respect their manual panning
            val playerTargetCenterX = animPlayerX * cellSize + (cellSize / 2)
            val playerTargetCenterY = animPlayerY * cellSize + (cellSize / 2)

            val cameraX = centerOffset.x - playerTargetCenterX + panOffset.x
            val cameraY = centerOffset.y - playerTargetCenterY + panOffset.y

            // --- 1. DRAW VISITED CELLS TRAIL ---
            trail.forEach { (tx, ty) ->
                val rx = tx * cellSize + cameraX
                val ry = ty * cellSize + cameraY
                // Soft glow of breadcrumbs
                drawRect(
                    color = palette.trailColor,
                    topLeft = Offset(rx + cellSize * 0.15f, ry + cellSize * 0.15f),
                    size = Size(cellSize * 0.7f, cellSize * 0.7f)
                )
            }

            // --- 2. DRAW ENTRANCE / EXIT PORTALS ---
            // Exit visual indicator has been hidden as per player request to hide exit location.
            // Players must discover the physical doorway gap leading outside the maze borders!

            // --- 3. DRAW MAZE WALLS (PREMIUM GLOW EFFECT) ---
            val wallStrokeWidth = max(2f, cellSize * 0.10f)
            val glowWidth = wallStrokeWidth * 2.8f

            // Double Pass Wall Rendering: Pass 1 is Glow shadow, Pass 2 is Core Solid Line
            grid.forEach { row ->
                row.forEach { cell ->
                    val cx = cell.x * cellSize + cameraX
                    val cy = cell.y * cellSize + cameraY

                    // Top Wall
                    if (cell.topWall) {
                        drawMazeWall(cx, cy, cx + cellSize, cy, wallStrokeWidth, glowWidth, palette.wallColor, palette.wallGlowColor)
                    }
                    // Bottom Wall
                    if (cell.bottomWall) {
                        drawMazeWall(cx, cy + cellSize, cx + cellSize, cy + cellSize, wallStrokeWidth, glowWidth, palette.wallColor, palette.wallGlowColor)
                    }
                    // Left Wall
                    if (cell.leftWall) {
                        drawMazeWall(cx, cy, cx, cy + cellSize, wallStrokeWidth, glowWidth, palette.wallColor, palette.wallGlowColor)
                    }
                    // Right Wall
                    if (cell.rightWall) {
                        drawMazeWall(cx + cellSize, cy, cx + cellSize, cy + cellSize, wallStrokeWidth, glowWidth, palette.wallColor, palette.wallGlowColor)
                    }
                }
            }

            // --- 4. DRAW PLAYER ORB WITH LIGHT FLARE ---
            val px = animPlayerX * cellSize + cameraX + (cellSize / 2f)
            val py = animPlayerY * cellSize + cameraY + (cellSize / 2f)
            val playerRadius = cellSize * 0.35f

            drawIntoCanvas { canvas ->
                val pColor = palette.playerColor.toArgb()
                val playerPaint = Paint().asFrameworkPaint().apply {
                    color = pColor
                    setShadowLayer(cellSize * 0.5f, 0f, 0f, pColor)
                }
                canvas.nativeCanvas.drawCircle(px, py, playerRadius, playerPaint)
            }

            // --- 5. CELEBRATORY COMPLETION PARTICLES ---
            if (particles.isNotEmpty()) {
                particles.forEach { p ->
                    val pCanvasX = p.x * cellSize + cameraX
                    val pCanvasY = p.y * cellSize + cameraY
                    drawCircle(
                        color = palette.playerColor.copy(alpha = p.alpha),
                        radius = p.size * scale,
                        center = Offset(pCanvasX, pCanvasY)
                    )
                }
            }
        }

        // Floating Zoom Controls Overlay on the middle-right edge of the maze
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
                .background(palette.cardBg.copy(alpha = 0.75f), RoundedCornerShape(12.dp))
                .border(1.2.dp, palette.wallColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                .padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            IconButton(
                onClick = { scale = (scale + 0.15f).coerceIn(0.3f, 3.5f) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Zoom In",
                    tint = palette.wallColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = { 
                    scale = autoBaseScale
                    panOffset = Offset.Zero
                },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Reset Zoom",
                    tint = palette.accent,
                    modifier = Modifier.size(22.dp)
                )
            }
            IconButton(
                onClick = { scale = (scale - 0.15f).coerceIn(0.3f, 3.5f) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Zoom Out",
                    tint = palette.wallColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

/**
 * Extension function to draw a line segment with dual-pass bloom glow.
 */
private fun DrawScope.drawMazeWall(
    startX: Float,
    startY: Float,
    endX: Float,
    endY: Float,
    strokeWidth: Float,
    glowWidth: Float,
    coreColor: Color,
    glowColor: Color
) {
    // Pass 1: Draw Thick semi-transparent glow path
    drawLine(
        color = glowColor,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = glowWidth,
        cap = StrokeCap.Round
    )

    // Pass 2: Draw High-intensity solid core path
    drawLine(
        color = coreColor,
        start = Offset(startX, startY),
        end = Offset(endX, endY),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
}
