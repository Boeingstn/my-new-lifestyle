package com.suthinee.calorietracker.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** A minimal dependency-free bar chart: no charting library needed for the MVP's weekly/monthly trends. */
@Composable
fun SimpleBarChart(
    values: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    barHeight: androidx.compose.ui.unit.Dp = 120.dp
) {
    val maxValue = (values.maxOfOrNull { it.second } ?: 0).coerceAtLeast(1)
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEach { (label, value) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(barHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val fraction = (value.toFloat() / maxValue.toFloat()).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight(fraction.coerceAtLeast(0.02f))
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    )
                }
                Text(label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
