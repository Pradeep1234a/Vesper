package com.vesper.ledger.ui.auth

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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.components.ChildHeader
import com.vesper.ledger.ui.components.DynamicLogo
import com.vesper.ledger.ui.components.RootHeader
import com.vesper.ledger.ui.components.ShButton
import com.vesper.ledger.ui.components.ShCard

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

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                )
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyMedium,
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
                            .size(20.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { passwordVisible = !passwordVisible }
                    )
                }
            } else null,
            shape = MaterialTheme.shapes.small, // Matches 6.dp small shape!
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.onBackground, // Monochrome focus
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }
}

// ─── Screen 1: Welcome ──────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val sharedPrefs = context.getSharedPreferences("vesper_settings", android.content.Context.MODE_PRIVATE)
    val appIcon = sharedPrefs.getString("appIcon", "default") ?: "default"
    val logoForegroundRes = com.vesper.ledger.data.secure.AppIconManager.getIconForegroundRes(appIcon)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Clean brand logo header matching onboarding style
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = logoForegroundRes),
                contentDescription = "Vesper Brand Logo",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "Vesper Ledger",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontFamily = SpaceGroteskFamily
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Ledger Preview Card (Engaging UI Mockup that uses vertical space)
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PREVIEW LEDGER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(MaterialTheme.colorScheme.onBackground, shape = androidx.compose.foundation.shape.CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "$12,480.00",
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(
                                    MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                    shape = MaterialTheme.shapes.small
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "☕", fontSize = 16.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Vesper Brew Co.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Food & Drink",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        Text(
                            text = "-$4.50",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    }
                }
            }

            // 3. Descriptive Welcoming Card
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Private finance, without the noise.",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "A calm space to track, plan, and understand your finances. Your data stays entirely on your device.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Connect CTAs & Links tightly inside a card/container
            ShCard(modifier = Modifier.fillMaxWidth()) {
                ShButton(
                    text = "Create Account",
                    onClick = onCreateAccountClick,
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(12.dp))

                ShButton(
                    text = "Sign In",
                    onClick = onSignInClick,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* Terms click */ }
                    )
                    Text(
                        text = "  ·  ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    )
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    ) {
        ChildHeader(
            title = "Sign In",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Single card container holds all fields, primary CTA and link navigation for visual continuity
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Welcome Back",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sign in to continue managing your finances.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                // Forgot Password link placed directly underneath the password field
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                Spacer(modifier = Modifier.height(28.dp))

                // Sign In Button placed inside the card to feel cohesive and connected
                ShButton(
                    text = "Sign In",
                    onClick = onSignInClick,
                    enabled = email.isNotBlank() && password.isNotBlank(),
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Underlined redirection link centered inside the card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onCreateAccountClick
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
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
    ) {
        ChildHeader(
            title = "Create Account",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Form container card holding fields and action items together
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Create your secure Vesper Ledger account.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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

                Spacer(modifier = Modifier.height(28.dp))

                // Action button inside card
                ShButton(
                    text = "Create Account",
                    onClick = onCreateAccountClick,
                    enabled = fullName.isNotBlank() && email.isNotBlank() &&
                            password.isNotBlank() && confirmPassword.isNotBlank(),
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Footer link inside card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSignInClick
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
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
    ) {
        ChildHeader(
            title = "Forgot Password",
            onBackClick = onBackClick
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Card container holding email, CTA and return link
            ShCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Enter your email address and we'll send you a password reset link.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    placeholder = "you@example.com",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done,
                    onImeAction = { focusManager.clearFocus() }
                )

                Spacer(modifier = Modifier.height(28.dp))

                ShButton(
                    text = "Send Reset Link",
                    onClick = onSendResetLinkClick,
                    enabled = email.isNotBlank(),
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back to Sign In",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onBackground,
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
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
