package com.vesper.ledger.ui.navigation

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import java.util.Calendar

@Composable
fun OnboardingScreen(
    onNavigateNext: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    val isDark = isSystemInDarkTheme()
    val bgColor = if (isDark) Color.Black else Color.White
    val contentColor = if (isDark) Color.White else Color.Black
    val secTextColor = if (isDark) Color.Gray else Color(0xFF666666)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Logo Area (64dp height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LogoV(color = contentColor)
                Text(
                    text = "Vesper",
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // 2. Hero Illustration Area (45% of screen height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = currentPage, label = "IllustrationTransition") { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when (page) {
                        0 -> TransactionManagementIllustration(color = contentColor)
                        1 -> AnalyticsIllustration(color = contentColor)
                        2 -> PrivacySecurityIllustration(color = contentColor)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 3. Title Area (96dp fixed height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = currentPage, label = "TitleTransition") { page ->
                val title = when (page) {
                    0 -> "Track Every\nTransaction"
                    1 -> "Understand Your\nSpending"
                    else -> "Your Data\nStays Yours"
                }
                Text(
                    text = title,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 38.sp
                )
            }
        }

        // 4. Description Area (72dp fixed height)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Crossfade(targetState = currentPage, label = "DescriptionTransition") { page ->
                val desc = when (page) {
                    0 -> "Add expenses, income, and transfers in seconds with a clean and distraction-free workflow."
                    1 -> "Explore trends, categories, and spending patterns with powerful analytics designed for clarity."
                    else -> "Everything is stored securely on your device without requiring an online account or cloud storage."
                }
                Text(
                    text = desc,
                    fontSize = 15.sp,
                    color = secTextColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.fillMaxWidth(0.85f)
                )
            }
        }

        // 5. Pagination Area (32dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) contentColor else contentColor.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // 6. Primary Action Area (72dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Button(
                onClick = {
                    if (currentPage < 2) {
                        currentPage++
                    } else {
                        onNavigateNext()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = contentColor,
                    contentColor = bgColor
                )
            ) {
                val btnText = when (currentPage) {
                    0 -> "Next →"
                    1 -> "Next →"
                    else -> "Get Started →"
                }
                Text(
                    text = btnText,
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun LogoV(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val strokeWidth = 2.dp.toPx()

            // Left branch of Y/V
            drawLine(
                color = color,
                start = Offset(w * 0.18f, h * 0.18f),
                end = Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Right branch of Y/V
            drawLine(
                color = color,
                start = Offset(w * 0.82f, h * 0.18f),
                end = Offset(w * 0.5f, h * 0.58f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Stem of Y/V
            drawLine(
                color = color,
                start = Offset(w * 0.5f, h * 0.58f),
                end = Offset(w * 0.5f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Horizontal baseline
            drawLine(
                color = color,
                start = Offset(w * 0.22f, h * 0.82f),
                end = Offset(w * 0.78f, h * 0.82f),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Dot
            drawCircle(
                color = color,
                radius = 2.dp.toPx(),
                center = Offset(w * 0.5f, h * 0.28f)
            )
        }
    }
}

@Composable
fun TransactionManagementIllustration(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()

        // 1. Draw Receipt card (right side)
        val recWidth = width * 0.35f
        val recHeight = height * 0.6f
        val recLeft = width * 0.55f
        val recTop = height * 0.2f
        drawRoundRect(
            color = color,
            topLeft = Offset(recLeft, recTop),
            size = Size(recWidth, recHeight),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        // Receipt line items
        for (i in 0 until 4) {
            val y = recTop + recHeight * 0.25f + i * recHeight * 0.15f
            drawLine(
                color = color.copy(alpha = if (i == 3) 0.3f else 0.7f),
                start = Offset(recLeft + recWidth * 0.15f, y),
                end = Offset(recLeft + recWidth * if (i == 2) 0.5f else 0.85f, y),
                strokeWidth = strokeWidth * 0.7f,
                cap = StrokeCap.Round
            )
        }

        // 2. Draw Wallet card (left side)
        val walWidth = width * 0.4f
        val walHeight = height * 0.3f
        val walLeft = width * 0.1f
        val walTop = height * 0.35f
        drawRoundRect(
            color = color,
            topLeft = Offset(walLeft, walTop),
            size = Size(walWidth, walHeight),
            cornerRadius = CornerRadius(16.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )
        // Draw card accent details
        drawCircle(
            color = color,
            radius = 16.dp.toPx(),
            center = Offset(walLeft + walWidth * 0.3f, walTop + walHeight * 0.5f),
            style = Stroke(width = strokeWidth)
        )
        drawLine(
            color = color,
            start = Offset(walLeft + walWidth * 0.23f, walTop + walHeight * 0.5f),
            end = Offset(walLeft + walWidth * 0.37f, walTop + walHeight * 0.5f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = color,
            start = Offset(walLeft + walWidth * 0.3f, walTop + walHeight * 0.35f),
            end = Offset(walLeft + walWidth * 0.3f, walTop + walHeight * 0.65f),
            strokeWidth = strokeWidth
        )

        // 3. Draw transaction flow lines
        val path = Path().apply {
            moveTo(walLeft + walWidth, walTop + walHeight * 0.4f)
            cubicTo(
                width * 0.48f, walTop + walHeight * 0.4f,
                width * 0.48f, recTop + recHeight * 0.15f,
                recLeft, recTop + recHeight * 0.15f
            )
            moveTo(walLeft + walWidth, walTop + walHeight * 0.7f)
            cubicTo(
                width * 0.52f, walTop + walHeight * 0.7f,
                width * 0.52f, recTop + recHeight * 0.85f,
                recLeft, recTop + recHeight * 0.85f
            )
        }
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth * 0.8f, cap = StrokeCap.Round)
        )

        // Dot nodes on flow
        drawCircle(
            color = color,
            radius = 4.dp.toPx(),
            center = Offset(width * 0.48f, height * 0.45f)
        )
    }
}

@Composable
fun AnalyticsIllustration(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()

        // 1. Draw Donut Chart (top right)
        val donutCenter = Offset(width * 0.75f, height * 0.3f)
        val donutRadius = width * 0.15f
        drawCircle(
            color = color.copy(alpha = 0.15f),
            radius = donutRadius,
            center = donutCenter,
            style = Stroke(width = strokeWidth * 3)
        )
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 240f,
            useCenter = false,
            topLeft = Offset(donutCenter.x - donutRadius, donutCenter.y - donutRadius),
            size = Size(donutRadius * 2, donutRadius * 2),
            style = Stroke(width = strokeWidth * 3, cap = StrokeCap.Round)
        )

        // 2. Draw coordinate axis & line graph (left side)
        val axisLeft = width * 0.1f
        val axisBottom = height * 0.65f
        val axisWidth = width * 0.5f
        val axisHeight = height * 0.4f

        // Draw graph grid lines
        for (i in 0 until 4) {
            val y = axisBottom - i * axisHeight / 3f
            drawLine(
                color = color.copy(alpha = 0.08f),
                start = Offset(axisLeft, y),
                end = Offset(axisLeft + axisWidth, y),
                strokeWidth = strokeWidth * 0.5f
            )
        }

        // Draw line graph path
        val graphPath = Path().apply {
            moveTo(axisLeft, axisBottom - axisHeight * 0.1f)
            cubicTo(
                axisLeft + axisWidth * 0.25f, axisBottom - axisHeight * 0.6f,
                axisLeft + axisWidth * 0.4f, axisBottom - axisHeight * 0.2f,
                axisLeft + axisWidth * 0.65f, axisBottom - axisHeight * 0.85f
            )
            cubicTo(
                axisLeft + axisWidth * 0.8f, axisBottom - axisHeight * 0.95f,
                axisLeft + axisWidth * 0.9f, axisBottom - axisHeight * 0.5f,
                axisLeft + axisWidth, axisBottom - axisHeight * 0.9f
            )
        }
        drawPath(
            path = graphPath,
            color = color,
            style = Stroke(width = strokeWidth * 1.2f, cap = StrokeCap.Round)
        )

        // 3. Draw Heatmap grid (bottom right / center)
        val gridRows = 3
        val gridCols = 6
        val gridLeft = width * 0.45f
        val gridTop = height * 0.72f
        val cellSize = 12.dp.toPx()
        val spacing = 4.dp.toPx()

        val opacities = listOf(
            listOf(0.1f, 0.4f, 0.2f, 0.8f, 0.1f, 0.3f),
            listOf(0.3f, 0.1f, 0.9f, 0.4f, 0.2f, 0.6f),
            listOf(0.6f, 0.8f, 0.2f, 0.1f, 0.7f, 0.2f)
        )

        for (r in 0 until gridRows) {
            for (c in 0 until gridCols) {
                val left = gridLeft + c * (cellSize + spacing)
                val top = gridTop + r * (cellSize + spacing)
                val alpha = opacities.getOrNull(r)?.getOrNull(c) ?: 0.2f
                drawRoundRect(
                    color = color.copy(alpha = alpha),
                    topLeft = Offset(left, top),
                    size = Size(cellSize, cellSize),
                    cornerRadius = CornerRadius(3.dp.toPx())
                )
            }
        }
    }
}

@Composable
fun PrivacySecurityIllustration(
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 2.dp.toPx()

        // 1. Draw Server/Local storage box in isometric view
        val centerX = width * 0.5f
        val centerY = height * 0.5f
        val dx = width * 0.22f
        val dy = height * 0.12f

        val topFace = Path().apply {
            moveTo(centerX, centerY - dy)
            lineTo(centerX + dx, centerY)
            lineTo(centerX, centerY + dy)
            lineTo(centerX - dx, centerY)
            close()
        }
        drawPath(
            path = topFace,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        val dh = height * 0.28f
        val leftFace = Path().apply {
            moveTo(centerX - dx, centerY)
            lineTo(centerX - dx, centerY + dh)
            lineTo(centerX, centerY + dy + dh)
            lineTo(centerX, centerY + dy)
            close()
        }
        val rightFace = Path().apply {
            moveTo(centerX, centerY + dy)
            lineTo(centerX, centerY + dy + dh)
            lineTo(centerX + dx, centerY + dh)
            lineTo(centerX + dx, centerY)
            close()
        }
        drawPath(
            path = leftFace,
            color = color,
            style = Stroke(width = strokeWidth)
        )
        drawPath(
            path = rightFace,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        for (i in 1..2) {
            val yOffset = i * dh / 3f
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(centerX - dx + dx * 0.2f, centerY + yOffset),
                end = Offset(centerX - dx * 0.2f, centerY + dy + yOffset),
                strokeWidth = strokeWidth * 0.8f
            )
            drawLine(
                color = color.copy(alpha = 0.5f),
                start = Offset(centerX + dx * 0.2f, centerY + dy + yOffset),
                end = Offset(centerX + dx - dx * 0.2f, centerY + yOffset),
                strokeWidth = strokeWidth * 0.8f
            )
        }

        // 2. Draw Lock/Keyhole on front center face
        val lockCenter = Offset(centerX, centerY + dy + dh * 0.4f)
        drawCircle(
            color = color,
            radius = 6.dp.toPx(),
            center = lockCenter,
            style = Stroke(width = strokeWidth)
        )
        val keyPath = Path().apply {
            moveTo(lockCenter.x - 2.dp.toPx(), lockCenter.y + 4.dp.toPx())
            lineTo(lockCenter.x + 2.dp.toPx(), lockCenter.y + 4.dp.toPx())
            lineTo(lockCenter.x + 3.dp.toPx(), lockCenter.y + 16.dp.toPx())
            lineTo(lockCenter.x - 3.dp.toPx(), lockCenter.y + 16.dp.toPx())
            close()
        }
        drawPath(
            path = keyPath,
            color = color
        )

        // 3. Draw flow connections / orbits around the storage box
        drawArc(
            color = color.copy(alpha = 0.25f),
            startAngle = 10f,
            sweepAngle = 160f,
            useCenter = false,
            topLeft = Offset(centerX - dx * 1.3f, centerY - dy * 1.5f),
            size = Size(dx * 2.6f, dy * 4f),
            style = Stroke(width = strokeWidth * 0.7f, pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )
    }
}
