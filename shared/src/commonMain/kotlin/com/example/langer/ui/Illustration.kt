package com.example.langer.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun AnimatedUnicornCharacter(
    modifier: Modifier = Modifier
) {
    // 1. Periodic double-blink timer (loops every 4 seconds)
    // Synchronized with the physical bounce/sparkle animation
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(3000) // Keep eyes open during idle state (first 3 seconds)
            // Double-blink sequence during the bounce
            isBlinking = true
            delay(80)
            isBlinking = false
            delay(100)
            isBlinking = true
            delay(80)
            isBlinking = false
            delay(740) // Wait for the rest of the 4-second cycle to finish
        }
    }

    // 2. Infinite transition for keyframe-driven periodic animations
    val infiniteTransition = rememberInfiniteTransition()

    // Bounce vertical offset (moves up/down only during 3000ms to 4000ms)
    val bobbingOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3000 with FastOutSlowInEasing
                -16f at 3300 with FastOutSlowInEasing
                4f at 3650 with FastOutSlowInEasing
                0f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    // Squash & Stretch X-scaling factor (triggers during bounce)
    val scaleX by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3000 with FastOutSlowInEasing
                1.08f at 3150 with FastOutSlowInEasing // Squashes wide before jump
                0.92f at 3400 with FastOutSlowInEasing // Stretches narrow in mid-air
                1.06f at 3700 with FastOutSlowInEasing // Squashes wide on impact landing
                1f at 3900 with FastOutSlowInEasing
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    // Squash & Stretch Y-scaling factor (triggers during bounce)
    val scaleY by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1f at 0
                1f at 3000 with FastOutSlowInEasing
                0.90f at 3150 with FastOutSlowInEasing // Squashes short before jump
                1.12f at 3400 with FastOutSlowInEasing // Stretches tall in mid-air
                0.92f at 3700 with FastOutSlowInEasing // Squashes short on impact landing
                1f at 3900 with FastOutSlowInEasing
                1f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    // Horn glowing aura radius (flares up only during the bounce)
    val hornGlowRadius by infiniteTransition.animateFloat(
        initialValue = 5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                5f at 0
                5f at 3000 with FastOutSlowInEasing
                38f at 3300 with FastOutSlowInEasing // Glowing peak
                5f at 3750 with FastOutSlowInEasing
                5f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    // Star rotation angle (spins rapidly during the bounce)
    val starRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3000 with FastOutSlowInEasing
                360f at 3800 with LinearEasing // Spins 360 deg during bounce
                360f at 4000
            },
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Colors
        val white = Color.White
        val black = Color.Black
        val hornColor = Color(0xFFFDE047) // Gold
        val snoutColor = Color(0xFFFFE4E6) // Soft pink
        val cheekColor = Color(0xFFFDA4AF).copy(alpha = 0.8f)
        val manePurple = Color(0xFFC084FC)
        val maneFuchsia = Color(0xFFF472B6)
        val maneCyan = Color(0xFF22D3EE)
        val hornGlowColor = Color(0xFFFEF08A).copy(alpha = 0.5f) // Glowing yellow

        // Base pivot for Squash & Stretch (at the bottom-center of the character)
        val pivot = Offset(w * 0.5f, h * 0.76f)

        // Apply Squash & Stretch and Bounce Translation to all drawing operations inside
        scale(scaleX, scaleY, pivot) {
            translate(top = bobbingOffset) {
                // 1. Draw Mane (Hair behind head)
                // Lock 1 (Left-back)
                drawOval(
                    color = manePurple,
                    topLeft = Offset(w * 0.20f, h * 0.35f),
                    size = Size(w * 0.16f, h * 0.25f)
                )
                drawOval(
                    color = black,
                    topLeft = Offset(w * 0.20f, h * 0.35f),
                    size = Size(w * 0.16f, h * 0.25f),
                    style = Stroke(width = 5f)
                )

                // Lock 2 (Left-bottom)
                drawOval(
                    color = maneFuchsia,
                    topLeft = Offset(w * 0.24f, h * 0.52f),
                    size = Size(w * 0.14f, h * 0.20f)
                )
                drawOval(
                    color = black,
                    topLeft = Offset(w * 0.24f, h * 0.52f),
                    size = Size(w * 0.14f, h * 0.20f),
                    style = Stroke(width = 5f)
                )

                // Lock 3 (Right-back)
                drawOval(
                    color = maneCyan,
                    topLeft = Offset(w * 0.62f, h * 0.42f),
                    size = Size(w * 0.15f, h * 0.22f)
                )
                drawOval(
                    color = black,
                    topLeft = Offset(w * 0.62f, h * 0.42f),
                    size = Size(w * 0.15f, h * 0.22f),
                    style = Stroke(width = 5f)
                )

                // 2. Draw Ears
                // Left Ear Outer
                val leftEarPath = Path().apply {
                    moveTo(w * 0.36f, h * 0.36f)
                    lineTo(w * 0.28f, h * 0.16f)
                    lineTo(w * 0.44f, h * 0.32f)
                    close()
                }
                drawPath(path = leftEarPath, color = white)
                drawPath(path = leftEarPath, color = black, style = Stroke(width = 5f))

                // Left Ear Inner (Pink)
                val leftEarInnerPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.33f)
                    lineTo(w * 0.30f, h * 0.20f)
                    lineTo(w * 0.40f, h * 0.30f)
                    close()
                }
                drawPath(path = leftEarInnerPath, color = snoutColor)

                // Right Ear Outer
                val rightEarPath = Path().apply {
                    moveTo(w * 0.64f, h * 0.36f)
                    lineTo(w * 0.72f, h * 0.16f)
                    lineTo(w * 0.56f, h * 0.32f)
                    close()
                }
                drawPath(path = rightEarPath, color = white)
                drawPath(path = rightEarPath, color = black, style = Stroke(width = 5f))

                // Right Ear Inner (Pink)
                val rightEarInnerPath = Path().apply {
                    moveTo(w * 0.65f, h * 0.33f)
                    lineTo(w * 0.70f, h * 0.20f)
                    lineTo(w * 0.60f, h * 0.30f)
                    close()
                }
                drawPath(path = rightEarInnerPath, color = snoutColor)

                // 3. Draw Main Head (Large white round oval)
                val headSize = Size(w * 0.44f, h * 0.44f)
                val headOffset = Offset(w * 0.28f, h * 0.30f)
                drawOval(
                    color = white,
                    topLeft = headOffset,
                    size = headSize
                )
                drawOval(
                    color = black,
                    topLeft = headOffset,
                    size = headSize,
                    style = Stroke(width = 5f)
                )

                // 4. Draw Snout (Overlapping oval)
                val snoutSize = Size(w * 0.34f, h * 0.20f)
                val snoutOffset = Offset(w * 0.33f, h * 0.52f)
                drawOval(
                    color = snoutColor,
                    topLeft = snoutOffset,
                    size = snoutSize
                )
                drawOval(
                    color = black,
                    topLeft = snoutOffset,
                    size = snoutSize,
                    style = Stroke(width = 5f)
                )

                // Nostrils (Two small dots)
                drawCircle(
                    color = black,
                    radius = 3.5f,
                    center = Offset(w * 0.45f, h * 0.58f)
                )
                drawCircle(
                    color = black,
                    radius = 3.5f,
                    center = Offset(w * 0.55f, h * 0.58f)
                )

                // Cute Mouth (Tiny smile arc)
                drawArc(
                    color = black,
                    startAngle = 0f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(w * 0.47f, h * 0.61f),
                    size = Size(w * 0.06f, h * 0.05f),
                    style = Stroke(width = 4f, cap = StrokeCap.Round)
                )

                // 5. Draw Eyes
                if (isBlinking) {
                    // Blinking eyes: draw sleeping curves
                    drawArc(
                        color = black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.36f, h * 0.42f),
                        size = Size(w * 0.08f, h * 0.06f),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = black,
                        startAngle = 0f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.56f, h * 0.42f),
                        size = Size(w * 0.08f, h * 0.06f),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                } else {
                    // Open cute eyes (Black ovals with white catchlights)
                    val eyeW = w * 0.08f
                    val eyeH = h * 0.11f
                    drawOval(
                        color = black,
                        topLeft = Offset(w * 0.36f, h * 0.39f),
                        size = Size(eyeW, eyeH)
                    )
                    drawOval(
                        color = black,
                        topLeft = Offset(w * 0.56f, h * 0.39f),
                        size = Size(eyeW, eyeH)
                    )
                    // Catchlights
                    drawCircle(
                        color = white,
                        radius = w * 0.02f,
                        center = Offset(w * 0.38f, h * 0.42f)
                    )
                    drawCircle(
                        color = white,
                        radius = w * 0.02f,
                        center = Offset(w * 0.58f, h * 0.42f)
                    )
                }

                // 6. Rosy Cheeks
                drawOval(
                    color = cheekColor,
                    topLeft = Offset(w * 0.30f, h * 0.48f),
                    size = Size(w * 0.07f, h * 0.04f)
                )
                drawOval(
                    color = cheekColor,
                    topLeft = Offset(w * 0.63f, h * 0.48f),
                    size = Size(w * 0.07f, h * 0.04f)
                )

                // 7. Magic Horn Glowing Aura
                drawCircle(
                    color = hornGlowColor,
                    radius = hornGlowRadius,
                    center = Offset(w * 0.50f, h * 0.15f)
                )

                // 8. Draw Magic Horn (Pointy Triangle)
                val hornPath = Path().apply {
                    moveTo(w * 0.46f, h * 0.31f)
                    lineTo(w * 0.50f, h * 0.08f)
                    lineTo(w * 0.54f, h * 0.31f)
                    close()
                }
                drawPath(path = hornPath, color = hornColor)
                drawPath(path = hornPath, color = black, style = Stroke(width = 5f))

                // Horn ribs (curved segment lines)
                drawLine(
                    color = black,
                    start = Offset(w * 0.47f, h * 0.25f),
                    end = Offset(w * 0.53f, h * 0.25f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = black,
                    start = Offset(w * 0.48f, h * 0.18f),
                    end = Offset(w * 0.52f, h * 0.18f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )

                // 9. Floating Star near the horn (Magic!)
                val starCenter = Offset(w * 0.38f, h * 0.15f)
                val starPath = Path().apply {
                    val starR = w * 0.025f
                    val innerR = w * 0.01f
                    val degToRad = (PI / 180.0).toFloat()
                    for (i in 0 until 5) {
                        val angleRad = (i * 72 - 90 + starRotation) * degToRad
                        val x1 = starCenter.x + starR * cos(angleRad)
                        val y1 = starCenter.y + starR * sin(angleRad)
                        if (i == 0) moveTo(x1, y1) else lineTo(x1, y1)

                        val angleRad2 = (i * 72 + 36 - 90 + starRotation) * degToRad
                        val x2 = starCenter.x + innerR * cos(angleRad2)
                        val y2 = starCenter.y + innerR * sin(angleRad2)
                        lineTo(x2, y2)
                    }
                    close()
                }
                drawPath(path = starPath, color = Color(0xFFFDE047))
                drawPath(path = starPath, color = black, style = Stroke(width = 3f))
            }
        }
    }
}
