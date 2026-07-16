package com.vesper.ledger.ui.auth

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.R
import com.vesper.ledger.ui.theme.InterFamily
import com.vesper.ledger.ui.theme.PlayfairDisplayFamily
import kotlinx.coroutines.delay

// ─── CUSTOM COMPONENTS ────────────────────────────────────────────────────────

@Composable
fun PremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onBackground
    val placeholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val containerBg = MaterialTheme.colorScheme.surfaceVariant

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = TextStyle(
            fontFamily = InterFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            color = textColor
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        cursorBrush = SolidColor(textColor),
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(containerBg, RoundedCornerShape(20.dp))
            .border(1.dp, outlineColor, RoundedCornerShape(20.dp)),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = InterFamily,
                            fontSize = 15.sp,
                            color = placeholderColor
                        )
                    }
                    innerTextField()
                }

                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(12.dp))
                    trailingIcon()
                }
            }
        }
    )
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    outlined: Boolean = false
) {
    val bgColor = if (outlined) Color.Transparent else MaterialTheme.colorScheme.primary
    val contentColor = if (outlined) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onPrimary
    val borderStroke = if (outlined) {
        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
    } else Modifier

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(
                color = if (enabled) bgColor else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .then(borderStroke)
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = InterFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = if (enabled) contentColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PremiumCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val outlineColor = MaterialTheme.colorScheme.outline
    val activeColor = MaterialTheme.colorScheme.onBackground
    val innerBg = if (checked) activeColor else Color.Transparent

    Box(
        modifier = modifier
            .size(22.dp)
            .background(innerBg, RoundedCornerShape(6.dp))
            .border(1.2.dp, if (checked) activeColor else outlineColor, RoundedCornerShape(6.dp))
            .clip(RoundedCornerShape(6.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.background,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// ─── 1. WELCOME SCREEN ────────────────────────────────────────────────────────

@Composable
fun WelcomeScreen(
    onCreateAccountClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Identity Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 48.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "Vesper Leaves Logo",
                colorFilter = ColorFilter.tint(onBg),
                modifier = Modifier.size(108.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Vesper Ledger",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Private finance, without the noise.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec
            )
        }

        // Features list
        Column(
            modifier = Modifier
                .padding(vertical = 48.dp)
                .fillMaxWidth(0.85f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            val features = listOf(
                "Track expenses." to R.drawable.ic_launcher_background,
                "Build savings." to R.drawable.ic_launcher_background,
                "Understand your money." to R.drawable.ic_launcher_background
            )
            
            features.forEach { (text, _) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(onBg, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = text,
                        fontFamily = InterFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = onBg
                    )
                }
            }
        }

        // Action CTAs & Footer
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PremiumButton(
                text = "Create Account",
                onClick = onCreateAccountClick
            )
            Spacer(modifier = Modifier.height(16.dp))
            PremiumButton(
                text = "Sign In",
                onClick = onSignInClick,
                outlined = true
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "By continuing you agree to the\nTerms of Service and Privacy Policy.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = sec.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// ─── 2. SIGN IN SCREEN ────────────────────────────────────────────────────────

@Composable
fun SignInScreen(
    onBack: () -> Unit,
    onSignInSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onCreateAccountClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = onBg
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title cluster
            Text(
                text = "Sign In",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Welcome back.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec
            )
            
            Spacer(modifier = Modifier.height(28.dp))

            // Input Form Group (connected cluster)
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Enter your password",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = onBg,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Forgot Password Link
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.End)
            ) {
                Text(
                    text = "Forgot Password?",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = sec,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onForgotPasswordClick)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary CTA
            PremiumButton(
                text = "Sign In",
                enabled = email.isNotEmpty() && password.isNotEmpty(),
                onClick = onSignInSuccess
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account? ",
                    fontFamily = InterFamily,
                    fontSize = 14.sp,
                    color = sec
                )
                Text(
                    text = "Create Account",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = onBg,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onCreateAccountClick)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── 3. SIGN UP SCREEN ────────────────────────────────────────────────────────

@Composable
fun SignUpScreen(
    onBack: () -> Unit,
    onSignUpSuccess: () -> Unit,
    onSignInClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var agreedTerms by remember { mutableStateOf(false) }
    var agreedPrivacy by remember { mutableStateOf(false) }

    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = onBg
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Create Account",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start your financial journey.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Input Form Group
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "Enter your full name",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                )

                PremiumTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Enter your email",
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                PremiumTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Create a password",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = onBg,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                PremiumTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm your password",
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = onBg,
                            modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Checkbox Group
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumCheckbox(checked = agreedTerms, onCheckedChange = { agreedTerms = it })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "I agree to the Terms of Service",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = sec
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PremiumCheckbox(checked = agreedPrivacy, onCheckedChange = { agreedPrivacy = it })
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "I agree to the Privacy Policy",
                        fontFamily = InterFamily,
                        fontSize = 13.sp,
                        color = sec
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA
            val isEnabled = name.isNotEmpty() && email.isNotEmpty() && 
                    password.isNotEmpty() && confirmPassword.isNotEmpty() && 
                    password == confirmPassword && agreedTerms && agreedPrivacy

            PremiumButton(
                text = "Create Account",
                enabled = isEnabled,
                onClick = onSignUpSuccess
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    fontFamily = InterFamily,
                    fontSize = 14.sp,
                    color = sec
                )
                Text(
                    text = "Sign In",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = onBg,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onSignInClick)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── 4. FORGOT PASSWORD SCREEN ────────────────────────────────────────────────

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    onContinue: (String) -> Unit,
    onSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    
    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = onBg
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Forgot Password",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter your email address and we'll send a verification code.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Input
            PremiumTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Enter your email",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // CTA
            PremiumButton(
                text = "Continue",
                enabled = email.isNotEmpty(),
                onClick = { onContinue(email) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Remember your password? ",
                    fontFamily = InterFamily,
                    fontSize = 14.sp,
                    color = sec
                )
                Text(
                    text = "Sign In",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = onBg,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable(onClick = onSignInClick)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── 5. OTP VERIFICATION SCREEN ──────────────────────────────────────────────

@Composable
fun OtpVerificationScreen(
    email: String,
    onBack: () -> Unit,
    onVerifySuccess: () -> Unit
) {
    var otpDigits = remember { mutableStateListOf("", "", "", "", "", "") }
    var timerSeconds by remember { mutableStateOf(45) }
    
    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant
    val outlineColor = MaterialTheme.colorScheme.outline

    // Countdown Timer
    LaunchedEffect(key1 = timerSeconds) {
        if (timerSeconds > 0) {
            delay(1000)
            timerSeconds -= 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Back
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = onBg
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Verify Email",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Enter the 6-digit code sent to\n$email",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec,
                lineHeight = 20.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // OTP Input row (monochrome square cells)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 0..5) {
                    val isSelected = false
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(horizontal = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) onBg else outlineColor,
                                RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                // Simple mock digits insertion for testing
                                val valToSet = if (otpDigits[i].isEmpty()) "•" else ""
                                otpDigits[i] = valToSet
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = otpDigits[i],
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = onBg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Timer & Resend Link
            if (timerSeconds > 0) {
                Text(
                    text = "Resend Code (00:${String.format("%02d", timerSeconds)})",
                    fontFamily = InterFamily,
                    fontSize = 14.sp,
                    color = sec
                )
            } else {
                Text(
                    text = "Resend Code",
                    fontFamily = InterFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = onBg,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier.clickable {
                        timerSeconds = 45
                    }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Verify CTA
            val isEnabled = otpDigits.all { it.isNotEmpty() }
            PremiumButton(
                text = "Verify",
                enabled = isEnabled,
                onClick = onVerifySuccess
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ─── 6. RESET PASSWORD SCREEN ────────────────────────────────────────────────

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val onBg = MaterialTheme.colorScheme.onBackground
    val sec = MaterialTheme.colorScheme.onSurfaceVariant
    val focusManager = LocalFocusManager.current

    // Calculated password strength
    val strengthLevel = when {
        newPassword.isEmpty() -> 0
        newPassword.length < 6 -> 1
        newPassword.any { it.isDigit() } && newPassword.any { it.isUpperCase() } -> 3
        else -> 2
    }
    val strengthText = when (strengthLevel) {
        0 -> ""
        1 -> "Weak"
        2 -> "Medium"
        else -> "Strong"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        // Back
        Box(
            modifier = Modifier
                .padding(top = 16.dp)
                .size(48.dp)
                .clickable { onBack() },
            contentAlignment = Alignment.CenterStart
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = onBg
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Title
            Text(
                text = "Create New Password",
                fontFamily = PlayfairDisplayFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = onBg
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your new password must be different from previous passwords.",
                fontFamily = InterFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = sec,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Input Form Group
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PremiumTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    placeholder = "Enter new password",
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = onBg,
                            modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                        )
                    }
                )

                // Strength Indicator Bar
                if (newPassword.isNotEmpty()) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (step in 1..3) {
                                val isActive = step <= strengthLevel
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(4.dp)
                                        .background(
                                            color = if (isActive) {
                                                when (strengthLevel) {
                                                    1 -> MaterialTheme.colorScheme.error
                                                    2 -> Color(0xFFEAB308) // Yellow
                                                    else -> Color(0xFF22C55E) // Green
                                                }
                                            } else MaterialTheme.colorScheme.outline,
                                            shape = RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Password strength: $strengthText",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = when (strengthLevel) {
                                1 -> MaterialTheme.colorScheme.error
                                2 -> Color(0xFFEAB308)
                                else -> Color(0xFF22C55E)
                            }
                        )
                    }
                }

                PremiumTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    placeholder = "Confirm new password",
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    trailingIcon = {
                        Text(
                            text = if (confirmPasswordVisible) "Hide" else "Show",
                            fontFamily = InterFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = onBg,
                            modifier = Modifier.clickable { confirmPasswordVisible = !confirmPasswordVisible }
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // CTA
            val isEnabled = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && 
                    newPassword == confirmPassword && strengthLevel >= 2

            PremiumButton(
                text = "Update Password",
                enabled = isEnabled,
                onClick = onResetSuccess
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
