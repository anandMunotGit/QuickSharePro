package com.example.quicksharepro.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedRadar(
    modifier: Modifier = Modifier,
    radarColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    @Composable
    fun RadarWave(delayMillis: Int) {
        val radiusRatio by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, delayMillis = delayMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radius"
        )

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000, delayMillis = delayMillis, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "alpha"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2
            drawCircle(
                color = radarColor.copy(alpha = alpha),
                radius = maxRadius * radiusRatio,
                style = Stroke(width = 2.dp.toPx())
            )
        }
    }

    Box(modifier = modifier, contentAlignment = androidx.compose.ui.Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val maxRadius = size.minDimension / 2
            
            // Fixed background rings
            for (i in 1..4) {
                drawCircle(
                    color = radarColor.copy(alpha = 0.05f),
                    radius = maxRadius * (i * 0.25f),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
        }
        
        RadarWave(delayMillis = 0)
        RadarWave(delayMillis = 1000)
        RadarWave(delayMillis = 2000)
    }
}
