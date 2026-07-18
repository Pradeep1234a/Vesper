package com.vesper.ledger.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

// ─── Shared Auth Components ─────────────────────────────────────────────────

private val AuthInputShape = RoundedCornerShape(20.dp)
private val AuthButtonShape = RoundedCornerShape(20.dp)

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

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            textStyle = TextStyle(
                fontFamily = PlusJakartaSansFamily,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
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
            trailingIcon = if (isPassword) {
                {
                    Icon(
                        imageVector = if (passwordVisible)
                            Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible)
                            "Hide password" else "Show password",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(22.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { passwordVisible = !passwordVisible }
                    )
                }
            } else null,
            shape = AuthInputShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        shape = AuthButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = SpaceGroteskFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
        )
    }
}

@Composable
private fun AuthTextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Text(
        text = text,
        style = TextStyle(
            fontFamily = PlusJakartaSansFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = onClick
        )
    )
}

@Composable
private fun AuthBackHeader(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Back",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .padding(start = 16.dp)
                .size(28.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBackClick() }
        )
    }
}

// ─── Screen 1: Welcome ──────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(120.dp))

            // Heading
            Text(
                text = "Welcome to",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )
            Text(
                text = "Vesper Ledger",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Subtitle
            Text(
                text = "Take control of every transaction\nwith clarity and confidence.",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Primary Action
            AuthPrimaryButton(
                text = "Create Account",
                onClick = onCreateAccountClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary Action
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AuthTextLink(
                    text = "Sign In",
                    onClick = onSignInClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terms of Service",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Terms click */ }
                )
                Text(
                    text = "  ·  ",
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                )
                Text(
                    text = "Privacy Policy",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Privacy click */ }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ─── Screen 2: Sign In ──────────────────────────────────────────────────────

@Composable
fun SignInScreen(
    onBackClick: () -> Unit,
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Back Header
        AuthBackHeader(onBackClick = onBackClick)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Heading
            Text(
                text = "Welcome\nBack",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Sign in to continue managing your finances.",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form
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
                placeholder = "Enter your password",
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Forgot Password
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                AuthTextLink(
                    text = "Forgot Password?",
                    onClick = onForgotPasswordClick
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action
            AuthPrimaryButton(
                text = "Sign In",
                onClick = onSignInClick,
                enabled = email.isNotBlank() && password.isNotBlank()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Don't have an account?",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                AuthTextLink(
                    text = "Create Account",
                    onClick = onCreateAccountClick,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Screen 3: Create Account ───────────────────────────────────────────────

@Composable
fun CreateAccountScreen(
    onBackClick: () -> Unit,
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Back Header
        AuthBackHeader(onBackClick = onBackClick)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Heading
            Text(
                text = "Create\nAccount",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Create your secure Vesper Ledger account.",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form
            AuthTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = "Full Name",
                placeholder = "Your full name",
                keyboardType = KeyboardType.Text,
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
                placeholder = "Create a password",
                isPassword = true,
                imeAction = ImeAction.Next,
                onImeAction = { focusManager.moveFocus(FocusDirection.Down) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm Password",
                placeholder = "Confirm your password",
                isPassword = true,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action
            AuthPrimaryButton(
                text = "Create Account",
                onClick = onCreateAccountClick,
                enabled = fullName.isNotBlank() && email.isNotBlank() &&
                        password.isNotBlank() && confirmPassword.isNotBlank()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Already have an account?",
                    style = TextStyle(
                        fontFamily = PlusJakartaSansFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                AuthTextLink(
                    text = "Sign In",
                    onClick = onSignInClick,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Screen 4: Forgot Password ──────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendResetLinkClick: () -> Unit,
    onBackToSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .navigationBarsPadding()
    ) {
        // Back Header
        AuthBackHeader(onBackClick = onBackClick)

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Heading
            Text(
                text = "Forgot\nPassword",
                style = TextStyle(
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 44.sp,
                    letterSpacing = (-0.5).sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = "Enter your email address and we'll send you a password reset link.",
                style = TextStyle(
                    fontFamily = PlusJakartaSansFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
                onImeAction = { focusManager.clearFocus() }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Action
            AuthPrimaryButton(
                text = "Send Reset Link",
                onClick = onSendResetLinkClick,
                enabled = email.isNotBlank()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AuthTextLink(
                    text = "Back to Sign In",
                    onClick = onBackToSignInClick,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
