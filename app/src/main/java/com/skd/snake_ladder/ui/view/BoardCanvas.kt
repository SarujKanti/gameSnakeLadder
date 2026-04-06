package com.skd.snake_ladder.ui.view

import android.graphics.Paint as AndroidPaint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.skd.snake_ladder.core.BoardMapper
import com.skd.snake_ladder.data.SnakeLadderConfig
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.sqrt

// Each snake has its own colour scheme: (outerBody, head/dark, innerStripe)
private val SNAKE_COLORS = mapOf(
    99 to Triple(Color(0xFFD32F2F), Color(0xFFB71C1C), Color(0xFFEF5350)),  // Crimson
    95 to Triple(Color(0xFFAD1457), Color(0xFF880E4F), Color(0xFFF06292)),  // Deep Pink
    70 to Triple(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFFCE93D8)),  // Purple
    52 to Triple(Color(0xFFE64A19), Color(0xFFBF360C), Color(0xFFFF7043)),  // Orange
    25 to Triple(Color(0xFF00695C), Color(0xFF004D40), Color(0xFF4DB6AC)),  // Teal
)
private val DEFAULT_SNAKE = Triple(Color(0xFF2E7D32), Color(0xFF1B5E20), Color(0xFF66BB6A))

@Composable
fun BoardCanvas(
    playerPositions: List<Pair<Int, Color>>,
    activeSnakeFrom: Int?  = null,
    activeLadderFrom: Int? = null,
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val cellSize = size.width / 10f

        // ── 1. Checkerboard cells ────────────────────────────────────────
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                drawRect(
                    color = if ((row + col) % 2 == 0) Color(0xFFFFF9C4) else Color(0xFFB3E5FC),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size    = Size(cellSize, cellSize)
                )
                drawRect(
                    color   = Color(0xFFBDBDBD),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size    = Size(cellSize, cellSize),
                    style   = Stroke(width = 0.8f)
                )
            }
        }

        // ── 2. Ladders ───────────────────────────────────────────────────
        SnakeLadderConfig.ladders.forEach { (from, to) ->
            val fc = BoardMapper.map(from)
            val tc = BoardMapper.map(to)
            val isActive = (from == activeLadderFrom)
            drawLadder(
                bottom   = Offset(fc.col * cellSize + cellSize / 2f, fc.row * cellSize + cellSize / 2f),
                top      = Offset(tc.col * cellSize + cellSize / 2f, tc.row * cellSize + cellSize / 2f),
                cellSize = cellSize,
                isActive = isActive
            )
        }

        // ── 3. Snakes ────────────────────────────────────────────────────
        SnakeLadderConfig.snakes.forEach { (from, to) ->
            val fc = BoardMapper.map(from)
            val tc = BoardMapper.map(to)
            val isActive = (from == activeSnakeFrom)
            val colors = SNAKE_COLORS[from] ?: DEFAULT_SNAKE
            drawSnake(
                head     = Offset(fc.col * cellSize + cellSize / 2f, fc.row * cellSize + cellSize / 2f),
                tail     = Offset(tc.col * cellSize + cellSize / 2f, tc.row * cellSize + cellSize / 2f),
                cellSize = cellSize,
                colors   = colors,
                isActive = isActive
            )
        }

        // ── 4. Cell numbers (always on top) ──────────────────────────────
        val textPaint = AndroidPaint().apply {
            color       = android.graphics.Color.BLACK
            textSize    = cellSize / 4.2f
            textAlign   = AndroidPaint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bgPaint = AndroidPaint().apply {
            color       = android.graphics.Color.argb(185, 255, 255, 255)
            isAntiAlias = true
        }
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                val boardRow = 9 - row
                val base   = boardRow * 10
                val number = if (boardRow % 2 == 0) base + col + 1 else base + (10 - col)
                val x  = col * cellSize + cellSize / 2f
                val y  = row * cellSize + textPaint.textSize * 1.1f
                val pad = textPaint.textSize * 0.20f
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    x - textPaint.textSize * 0.78f, y - textPaint.textSize - pad,
                    x + textPaint.textSize * 0.78f, y + pad,
                    textPaint.textSize * 0.28f, textPaint.textSize * 0.28f, bgPaint
                )
                drawContext.canvas.nativeCanvas.drawText(number.toString(), x, y, textPaint)
            }
        }

        // ── 5. Player tokens ─────────────────────────────────────────────
        playerPositions.groupBy { it.first }.forEach { (pos, players) ->
            if (pos <= 0) return@forEach
            val m  = BoardMapper.map(pos)
            val cx = m.col * cellSize + cellSize / 2f
            val cy = m.row * cellSize + cellSize / 2f
            val r  = if (players.size == 1) cellSize * 0.24f else cellSize * 0.165f

            players.forEachIndexed { idx, (_, color) ->
                val off = if (players.size == 2)
                    if (idx == 0) Offset(-r * 0.92f, 0f) else Offset(r * 0.92f, 0f)
                else Offset(0f, 0f)
                val c = Offset(cx + off.x, cy + off.y)

                drawCircle(Color(0x55000000), radius = r,          center = c + Offset(2f, 2f))
                drawCircle(color,             radius = r,          center = c)
                drawCircle(Color.White,       radius = r * 0.68f,  center = c, style = Stroke(width = r * 0.14f))
                drawCircle(Color(0x88FFFFFF), radius = r * 0.30f,  center = c - Offset(r * 0.24f, r * 0.24f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Ladder — wooden rails + rungs; golden glow when active
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawLadder(
    bottom: Offset, top: Offset, cellSize: Float, isActive: Boolean
) {
    val dx = top.x - bottom.x;  val dy = top.y - bottom.y
    val len = sqrt(dx * dx + dy * dy);  if (len < 1f) return
    val ux = dx / len;  val uy = dy / len
    val px = -uy;       val py = ux

    val halfGap = cellSize * 0.10f
    val railW   = cellSize * 0.055f
    val rungW   = cellSize * 0.042f
    val sh      = Offset(2f, 2f)

    val r1s = Offset(bottom.x + px * halfGap, bottom.y + py * halfGap)
    val r1e = Offset(top.x   + px * halfGap, top.y   + py * halfGap)
    val r2s = Offset(bottom.x - px * halfGap, bottom.y - py * halfGap)
    val r2e = Offset(top.x   - px * halfGap, top.y   - py * halfGap)

    // Glow when active
    if (isActive) {
        drawLine(Color(0x88FFD700), r1s, r1e, strokeWidth = railW * 3.5f, cap = StrokeCap.Round)
        drawLine(Color(0x88FFD700), r2s, r2e, strokeWidth = railW * 3.5f, cap = StrokeCap.Round)
    }

    drawLine(Color(0x44000000), r1s + sh, r1e + sh, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0x44000000), r2s + sh, r2e + sh, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0xFF5D4037), r1s, r1e, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0xFF5D4037), r2s, r2e, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0xFF8D6E63), r1s, r1e, strokeWidth = railW * 0.26f, cap = StrokeCap.Round)
    drawLine(Color(0xFF8D6E63), r2s, r2e, strokeWidth = railW * 0.26f, cap = StrokeCap.Round)

    val numRungs = maxOf(3, (len / (cellSize * 0.36f)).toInt())
    for (i in 1 until numRungs) {
        val t  = i.toFloat() / numRungs
        val rc = Offset(bottom.x + dx * t, bottom.y + dy * t)
        val rs = Offset(rc.x + px * halfGap, rc.y + py * halfGap)
        val re = Offset(rc.x - px * halfGap, rc.y - py * halfGap)
        if (isActive) drawLine(Color(0x66FFD700), rs, re, strokeWidth = rungW * 3f, cap = StrokeCap.Round)
        drawLine(Color(0x44000000), rs + sh, re + sh, strokeWidth = rungW, cap = StrokeCap.Round)
        drawLine(Color(0xFF3E2723), rs, re, strokeWidth = rungW, cap = StrokeCap.Round)
        drawLine(Color(0xFF6D4C41), rs, re, strokeWidth = rungW * 0.3f, cap = StrokeCap.Round)
    }

    val boltR = railW * 0.72f
    for (pt in listOf(r1s, r1e, r2s, r2e)) {
        drawCircle(Color(0xFF3E2723), radius = boltR,         center = pt)
        drawCircle(Color(0xFF8D6E63), radius = boltR * 0.42f, center = pt - Offset(boltR * 0.22f, boltR * 0.22f))
    }
}

// ═══════════════════════════════════════════════════════════════════
//  Snake — sinusoidal body, per-snake colour, white glow when active
// ═══════════════════════════════════════════════════════════════════
private fun DrawScope.drawSnake(
    head: Offset, tail: Offset, cellSize: Float,
    colors: Triple<Color, Color, Color>, isActive: Boolean
) {
    val dx = tail.x - head.x;  val dy = tail.y - head.y
    val len = sqrt(dx * dx + dy * dy);  if (len < 1f) return
    val ux = dx / len;  val uy = dy / len
    val px = -uy;       val py = ux

    val (outerCol, headCol, stripeCol) = colors
    val bodyW     = cellSize * 0.18f
    val amplitude = cellSize * 0.13f
    val halfWaves = 6   // 3 S-curves; sin(nπ)=0 so ends land exactly on head/tail

    val steps = 80
    val pts = Array(steps + 1) { i ->
        val t    = i.toFloat() / steps
        val wave = sin(t.toDouble() * halfWaves * PI).toFloat() * amplitude
        Offset(head.x + dx * t + px * wave, head.y + dy * t + py * wave)
    }
    val bodyPath = Path().apply {
        moveTo(pts[0].x, pts[0].y)
        for (i in 1..steps) lineTo(pts[i].x, pts[i].y)
    }

    // White glow halo when active
    if (isActive) {
        drawPath(bodyPath, Color(0xAAFFFFFF), style = Stroke(width = bodyW * 2.6f, cap = StrokeCap.Round))
    }

    drawPath(bodyPath, Color(0x44000000), style = Stroke(width = bodyW + 4f,    cap = StrokeCap.Round))
    drawPath(bodyPath, outerCol,          style = Stroke(width = bodyW,          cap = StrokeCap.Round))
    drawPath(bodyPath, stripeCol,         style = Stroke(width = bodyW * 0.46f, cap = StrokeCap.Round))

    // ── Head ──────────────────────────────────────────────────────
    val headR = cellSize * 0.165f
    if (isActive) drawCircle(Color(0xAAFFFFFF), radius = headR * 2.2f, center = head)
    drawCircle(Color(0x44000000), radius = headR + 2f,      center = head + Offset(2f, 2f))
    drawCircle(headCol,           radius = headR,            center = head)
    drawCircle(outerCol,          radius = headR * 0.65f,   center = head)

    // ── Eyes ──────────────────────────────────────────────────────
    val eyeR   = headR * 0.28f
    val eyeOff = headR * 0.50f
    for (eye in listOf(
        Offset(head.x + px * eyeOff, head.y + py * eyeOff),
        Offset(head.x - px * eyeOff, head.y - py * eyeOff)
    )) {
        drawCircle(Color.White,           radius = eyeR,          center = eye)
        drawCircle(Color(0xFF1A237E),     radius = eyeR * 0.65f,  center = eye)
        drawCircle(Color.Black,           radius = eyeR * 0.33f,  center = eye)
        drawCircle(Color.White,           radius = eyeR * 0.16f,  center = eye - Offset(eyeR * 0.18f, eyeR * 0.18f))
    }

    // ── Forked tongue ─────────────────────────────────────────────
    val tongLen    = headR * 1.35f
    val forkLen    = headR * 0.55f
    val forkSpread = forkLen * 0.55f
    val tipX = head.x - ux * tongLen;  val tipY = head.y - uy * tongLen
    val fL = Offset(tipX - ux * forkLen + px * forkSpread, tipY - uy * forkLen + py * forkSpread)
    val fR = Offset(tipX - ux * forkLen - px * forkSpread, tipY - uy * forkLen - py * forkSpread)
    drawPath(Path().apply {
        moveTo(head.x, head.y); lineTo(tipX, tipY)
        lineTo(fL.x, fL.y); moveTo(tipX, tipY); lineTo(fR.x, fR.y)
    }, color = Color(0xFFE53935), style = Stroke(width = headR * 0.16f, cap = StrokeCap.Round))

    // ── Tail tip ──────────────────────────────────────────────────
    drawCircle(outerCol, radius = bodyW * 0.18f, center = tail)
}
