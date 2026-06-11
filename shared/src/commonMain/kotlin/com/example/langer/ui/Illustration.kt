package com.example.langer.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun HeadphonesCloudCharacter(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Draw Stick Legs
        val legYStart = h * 0.72f
        val legYEnd = h * 0.88f
        val legStroke = 5f
        
        // Left leg
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.44f, legYStart),
            end = Offset(w * 0.44f, legYEnd),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )
        // Left foot
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.40f, legYEnd),
            end = Offset(w * 0.44f, legYEnd),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )
        
        // Right leg
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.56f, legYStart),
            end = Offset(w * 0.56f, legYEnd),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )
        // Right foot
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.56f, legYEnd),
            end = Offset(w * 0.60f, legYEnd),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )

        // 2. Draw Stick Arms
        // Left arm (waving slightly upward)
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.26f, h * 0.5f),
            end = Offset(w * 0.18f, h * 0.58f),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )
        // Right arm
        drawLine(
            color = Color.Black,
            start = Offset(w * 0.74f, h * 0.5f),
            end = Offset(w * 0.82f, h * 0.58f),
            strokeWidth = legStroke,
            cap = StrokeCap.Round
        )

        // 3. Draw Body (Large white egg/cloud oval)
        val bodySize = Size(w * 0.56f, h * 0.46f)
        val bodyOffset = Offset(w * 0.22f, h * 0.26f)
        drawOval(
            color = Color.White,
            topLeft = bodyOffset,
            size = bodySize
        )
        drawOval(
            color = Color.Black,
            topLeft = bodyOffset,
            size = bodySize,
            style = Stroke(width = 5f)
        )

        // 4. Draw Headphones Band
        val bandPath = Path().apply {
            moveTo(w * 0.26f, h * 0.45f)
            cubicTo(
                w * 0.24f, h * 0.12f,
                w * 0.76f, h * 0.12f,
                w * 0.74f, h * 0.45f
            )
        }
        drawPath(
            path = bandPath,
            color = Color.Black,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // 5. Draw Headphone Ear Cups (Pill shapes on sides)
        val cupW = w * 0.08f
        val cupH = h * 0.18f
        // Left Cup (Red-coral)
        drawRoundRect(
            color = Color(0xFFFB7185), // coral-pink/red
            topLeft = Offset(w * 0.19f, h * 0.36f),
            size = Size(cupW, cupH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(w * 0.19f, h * 0.36f),
            size = Size(cupW, cupH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(width = 5f)
        )
        // Right Cup (Red-coral)
        drawRoundRect(
            color = Color(0xFFFB7185),
            topLeft = Offset(w * 0.73f, h * 0.36f),
            size = Size(cupW, cupH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
        )
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(w * 0.73f, h * 0.36f),
            size = Size(cupW, cupH),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f),
            style = Stroke(width = 5f)
        )

        // 6. Draw Closed Eyes (curved sleeping arcs)
        // Left eye
        drawArc(
            color = Color.Black,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.38f, h * 0.40f),
            size = Size(w * 0.07f, h * 0.06f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
        // Right eye
        drawArc(
            color = Color.Black,
            startAngle = 0f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(w * 0.55f, h * 0.40f),
            size = Size(w * 0.07f, h * 0.06f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        // 7. Draw Rosy Cheeks
        drawOval(
            color = Color(0xFFFDA4AF).copy(alpha = 0.7f), // rose-pink
            topLeft = Offset(w * 0.31f, h * 0.47f),
            size = Size(w * 0.06f, h * 0.04f)
        )
        drawOval(
            color = Color(0xFFFDA4AF).copy(alpha = 0.7f),
            topLeft = Offset(w * 0.63f, h * 0.47f),
            size = Size(w * 0.06f, h * 0.04f)
        )

        // 8. Draw Smile (cute curved line)
        drawArc(
            color = Color.Black,
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(w * 0.47f, h * 0.49f),
            size = Size(w * 0.06f, h * 0.06f),
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )
    }
}
