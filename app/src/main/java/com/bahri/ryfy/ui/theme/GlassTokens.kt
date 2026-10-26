package com.bahri.ryfy.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Liquid Glass Design Tokens for RyFy Spotless
val GlassBaseColor = Color(0xCC141414)
val GlassSurfaceColor = Color(0x99202020)
val GlassCardColor = Color(0x662A2A2A)
val GlassHighlightColor = Color(0x331DB954)

val GlassBorderBrush = Brush.verticalGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.22f),
        Color.White.copy(alpha = 0.04f)
    )
)

val GlassBorderActiveBrush = Brush.verticalGradient(
    colors = listOf(
        SpotifyGreenBright.copy(alpha = 0.6f),
        SpotifyGreen.copy(alpha = 0.2f)
    )
)

fun Modifier.liquidGlass(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundColor: Color = GlassSurfaceColor,
    borderWidth: Dp = 1.dp,
    borderBrush: Brush = GlassBorderBrush
): Modifier = this
    .clip(shape)
    .background(backgroundColor)
    .border(width = borderWidth, brush = borderBrush, shape = shape)
