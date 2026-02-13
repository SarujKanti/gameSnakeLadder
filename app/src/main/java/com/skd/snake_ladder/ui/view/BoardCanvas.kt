package com.skd.snake_ladder.ui.view

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.skd.snake_ladder.core.BoardMapper
import com.skd.snake_ladder.data.SnakeLadderConfig
import kotlin.math.abs

@Composable
fun BoardCanvas(
    playerPositions: List<Pair<Int, Color>>
) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {

        val cellSize = size.width / 10

        // Paint for numbering
        val textPaint = Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = cellSize / 3
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }

        // 🟦 Draw Board Grid + Numbers
        for (row in 0 until 10) {
            for (col in 0 until 10) {

                drawRect(
                    color = if ((row + col) % 2 == 0)
                        Color(0xFFF5F5F5)
                    else
                        Color(0xFFBBDEFB),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize)
                )

                // Calculate correct board number
                val boardRowFromBottom = 9 - row
                val baseNumber = boardRowFromBottom * 10

                val number =
                    if (boardRowFromBottom % 2 == 0)
                        baseNumber + col + 1
                    else
                        baseNumber + (10 - col)

                // Draw number
                drawContext.canvas.nativeCanvas.drawText(
                    number.toString(),
                    col * cellSize + cellSize / 2,
                    row * cellSize + cellSize / 1.5f,
                    textPaint
                )
            }
        }

        // 🪜 Draw Ladders
        SnakeLadderConfig.ladders.forEach { (start, end) ->

            val startCell = BoardMapper.map(start)
            val endCell = BoardMapper.map(end)

            val startOffset = Offset(
                startCell.col * cellSize + cellSize / 2,
                startCell.row * cellSize + cellSize / 2
            )

            val endOffset = Offset(
                endCell.col * cellSize + cellSize / 2,
                endCell.row * cellSize + cellSize / 2
            )

            drawLine(
                color = Color(0xFF4CAF50),
                start = startOffset,
                end = endOffset,
                strokeWidth = 10f,
                cap = StrokeCap.Round
            )
        }

        // 🐍 Draw Snakes
        SnakeLadderConfig.snakes.forEach { (start, end) ->

            val startCell = BoardMapper.map(start)
            val endCell = BoardMapper.map(end)

            val startOffset = Offset(
                startCell.col * cellSize + cellSize / 2,
                startCell.row * cellSize + cellSize / 2
            )

            val endOffset = Offset(
                endCell.col * cellSize + cellSize / 2,
                endCell.row * cellSize + cellSize / 2
            )

            val path = Path().apply {
                moveTo(startOffset.x, startOffset.y)

                cubicTo(
                    startOffset.x + abs(endOffset.x - startOffset.x) / 2,
                    startOffset.y,
                    endOffset.x - abs(endOffset.x - startOffset.x) / 2,
                    endOffset.y,
                    endOffset.x,
                    endOffset.y
                )
            }

            drawPath(
                path = path,
                color = Color.Red,
                style = Stroke(width = 8f)
            )
        }

        // 🔵 Draw Tokens (Handle Overlap)
// Group tokens by position
        val groupedTokens = playerPositions.groupBy { it.first }

        groupedTokens.forEach { (position, playersAtSameBox) ->

            if (position <= 0) return@forEach

            val mapped = BoardMapper.map(position)

            val baseCenterX = mapped.col * cellSize + cellSize / 2
            val baseCenterY = mapped.row * cellSize + cellSize / 2

            val count = playersAtSameBox.size

            val radius =
                if (count == 1) cellSize / 3
                else cellSize / 5

            playersAtSameBox.forEachIndexed { index, (_, color) ->

                val offset = when (count) {

                    1 -> Offset(0f, 0f)

                    2 -> if (index == 0)
                        Offset(-radius, 0f)
                    else
                        Offset(radius, 0f)

                    3 -> when (index) {
                        0 -> Offset(-radius, -radius)
                        1 -> Offset(radius, -radius)
                        else -> Offset(0f, radius)
                    }

                    4 -> when (index) {
                        0 -> Offset(-radius, -radius)
                        1 -> Offset(radius, -radius)
                        2 -> Offset(-radius, radius)
                        else -> Offset(radius, radius)
                    }

                    else -> Offset(0f, 0f)
                }

                drawCircle(
                    color = color,
                    radius = radius,
                    center = Offset(
                        baseCenterX + offset.x,
                        baseCenterY + offset.y
                    )
                )
            }
        }

    }
}
