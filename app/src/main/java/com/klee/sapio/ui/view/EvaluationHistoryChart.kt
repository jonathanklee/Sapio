package com.klee.sapio.ui.view

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.klee.sapio.ui.model.Rating

private const val MAX_POINTS = 8
private val POINT_RADIUS = 3.dp
private val LINE_WIDTH = 1.5.dp

private val GOOD_COLOR = Color(0xFF4CAF50)
private val AVERAGE_COLOR = Color(0xFFFFC107)
private val BAD_COLOR = Color(0xFFF44336)
private val LINE_COLOR = Color(0x66888888)

@Composable
fun EvaluationHistoryChart(ratings: List<Int>, modifier: Modifier = Modifier) {
    val points = ratings.takeLast(MAX_POINTS)

    Canvas(modifier) {
        val radius = POINT_RADIUS.toPx()
        val offsets = points.mapIndexed { index, rating ->
            Offset(
                x = horizontalPosition(index, points.size, size.width, radius),
                y = verticalPosition(rating, size.height, radius)
            )
        }

        offsets.zipWithNext { from, to ->
            drawLine(
                color = LINE_COLOR,
                start = from,
                end = to,
                strokeWidth = LINE_WIDTH.toPx(),
                cap = StrokeCap.Round
            )
        }

        offsets.forEachIndexed { index, offset ->
            drawCircle(color = ratingColor(points[index]), radius = radius, center = offset)
        }
    }
}

private fun horizontalPosition(index: Int, count: Int, width: Float, radius: Float): Float {
    if (count == 1) {
        return width / 2
    }

    val span = width - radius * 2

    return radius + span * index / (count - 1)
}

private fun verticalPosition(rating: Int, height: Float, radius: Float): Float {
    val span = height - radius * 2

    return radius + span * levelOf(rating) / 2
}

private fun levelOf(rating: Int): Float {
    return when (rating) {
        Rating.GOOD -> 0f
        Rating.AVERAGE -> 1f
        else -> 2f
    }
}

fun ratingColor(rating: Int): Color {
    return when (rating) {
        Rating.GOOD -> GOOD_COLOR
        Rating.AVERAGE -> AVERAGE_COLOR
        else -> BAD_COLOR
    }
}
