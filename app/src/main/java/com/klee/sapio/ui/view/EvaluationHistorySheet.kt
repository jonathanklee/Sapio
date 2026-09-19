package com.klee.sapio.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class EvaluationHistoryRow(
    val rating: Int,
    val label: String,
    val version: String?,
    val date: String,
    val brokenFeatures: List<String>
)

private val BROKEN_CHIP_COLOR = Color(0xFFE53935)

@Composable
fun EvaluationHistorySheet(
    title: String,
    rows: List<EvaluationHistoryRow>,
    textColor: Color,
    secondaryTextColor: Color
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = title,
            color = textColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(rows) { row ->
                HistoryRow(row, textColor, secondaryTextColor)
            }
        }
    }
}

@Composable
private fun HistoryRow(row: EvaluationHistoryRow, textColor: Color, secondaryTextColor: Color) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(
            Modifier
                .padding(top = 4.dp)
                .size(14.dp)
                .clip(CircleShape)
                .background(ratingColor(row.rating))
        )

        Column(Modifier.padding(start = 12.dp)) {
            Text(
                text = row.label,
                color = textColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            row.version?.let { version ->
                Text(
                    text = "v$version",
                    color = secondaryTextColor,
                    fontSize = 12.sp,
                    fontStyle = FontStyle.Italic
                )
            }

            Text(
                text = row.date,
                color = secondaryTextColor,
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic
            )

            if (row.brokenFeatures.isNotEmpty()) {
                BrokenFeatureChips(row.brokenFeatures)
            }
        }
    }
}

@Composable
private fun BrokenFeatureChips(features: List<String>) {
    Row(
        Modifier.padding(top = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        features.forEach { feature ->
            Text(
                text = feature,
                color = Color.White,
                fontSize = 11.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(BROKEN_CHIP_COLOR)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}
