package com.skd.snake_ladder.ui.view

import android.graphics.Paint as AndroidPaint
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import com.skd.snake_ladder.core.BoardMapper
import com.skd.snake_ladder.data.SnakeLadderConfig
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun BoardCanvas(playerPositions: List<Pair<Int, Color>>) {

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val cellSize = size.width / 10f

        // ── 1. Board cells ──────────────────────────────────────────────
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                val light = (row + col) % 2 == 0
                drawRect(
                    color = if (light) Color(0xFFFFF9C4) else Color(0xFFB3E5FC),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize)
                )
                drawRect(
                    color = Color(0xFFBDBDBD),
                    topLeft = Offset(col * cellSize, row * cellSize),
                    size = Size(cellSize, cellSize),
                    style = Stroke(width = 0.8f)
                )
            }
        }

        // ── 2. Ladders ──────────────────────────────────────────────────
        SnakeLadderConfig.ladders.forEach { (from, to) ->
            val fc = BoardMapper.map(from)
            val tc = BoardMapper.map(to)
            drawLadder(
                bottom = Offset(fc.col * cellSize + cellSize / 2f, fc.row * cellSize + cellSize / 2f),
                top    = Offset(tc.col * cellSize + cellSize / 2f, tc.row * cellSize + cellSize / 2f),
                cellSize = cellSize
            )
        }

        // ── 3. Snakes ───────────────────────────────────────────────────
        SnakeLadderConfig.snakes.forEach { (from, to) ->
            val fc = BoardMapper.map(from)
            val tc = BoardMapper.map(to)
            drawSnake(
                head = Offset(fc.col * cellSize + cellSize / 2f, fc.row * cellSize + cellSize / 2f),
                tail = Offset(tc.col * cellSize + cellSize / 2f, tc.row * cellSize + cellSize / 2f),
                cellSize = cellSize
            )
        }

        // ── 4. Cell numbers (drawn on top so always readable) ───────────
        val textPaint = AndroidPaint().apply {
            color = android.graphics.Color.BLACK
            textSize = cellSize / 4f
            textAlign = AndroidPaint.Align.CENTER
            isFakeBoldText = true
            isAntiAlias = true
        }
        val bgPaint = AndroidPaint().apply {
            color = android.graphics.Color.argb(180, 255, 255, 255)
            isAntiAlias = true
        }
        for (row in 0 until 10) {
            for (col in 0 until 10) {
                val boardRow = 9 - row
                val base = boardRow * 10
                val number = if (boardRow % 2 == 0) base + col + 1 else base + (10 - col)
                val x = col * cellSize + cellSize / 2f
                val y = row * cellSize + textPaint.textSize * 1.15f
                val pad = textPaint.textSize * 0.22f
                // Semi-transparent pill so number is legible over art
                drawContext.canvas.nativeCanvas.drawRoundRect(
                    x - textPaint.textSize * 0.75f, y - textPaint.textSize - pad,
                    x + textPaint.textSize * 0.75f, y + pad,
                    textPaint.textSize * 0.28f, textPaint.textSize * 0.28f, bgPaint
                )
                drawContext.canvas.nativeCanvas.drawText(number.toString(), x, y, textPaint)
            }
        }

        // ── 5. Player tokens ────────────────────────────────────────────
        playerPositions.groupBy { it.first }.forEach { (pos, players) ->
            if (pos <= 0) return@forEach
            val m = BoardMapper.map(pos)
            val cx = m.col * cellSize + cellSize / 2f
            val cy = m.row * cellSize + cellSize / 2f
            val r = if (players.size == 1) cellSize * 0.24f else cellSize * 0.16f

            players.forEachIndexed { idx, (_, color) ->
                val off = if (players.size == 2)
                    if (idx == 0) Offset(-r * 0.9f, 0f) else Offset(r * 0.9f, 0f)
                else Offset(0f, 0f)

                val c = Offset(cx + off.x, cy + off.y)
                // Drop shadow
                drawCircle(Color(0x55000000), radius = r, center = c + Offset(2f, 2f))
                // Body
                drawCircle(color, radius = r, center = c)
                // Inner ring
                drawCircle(Color.White, radius = r * 0.68f, center = c, style = Stroke(width = r * 0.13f))
                // Shine
                drawCircle(Color(0x88FFFFFF), radius = r * 0.32f, center = c - Offset(r * 0.22f, r * 0.22f))
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Ladder — wooden rails + rungs with shadow and shine
// ═══════════════════════════════════════════════════════════════════════════
private fun DrawScope.drawLadder(bottom: Offset, top: Offset, cellSize: Float) {
    val dx = top.x - bottom.x
    val dy = top.y - bottom.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return

    val ux = dx / len;  val uy = dy / len   // unit along ladder
    val px = -uy;       val py = ux          // perpendicular

    val halfGap  = cellSize * 0.10f   // half-distance between the two rails
    val railW    = cellSize * 0.055f
    val rungW    = cellSize * 0.042f
    val shadow   = Offset(2f, 2f)

    val r1s = Offset(bottom.x + px * halfGap, bottom.y + py * halfGap)
    val r1e = Offset(top.x   + px * halfGap, top.y   + py * halfGap)
    val r2s = Offset(bottom.x - px * halfGap, bottom.y - py * halfGap)
    val r2e = Offset(top.x   - px * halfGap, top.y   - py * halfGap)

    // Shadows
    drawLine(Color(0x44000000), r1s + shadow, r1e + shadow, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0x44000000), r2s + shadow, r2e + shadow, strokeWidth = railW, cap = StrokeCap.Round)

    // Rails (dark wood)
    drawLine(Color(0xFF5D4037), r1s, r1e, strokeWidth = railW, cap = StrokeCap.Round)
    drawLine(Color(0xFF5D4037), r2s, r2e, strokeWidth = railW, cap = StrokeCap.Round)

    // Rail highlight stripe
    drawLine(Color(0xFF8D6E63), r1s, r1e, strokeWidth = railW * 0.26f, cap = StrokeCap.Round)
    drawLine(Color(0xFF8D6E63), r2s, r2e, strokeWidth = railW * 0.26f, cap = StrokeCap.Round)

    // Rungs
    val numRungs = maxOf(3, (len / (cellSize * 0.36f)).toInt())
    for (i in 1 until numRungs) {
        val t  = i.toFloat() / numRungs
        val rc = Offset(bottom.x + dx * t, bottom.y + dy * t)
        val rs = Offset(rc.x + px * halfGap, rc.y + py * halfGap)
        val re = Offset(rc.x - px * halfGap, rc.y - py * halfGap)
        drawLine(Color(0x44000000), rs + shadow, re + shadow, strokeWidth = rungW, cap = StrokeCap.Round)
        drawLine(Color(0xFF3E2723), rs, re, strokeWidth = rungW, cap = StrokeCap.Round)
        // Rung highlight
        drawLine(Color(0xFF6D4C41), rs, re, strokeWidth = rungW * 0.3f, cap = StrokeCap.Round)
    }

    // Bolt circles at four corners
    val boltR = railW * 0.72f
    for (pt in listOf(r1s, r1e, r2s, r2e)) {
        drawCircle(Color(0xFF3E2723), radius = boltR, center = pt)
        drawCircle(Color(0xFF8D6E63), radius = boltR * 0.42f, center = pt - Offset(boltR * 0.22f, boltR * 0.22f))
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Snake — sinusoidal body, detailed head with eyes + forked tongue, tapered tail
// ═══════════════════════════════════════════════════════════════════════════
private fun DrawScope.drawSnake(head: Offset, tail: Offset, cellSize: Float) {
    val dx = tail.x - head.x
    val dy = tail.y - head.y
    val len = sqrt(dx * dx + dy * dy)
    if (len < 1f) return

    val ux = dx / len;  val uy = dy / len   // unit toward tail
    val px = -uy;       val py = ux          // perpendicular (left of travel)

    val bodyW    = cellSize * 0.18f
    val amplitude = cellSize * 0.13f
    // 6 half-waves → 3 S-curves; sin(nπ)=0 so endpoints land exactly on head/tail
    val halfWaves = 6

    // Build sine-wave points (80 steps for smooth curve)
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

    // Drop shadow
    drawPath(bodyPath, Color(0x44000000), style = Stroke(width = bodyW + 4f, cap = StrokeCap.Round))
    // Outer body (dark green)
    drawPath(bodyPath, Color(0xFF2E7D32), style = Stroke(width = bodyW, cap = StrokeCap.Round))
    // Inner stripe (lighter green — gives a 3-D scale effect)
    drawPath(bodyPath, Color(0xFF66BB6A), style = Stroke(width = bodyW * 0.48f, cap = StrokeCap.Round))

    // ── Head ────────────────────────────────────────────────────────────
    val headR = cellSize * 0.165f

    // Head shadow
    drawCircle(Color(0x44000000), radius = headR + 2f, center = head + Offset(2f, 2f))
    // Outer head
    drawCircle(Color(0xFF1B5E20), radius = headR, center = head)
    // Inner dome (lighter, gives roundness)
    drawCircle(Color(0xFF2E7D32), radius = headR * 0.66f, center = head)

    // ── Eyes ────────────────────────────────────────────────────────────
    val eyeR   = headR * 0.28f
    val eyeOff = headR * 0.50f
    val eyeL   = Offset(head.x + px * eyeOff, head.y + py * eyeOff)
    val eyeR2  = Offset(head.x - px * eyeOff, head.y - py * eyeOff)

    for (eye in listOf(eyeL, eyeR2)) {
        drawCircle(Color.White,           radius = eyeR,          center = eye)
        drawCircle(Color(0xFF1A237E),     radius = eyeR * 0.65f,  center = eye)  // blue iris
        drawCircle(Color.Black,           radius = eyeR * 0.33f,  center = eye)  // pupil
        // Catch-light
        drawCircle(Color.White,           radius = eyeR * 0.16f,  center = eye - Offset(eyeR * 0.18f, eyeR * 0.18f))
    }

    // ── Forked tongue ────────────────────────────────────────────────────
    val tongLen  = headR * 1.35f
    val forkLen  = headR * 0.55f
    val forkSpread = forkLen * 0.55f
    // Tongue root starts at head center, extends OPPOSITE to body direction
    val tipX = head.x - ux * tongLen
    val tipY = head.y - uy * tongLen
    // Fork tips: forward a bit + left/right spread
    val fL = Offset(tipX - ux * forkLen + px * forkSpread, tipY - uy * forkLen + py * forkSpread)
    val fR = Offset(tipX - ux * forkLen - px * forkSpread, tipY - uy * forkLen - py * forkSpread)

    val tongue = Path().apply {
        moveTo(head.x, head.y)
        lineTo(tipX, tipY)
        lineTo(fL.x, fL.y)
        moveTo(tipX, tipY)
        lineTo(fR.x, fR.y)
    }
    drawPath(tongue, Color(0xFFE53935), style = Stroke(width = headR * 0.16f, cap = StrokeCap.Round))

    // ── Tail tip (tapered) ────────────────────────────────────────────────
    drawCircle(Color(0xFF2E7D32), radius = bodyW * 0.18f, center = tail)
}
