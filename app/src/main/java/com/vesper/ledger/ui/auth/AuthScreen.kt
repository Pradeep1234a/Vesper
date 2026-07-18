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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.drawscope.Stroke
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

// ─── Custom Outlined TextField Matching ShTextField Exactly ──────────────────

@Composable
private fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    onImeAction: () -> Unit = {}
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = textColorSecondary
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
                        color = textColorSecondary.copy(alpha = 0.5f)
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColorPrimary),
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
            trailingIcon = if (isPassword) {
                {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Hide password" else "Show password",
                        tint = textColorSecondary,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { passwordVisible = !passwordVisible }
                    )
                }
            } else null,
            shape = MaterialTheme.shapes.small, // 6.dp curve matching buttons and inputs
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColorPrimary,
                unfocusedTextColor = textColorPrimary,
                focusedContainerColor = surfaceVariantColor.copy(alpha = 0.2f),
                unfocusedContainerColor = surfaceVariantColor.copy(alpha = 0.1f),
                focusedBorderColor = textColorPrimary,
                unfocusedBorderColor = outlineColor,
                cursorColor = textColorPrimary
            )
        )
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
    // Dynamic theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val outlineColor = MaterialTheme.colorScheme.outline

    val containerColor = if (isPrimary) textColorPrimary else Color.Transparent
    val contentColor = if (isPrimary) backgroundColor else textColorPrimary
    val border = if (isPrimary) null else BorderStroke(1.dp, outlineColor)

    Button(
        onClick = onClick,
        modifier = modifier
            .height(56.dp) // Touch target minimum requirement
            .fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.small, // 6.dp rounded corners matching the dashboard family shape
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.4f),
            disabledContentColor = contentColor.copy(alpha = 0.4f)
        ),
        border = border,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    letterSpacing = 0.2.sp
                ),
                modifier = Modifier.align(Alignment.Center)
            )

            // Custom thin right arrow matching mockup
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.CenterEnd)
            ) {
                val strokeWidth = 1.5.dp.toPx()
                val color = contentColor

                // Line
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )
                // Chevron top
                drawLine(
                    color = color,
                    start = Offset(size.width - size.height / 3, size.height / 2 - size.height / 3),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = strokeWidth
                )
                // Chevron bottom
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

// ─── Screen 1: Welcome ──────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = context.getSharedPreferences("vesper_settings", android.content.Context.MODE_PRIVATE)
    val appIcon = sharedPrefs.getString("appIcon", "default") ?: "default"
    val logoForegroundRes = com.vesper.ledger.data.secure.AppIconManager.getIconForegroundRes(appIcon)

    // Dynamic Theme Colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline
    val iconBgColor = textColorPrimary.copy(alpha = 0.06f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 1. Sleek brand header (Logo & App Name aligned flush with the left greeting edge)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 12.dp, end = 16.dp, top = 20.dp, bottom = 20.dp), // Start padding 12.dp offsets asset margin
            horizontalArrangement = Arrangement.spacedBy(6.dp), // Exact 6.dp gap requested
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = logoForegroundRes),
                contentDescription = "Vesper Brand Logo",
                tint = textColorPrimary,
                modifier = Modifier.size(28.dp) // Size matches the dashboard top header scale
            )
            Text(
                text = "Vesper Ledger",
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 22.sp, // Identical size to the dashboard top header title
                    fontWeight = FontWeight.Bold,
                    color = textColorPrimary,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = false // Ensures perfect vertical alignment with logo
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // 2. Large Serif Heading & Paragraph
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome.",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Normal,
                        fontSize = 52.sp,
                        color = textColorPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "A private space built for thoughtful money management.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = textColorSecondary,
                        lineHeight = 24.sp,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Minimal horizontal line
                Divider(
                    color = outlineColor,
                    thickness = 1.dp,
                    modifier = Modifier.width(60.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // 3. Trust Highlights (Editorial rows)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Row 1: Private by Design
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(iconBgColor, shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                            val stroke = 1.5.dp.toPx()
                            val color = textColorPrimary
                            // Shackle
                            drawArc(
                                color = color,
                                startAngle = 180f,
                                sweepAngle = 180f,
                                useCenter = false,
                                style = Stroke(width = stroke),
                                topLeft = Offset(size.width * 0.25f, size.height * 0.15f),
                                size = Size(size.width * 0.5f, size.height * 0.5f)
                            )
                            // Body
                            drawRoundRect(
                                color = color,
                                topLeft = Offset(size.width * 0.15f, size.height * 0.45f),
                                size = Size(size.width * 0.7f, size.height * 0.45f),
                                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                                style = Stroke(width = stroke)
                            )
                            // Dot
                            drawCircle(
                                color = color,
                                radius = 1.5.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.65f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Your Money. Your Rules.",
                            color = textColorPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your financial life stays entirely yours.",
                            color = textColorSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Divider(color = outlineColor.copy(alpha = 0.5f), thickness = 1.dp)

                // Row 2: Built for Intention
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(iconBgColor, shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                            val stroke = 1.5.dp.toPx()
                            val color = textColorPrimary
                            drawCircle(
                                color = color,
                                radius = 2.dp.toPx(),
                                center = Offset(size.width * 0.5f, size.height * 0.5f)
                            )
                            drawCircle(
                                color = color,
                                radius = size.width * 0.35f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                style = Stroke(width = stroke)
                            )
                            val tick = 3.dp.toPx()
                            drawLine(color, Offset(size.width * 0.5f, 0f), Offset(size.width * 0.5f, tick), stroke)
                            drawLine(color, Offset(size.width * 0.5f, size.height), Offset(size.width * 0.5f, size.height - tick), stroke)
                            drawLine(color, Offset(0f, size.height * 0.5f), Offset(tick, size.height * 0.5f), stroke)
                            drawLine(color, Offset(size.width, size.height * 0.5f), Offset(size.width - tick, size.height * 0.5f), stroke)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Slow Down. Notice More.",
                            color = textColorPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Recording each expense builds lasting awareness.",
                            color = textColorSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Divider(color = outlineColor.copy(alpha = 0.5f), thickness = 1.dp)

                // Row 3: Clarity in Simplicity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(iconBgColor, shape = androidx.compose.foundation.shape.CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                            val stroke = 1.5.dp.toPx()
                            val color = textColorPrimary
                            drawCircle(
                                color = color,
                                radius = size.width * 0.4f,
                                center = Offset(size.width * 0.5f, size.height * 0.5f),
                                style = Stroke(width = stroke)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Less Noise. More Clarity.",
                            color = textColorPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "A calm space to understand your finances.",
                            color = textColorSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.height(32.dp))

            // 4. Action Buttons & Links (Tuned curves and margins)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PremiumButton(
                    text = "Create Account",
                    onClick = onCreateAccountClick,
                    isPrimary = true
                )

                PremiumButton(
                    text = "Sign In",
                    onClick = onSignInClick,
                    isPrimary = false
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColorSecondary.copy(alpha = 0.8f),
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Terms click */ }
                    )
                    Text(
                        text = "   ·   ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = outlineColor
                        )
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textColorSecondary.copy(alpha = 0.8f),
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Privacy click */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Leaves space for bottom pinned link
            verticalArrangement = Arrangement.Top
        ) {
            ChildHeader(
                title = "Sign In",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            ShCard(
                modifier = Modifier.fillMaxWidth(),
                borderStroke = BorderStroke(1.dp, outlineColor)
            ) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign in to continue managing your finances.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorSecondary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = textColorSecondary,
                            fontWeight = FontWeight.SemiBold,
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

            Spacer(modifier = Modifier.height(28.dp))

            PremiumButton(
                text = if (isLoading) "Signing In..." else "Sign In",
                onClick = {
                    isLoading = true
                    errorMessage = null
                    onSignInClick(email, password) { error ->
                        isLoading = false
                        errorMessage = error
                    }
                },
                enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                isPrimary = true
            )
        }

        // Pin the link to the absolute bottom of the viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
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
                        color = textColorSecondary
                    )
                )
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
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
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Leaves space for bottom pinned link
            verticalArrangement = Arrangement.Top
        ) {
            ChildHeader(
                title = "Create Account",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            ShCard(
                modifier = Modifier.fillMaxWidth(),
                borderStroke = BorderStroke(1.dp, outlineColor)
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create your secure Vesper Ledger account.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorSecondary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = "Full Name",
                    placeholder = "Your full name",
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Terms of Service and Privacy Policy checkbox
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = textColorPrimary,
                            uncheckedColor = outlineColor,
                            checkmarkColor = backgroundColor
                        )
                    )
                    Text(
                        text = "I accept the Terms of Service & Privacy Policy",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColorSecondary
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { termsAccepted = !termsAccepted }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            PremiumButton(
                text = if (isLoading) "Creating Account..." else "Create Account",
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match."
                        return@PremiumButton
                    }
                    isLoading = true
                    errorMessage = null
                    onCreateAccountClick(fullName, email, password) { error ->
                        isLoading = false
                        errorMessage = error
                    }
                },
                enabled = fullName.isNotBlank() && email.isNotBlank() &&
                        password.isNotBlank() && confirmPassword.isNotBlank() &&
                        termsAccepted && !isLoading,
                isPrimary = true
            )
        }

        // Pin the link to the absolute bottom of the viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
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
                        color = textColorSecondary
                    )
                )
                Text(
                    text = "Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorPrimary,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                )
            }
        }
    }
}

// ─── Screen 4: Forgot Password ──────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendResetLinkClick: (String, String, (String?) -> Unit) -> Unit, // Uses (email, newPassword, callback)
    onBackToSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColorPrimary = MaterialTheme.colorScheme.onBackground
    val textColorSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp), // Leaves space for bottom pinned link
            verticalArrangement = Arrangement.Top
        ) {
            ChildHeader(
                title = "Reset Password",
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (statusMessage != null) {
                Text(
                    text = statusMessage!!,
                    color = if (isSuccess) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                )
            }

            ShCard(
                modifier = Modifier.fillMaxWidth(),
                borderStroke = BorderStroke(1.dp, outlineColor)
            ) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColorPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your email address and a new password to reset your login credentials.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColorSecondary
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next,
                    onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AuthTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = "New Password",
                    placeholder = "••••••••",
                    isPassword = true,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            PremiumButton(
                text = if (isLoading) "Resetting Password..." else "Reset Password",
                onClick = {
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
                enabled = email.isNotBlank() && newPassword.isNotBlank() && !isLoading,
                isPrimary = true
            )
        }

        // Pin the link to the absolute bottom of the viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Back to Sign In",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = textColorPrimary,
                    fontWeight = FontWeight.Bold,
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
