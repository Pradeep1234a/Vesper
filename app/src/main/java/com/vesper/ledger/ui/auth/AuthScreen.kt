package com.vesper.ledger.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.animation.core.*
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

object AuthValidator {
    fun isValidEmail(email: String): Boolean {
        val trimmed = email.trim()
        return trimmed.isNotEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(trimmed).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return password.length >= 8 && password.any { it.isDigit() } && password.any { it.isLetter() }
    }

    fun isValidFullName(name: String): Boolean {
        val trimmed = name.trim()
        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return parts.size >= 2 && parts.all { part ->
            part.length >= 2 && part.all { it.isLetter() || it == '-' || it == '\'' }
        }
    }
}

// ─── Custom Outlined TextField Matching ShTextField Exactly ──────────────────

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {},
    errorText: String? = null
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceColor = MaterialTheme.colorScheme.surface
    val borderUnfocused = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val borderFocused = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Medium,
                color = textColorSecondary,
                fontSize = 13.sp
            ),
            modifier = Modifier.padding(bottom = 6.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorSecondary.copy(alpha = 0.45f),
                        fontSize = 14.sp
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = textColorPrimary,
                fontSize = 15.sp
            ),
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = { onImeAction() },
                onDone = { onImeAction() }
            ),
            leadingIcon = if (leadingIcon != null) {
                {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = textColorPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else null,
            trailingIcon = if (isPassword) {
                {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Hide password" else "Show password",
                        tint = textColorPrimary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { passwordVisible = !passwordVisible }
                    )
                }
            } else null,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColorPrimary,
                unfocusedTextColor = textColorPrimary,
                focusedContainerColor = surfaceColor,
                unfocusedContainerColor = surfaceColor,
                focusedBorderColor = borderFocused,
                unfocusedBorderColor = borderUnfocused,
                cursorColor = textColorPrimary
            )
        )

        if (errorText != null) {
            Text(
                text = errorText,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )
        }
    }
}

// ─── Programmatic Premium Button (Curve & Hierarchy Aligned) ────────────────

@Composable
private fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scalePress by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pressScale"
    )

    val arrowOffset by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "arrowOffset"
    )

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground

    val containerColor = if (isPrimary) textColorPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val contentColor = if (isPrimary) backgroundColor else textColorPrimary
    val border = if (isPrimary) null else BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .scale(scalePress)
            .clip(RoundedCornerShape(22.dp))
            .background(containerColor)
            .then(
                if (border != null) Modifier.border(border, RoundedCornerShape(22.dp))
                else Modifier
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = rememberRipple(color = contentColor.copy(alpha = 0.15f)),
                onClick = onClick
            )
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)
                ),
                modifier = Modifier.align(Alignment.Center)
            )

            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(16.dp)
                    .offset(x = arrowOffset)
                    .align(Alignment.CenterEnd)
            ) {
                val strokeWidth = 1.5.dp.toPx()
                val color = if (enabled) contentColor else contentColor.copy(alpha = 0.5f)

                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(size.width - size.height / 3, size.height / 2 - size.height / 3),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )
                drawLine(
                    color = color,
                    start = Offset(size.width - size.height / 3, size.height / 2 + size.height / 3),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )
            }
        }
    }
}

// ─── Welcome Screen Premium Composables ───────────────────────────────────────

private enum class BenefitIconType { LOCK, CROSSHAIR, CIRCLE }

@Composable
private fun WelcomeBenefitCard(
    title: String,
    description: String,
    iconType: BenefitIconType,
    alpha: Float,
    translationY: Float
) {
    val textColorPrimary = MaterialTheme.colorScheme.onSurface
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    val cardBorderColor = MaterialTheme.colorScheme.outlineVariant
    val iconBgColor = MaterialTheme.colorScheme.surface
    val iconBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 76.dp)
            .graphicsLayer {
                this.alpha = alpha
                this.translationY = translationY
            }
            .clip(RoundedCornerShape(18.dp))
            .background(cardBgColor)
            .border(
                BorderStroke(1.dp, cardBorderColor),
                RoundedCornerShape(18.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBgColor)
                    .border(
                        BorderStroke(1.5.dp, iconBorderColor),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                    val stroke = 2.dp.toPx()
                    val color = textColorPrimary

                    when (iconType) {
                        BenefitIconType.LOCK -> {
                            drawArc(
                                color = color,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                style = Stroke(width = stroke),
                                topLeft = Offset(size.width * 0.25f, size.height * 0.15f),
                                size = Size(size.width * 0.5f, size.height * 0.5f)
                            )
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.45f),
                                size = Size(size.width * 0.7f, size.height * 0.45f),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                style = Stroke(width = stroke)
                            )
                            drawCircle(
                                color = color,
                                radius = 2.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.65f)
                            )
                        }
                        BenefitIconType.CROSSHAIR -> {
                            drawCircle(
                                color = color,
                                radius = 2.5.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                            drawCircle(
                                color = color,
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                style = Stroke(width = stroke)
                            )
                            val tick = 3.5.dp.toPx()
                            drawLine(color, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, tick), stroke)
                            drawLine(color, Offset(size.width * 0.5f, size.height), Offset(size.width * 0.5f, size.height - tick), stroke)
                            drawLine(color, Offset(0f, size.height * 0.5f), Offset(tick, size.height * 0.5f), stroke)
                            drawLine(color, Offset(size.width, size.height * 0.5f), Offset(size.width - tick, size.height * 0.5f), stroke)
                        }
                        BenefitIconType.CIRCLE -> {
                            drawCircle(
                                color = color,
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                style = Stroke(width = stroke)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColorPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 13.sp,
                        color = textColorSecondary,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun WelcomePremiumButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    PremiumButton(
        text = text,
        onClick = onClick,
        isPrimary = isPrimary,
        modifier = modifier
    )
}

// ─── Screen 1: Welcome ──────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var activeDialog by remember { mutableStateOf<String?>(null) }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Scaffold(
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VESPER LEDGER",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = textColorSecondary
                    )
                )
            }

            // Center Hero Welcome Section (Clean, Minimalist, No Clutter)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // App Logo Badge in Dashboard Curved Tile
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccountBalanceWallet,
                        contentDescription = "Vesper Logo",
                        tint = textColorPrimary,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Welcome to Vesper",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = textColorPrimary
                        ),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Master your money with total clarity, privacy, and effortless elegance.",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 15.sp,
                            color = textColorSecondary,
                            lineHeight = 22.sp
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Bottom Actions Section (Get Started CTA + Sign In Link)
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Primary CTA: Get Started (Dashboard-styled rounded button)
                Button(
                    onClick = onCreateAccountClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = textColorPrimary,
                        contentColor = backgroundColor
                    )
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                }

                // Secondary Link: Sign In
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            color = textColorSecondary
                        )
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Bold,
                            color = textColorPrimary,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable { onSignInClick() }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Legal Footer Links
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColorSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.clickable { activeDialog = "terms" }
                    )
                    Text(
                        text = "  •  ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColorSecondary.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColorSecondary.copy(alpha = 0.7f),
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.clickable { activeDialog = "privacy" }
                    )
                }
            }
        }
    }

    when (activeDialog) {
        "terms" -> {
            AuthInfoDialog(
                title = "Terms & Conditions",
                text = TERMS_CONDITIONS_TEXT,
                onDismissRequest = { activeDialog = null }
            )
        }
        "privacy" -> {
            AuthInfoDialog(
                title = "Privacy Policy",
                text = PRIVACY_POLICY_TEXT,
                onDismissRequest = { activeDialog = null }
            )
        }
    }
}

// ─── Screen 2: Sign In ──────────────────────────────────────────────────────

@Composable
fun SignInScreen(
    onBackClick: () -> Unit,
    onSignInClick: (String, String, (String?) -> Unit) -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val borderDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ChildHeader(
                title = "Sign In",
                onBackClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Elevated Card Panel matching Dashboard card margin & shape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBgColor)
                            .border(
                                BorderStroke(1.dp, borderDividerColor),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Card Header with Circular Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        .border(
                                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = textColorPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Welcome Back",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = textColorPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Sign in to continue managing your finances.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            color = textColorSecondary.copy(alpha = 0.75f),
                                            lineHeight = 17.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            AuthTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                placeholder = "you@example.com",
                                leadingIcon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                errorText = if (showErrors && !AuthValidator.isValidEmail(email)) "Please enter a valid email address." else null
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                placeholder = "••••••••",
                                leadingIcon = Icons.Outlined.Lock,
                                isPassword = true,
                                imeAction = ImeAction.Done,
                                onImeAction = { focusManager.clearFocus() },
                                errorText = if (showErrors && password.isEmpty()) "Password cannot be empty." else null
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Text(
                                    text = "Forgot Password?",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = SpaceGroteskFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColorSecondary,
                                        fontSize = 13.sp,
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = onForgotPasswordClick
                                    )
                                )
                            }
                        }
                    }

                    PremiumButton(
                        text = if (isLoading) "Signing In..." else "Sign In",
                        onClick = {
                            showErrors = true
                            if (!AuthValidator.isValidEmail(email) || password.isEmpty()) {
                                return@PremiumButton
                            }
                            isLoading = true
                            errorMessage = null
                            onSignInClick(email, password) { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        },
                        enabled = !isLoading,
                        isPrimary = true
                    )
                }
            }

            // Pinned Link at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onCreateAccountClick
                    ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColorSecondary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColorPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
        }
    }
}

// ─── Screen 3: Create Account ───────────────────────────────────────────────

@Composable
fun CreateAccountScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: (String, String, String, (String?) -> Unit) -> Unit,
    onSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val borderDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ChildHeader(
                title = "Create Account",
                onBackClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Elevated Card Panel matching Dashboard card margin & shape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBgColor)
                            .border(
                                BorderStroke(1.dp, borderDividerColor),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 14.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Card Header with Circular Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        .border(
                                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = textColorPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "Get Started",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 19.sp,
                                            color = textColorPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Create your secure Vesper Ledger account.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 12.sp,
                                            color = textColorSecondary.copy(alpha = 0.75f),
                                            lineHeight = 15.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            AuthTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = "Full Name",
                                placeholder = "e.g. John Doe",
                                leadingIcon = Icons.Outlined.Person,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                errorText = if (showErrors && !AuthValidator.isValidFullName(fullName)) "Please enter first and last name (letters only)." else null
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            AuthTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                placeholder = "you@example.com",
                                leadingIcon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                errorText = if (showErrors && !AuthValidator.isValidEmail(email)) "Please enter a valid email address." else null
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            AuthTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                placeholder = "Min 8 chars, letter & number",
                                leadingIcon = Icons.Outlined.Lock,
                                isPassword = true,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                errorText = if (showErrors && !AuthValidator.isValidPassword(password)) "Min 8 chars with 1 letter and 1 digit." else null
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            AuthTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = "Confirm Password",
                                placeholder = "••••••••",
                                leadingIcon = Icons.Outlined.Lock,
                                isPassword = true,
                                imeAction = ImeAction.Done,
                                onImeAction = { focusManager.clearFocus() },
                                errorText = if (showErrors && confirmPassword != password) "Passwords do not match." else null
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { termsAccepted = it },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = textColorPrimary,
                                        uncheckedColor = borderDividerColor,
                                        checkmarkColor = backgroundColor
                                    )
                                )
                                Text(
                                    text = "I accept the ",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColorSecondary, fontSize = 12.sp)
                                )
                                Text(
                                    text = "Terms",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textColorPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGroteskFamily,
                                        textDecoration = TextDecoration.Underline,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showTerms = true }
                                )
                                Text(
                                    text = " & ",
                                    style = MaterialTheme.typography.bodyMedium.copy(color = textColorSecondary, fontSize = 12.sp)
                                )
                                Text(
                                    text = "Privacy Policy",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textColorPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = SpaceGroteskFamily,
                                        textDecoration = TextDecoration.Underline,
                                        fontSize = 12.sp
                                    ),
                                    modifier = Modifier.clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) { showPrivacy = true }
                                )
                            }
                        }
                    }

                    PremiumButton(
                        text = if (isLoading) "Creating Account..." else "Create Account",
                        onClick = {
                            showErrors = true
                            if (!AuthValidator.isValidFullName(fullName) ||
                                !AuthValidator.isValidEmail(email) ||
                                !AuthValidator.isValidPassword(password) ||
                                confirmPassword != password
                            ) {
                                return@PremiumButton
                            }
                            if (!termsAccepted) {
                                errorMessage = "You must accept the Terms of Service & Privacy Policy."
                                return@PremiumButton
                            }
                            isLoading = true
                            errorMessage = null
                            onCreateAccountClick(fullName, email, password) { error ->
                                isLoading = false
                                errorMessage = error
                            }
                        },
                        enabled = !isLoading,
                        isPrimary = true
                    )
                }
            }

            // Pinned Link at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSignInClick
                    ),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColorSecondary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColorPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
        }

        if (showTerms) {
            AuthInfoDialog(
                title = "Terms & Conditions",
                text = TERMS_CONDITIONS_TEXT,
                onDismissRequest = { showTerms = false }
            )
        }
        if (showPrivacy) {
            AuthInfoDialog(
                title = "Privacy Policy",
                text = PRIVACY_POLICY_TEXT,
                onDismissRequest = { showPrivacy = false }
            )
        }
    }
}

// ─── Screen 4: Forgot Password ──────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendResetLinkClick: (String, String, (String?) -> Unit) -> Unit,
    onBackToSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var showErrors by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val cardBgColor = MaterialTheme.colorScheme.surface
    val borderDividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            ChildHeader(
                title = "Reset Password",
                onBackClick = onBackClick
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (statusMessage != null) {
                        Text(
                            text = statusMessage!!,
                            color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Elevated Card Panel matching Dashboard card margin & shape
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(cardBgColor)
                            .border(
                                BorderStroke(1.dp, borderDividerColor),
                                RoundedCornerShape(18.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 20.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Card Header with Circular Icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                        .border(
                                            BorderStroke(1.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)),
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Lock,
                                        contentDescription = null,
                                        tint = textColorPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Text(
                                        text = "Reset Password",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp,
                                            color = textColorPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Enter your email address and a new password to reset your login credentials.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            color = textColorSecondary.copy(alpha = 0.75f),
                                            lineHeight = 17.sp
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            AuthTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                placeholder = "you@example.com",
                                leadingIcon = Icons.Outlined.Email,
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                                onImeAction = { focusManager.moveFocus(FocusDirection.Down) },
                                errorText = if (showErrors && !AuthValidator.isValidEmail(email)) "Please enter a valid email address." else null
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            AuthTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = "New Password",
                                placeholder = "Min 8 chars, letter & number",
                                leadingIcon = Icons.Outlined.Lock,
                                isPassword = true,
                                imeAction = ImeAction.Done,
                                onImeAction = { focusManager.clearFocus() },
                                errorText = if (showErrors && !AuthValidator.isValidPassword(newPassword)) "Min 8 chars with 1 letter and 1 digit." else null
                            )
                        }
                    }

                    PremiumButton(
                        text = if (isLoading) "Resetting Password..." else "Reset Password",
                        onClick = {
                            showErrors = true
                            if (!AuthValidator.isValidEmail(email) || !AuthValidator.isValidPassword(newPassword)) {
                                return@PremiumButton
                            }
                            isLoading = true
                            statusMessage = null
                            onSendResetLinkClick(email, newPassword) { error ->
                                isLoading = false
                                if (error == null) {
                                    isSuccess = true
                                    statusMessage = "Password reset successfully!"
                                } else {
                                    isSuccess = false
                                    statusMessage = error
                                }
                            }
                        },
                        enabled = !isLoading,
                        isPrimary = true
                    )
                }
            }

            // Pinned Link at Bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Back to Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp,
                        textDecoration = TextDecoration.Underline
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackToSignInClick
                    )
                )
            }
        }
    }
}

// ─── Legal Documentation & Dialogue Composable ─────────────────────────────

private const val PRIVACY_POLICY_TEXT = """Your financial privacy is our highest priority.

1. Data Collection & Privacy
Vesper Ledger is a local-first application. We do not transmit, track, or share your financial data. All information stays safely stored on your device's offline database.

2. Database Security
Individual database files are created per account credentials on your local storage. You have full custody of your account records.

3. Third-Party Access
No third parties have access to your data. No analytical SDKs or background telemetry tools are installed."""

private const val TERMS_CONDITIONS_TEXT = """Vesper Ledger is provided as-is without any warranties of any kind.

1. Scope of Service
Vesper Ledger is provided as a local personal ledger utility. Users are responsible for backing up their own data.

2. Limitation of Liability
Under no circumstances shall Vesper Ledger be liable for direct, indirect, incidental, or consequential loss of data, funds, or savings.

3. Security Responsibility
You are solely responsible for maintaining the confidentiality of your master password and device security settings."""

@Composable
fun AuthInfoDialog(
    title: String,
    text: String,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        },
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                TextButton(onClick = onDismissRequest) {
                    Text("Close")
                }
            }
        }
    )
}
