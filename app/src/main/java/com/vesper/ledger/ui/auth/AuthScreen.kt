package com.vesper.ledger.ui.auth

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily

enum class AuthTab {
    SIGN_IN, SIGN_UP, FORGOT_PASSWORD, PASSWORD_RESET
}

@Composable
fun VesperLogo(modifier: Modifier = Modifier) {
    val onBgColor = MaterialTheme.colorScheme.onBackground
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D0E11))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val strokeWidth = 3.5.dp.toPx()

                // Left branch of Y
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.18f, h * 0.18f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                    strokeWidth = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Right branch of Y
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.18f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                    strokeWidth = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Stem of Y
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.58f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.82f),
                    strokeWidth = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // Horizontal baseline
                drawLine(
                    color = Color.White,
                    start = androidx.compose.ui.geometry.Offset(w * 0.22f, h * 0.82f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.78f, h * 0.82f),
                    strokeWidth = strokeWidth,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )

                // White dot
                drawCircle(
                    color = Color.White,
                    radius = 3.5.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.28f)
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Vesper",
            fontFamily = PlusJakartaSansFamily,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = onBgColor,
            letterSpacing = (-0.5).sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (Boolean) -> Unit, // isNewUser
    onContinueAsGuest: () -> Unit
) {
    var activeTab by remember { mutableStateOf(AuthTab.SIGN_IN) }
    var resetEmailTarget by remember { mutableStateOf("") }

    val onBgColor = MaterialTheme.colorScheme.onBackground
    val outlineColor = MaterialTheme.colorScheme.outline

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header Bar (Contains back arrow if in Sign Up / Forgot Password)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (activeTab != AuthTab.SIGN_IN) {
                    IconButton(onClick = { activeTab = AuthTab.SIGN_IN }) {
                        Icon(Icons.Outlined.ArrowBack, null, tint = onBgColor)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    if (activeTab == AuthTab.SIGN_UP) {
                        Text(
                            text = "Vesper Ledger",
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = onBgColor
                        )
                    }
                }
            }

            // Main scrollable forms
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                when (activeTab) {
                    AuthTab.SIGN_IN -> SignInView(
                        viewModel = viewModel,
                        onSuccess = { onAuthSuccess(false) },
                        onNavigateSignUp = { activeTab = AuthTab.SIGN_UP },
                        onNavigateForgot = { activeTab = AuthTab.FORGOT_PASSWORD },
                        onContinueAsGuest = onContinueAsGuest
                    )
                    AuthTab.SIGN_UP -> SignUpView(
                        viewModel = viewModel,
                        onSuccess = { onAuthSuccess(true) },
                        onNavigateSignIn = { activeTab = AuthTab.SIGN_IN }
                    )
                    AuthTab.FORGOT_PASSWORD -> ForgotPasswordView(
                        viewModel = viewModel,
                        onVerified = { email ->
                            resetEmailTarget = email
                            activeTab = AuthTab.PASSWORD_RESET
                        }
                    )
                    AuthTab.PASSWORD_RESET -> PasswordResetView(
                        viewModel = viewModel,
                        email = resetEmailTarget,
                        onSuccess = { activeTab = AuthTab.SIGN_IN }
                    )
                }
            }
        }
    }
}

@Composable
fun SignInView(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onNavigateSignUp: () -> Unit,
    onNavigateForgot: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val outlineColor = MaterialTheme.colorScheme.outline
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VesperLogo(modifier = Modifier.padding(bottom = 12.dp))

        // Titles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome Back",
                fontFamily = SpaceGroteskFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
            Text(
                text = "Sign in to continue managing your income, expenses, and savings securely.",
                fontSize = 14.sp,
                color = secTextColor,
                lineHeight = 20.sp
            )
        }

        // Form Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Email Address
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Email Address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your email", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Mail, null, tint = secTextColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }

            // Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your password", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = secTextColor) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = secTextColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }
        }

        // Remember Me & Forgot Password Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Text("Remember me", fontSize = 13.sp, color = secTextColor)
            }
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor,
                modifier = Modifier.clickable { onNavigateForgot() }
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        // Primary Sign In Button
        Button(
            onClick = {
                val email = emailInput.trim()
                val password = passwordInput.trim()

                if (email.isEmpty() || password.isEmpty()) {
                    errorMessage = "Email and Password are required."
                    return@Button
                }

                isLoading = true
                viewModel.login(email, password) { success, msg ->
                    isLoading = false
                    if (success) {
                        onSuccess()
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Sign In", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f).height(1.dp).background(outlineColor))
            Text(
                text = "OR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = secTextColor,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Box(modifier = Modifier.weight(1f).height(1.dp).background(outlineColor))
        }

        // Guest session trigger
        Button(
            onClick = onContinueAsGuest,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = onBgColor
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.Person, null, tint = onBgColor, modifier = Modifier.size(16.dp))
                Text("Continue as Guest", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Toggle text link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Don't have an account? ", fontSize = 13.sp, color = secTextColor)
            Text(
                text = "Create Account",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor,
                modifier = Modifier.clickable { onNavigateSignUp() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device shield footer info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Shield, null, tint = secTextColor, modifier = Modifier.size(14.dp))
            Text(
                text = "Your data stays securely on your device.",
                fontSize = 11.sp,
                color = secTextColor
            )
        }
    }
}

@Composable
fun SignUpView(
    viewModel: AuthViewModel,
    onSuccess: () -> Unit,
    onNavigateSignIn: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var agreeTerms by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Titles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Create Account",
                fontFamily = SpaceGroteskFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
            Text(
                text = "Create your local account to personalize your expense tracking experience.",
                fontSize = 14.sp,
                color = secTextColor,
                lineHeight = 20.sp
            )
        }

        // Form Fields
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Full Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Full Name", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = fullName,
                    onValueChange = {
                        fullName = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your full name", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = secTextColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }

            // Email Address
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Email Address", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your email", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Mail, null, tint = secTextColor) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }

            // Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Enter your password", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = secTextColor) },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = secTextColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }

            // Confirm Password
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Confirm Password", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = onBgColor)
                OutlinedTextField(
                    value = confirmPasswordInput,
                    onValueChange = {
                        confirmPasswordInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Confirm your password", color = secTextColor.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = secTextColor) },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                                contentDescription = null,
                                tint = secTextColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = outlineColor,
                        focusedTextColor = onBgColor,
                        unfocusedTextColor = onBgColor
                    )
                )
            }
        }

        // Terms of service agreement checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = agreeTerms,
                onCheckedChange = { agreeTerms = it },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "I agree to the Privacy Policy",
                    fontSize = 12.sp,
                    color = secTextColor
                )
                Text(
                    text = "and Terms & Conditions.",
                    fontSize = 12.sp,
                    color = secTextColor
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        // Create Account Action
        Button(
            onClick = {
                val fName = fullName.trim()
                val em = emailInput.trim()
                val pwd = passwordInput.trim()
                val cpwd = confirmPasswordInput.trim()

                if (fName.length < 2) {
                    errorMessage = "Full Name must be at least 2 characters."
                    return@Button
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(em).matches()) {
                    errorMessage = "Invalid email format."
                    return@Button
                }
                if (pwd.length < 8 || !pwd.any { it.isUpperCase() } || !pwd.any { it.isLowerCase() } || !pwd.any { it.isDigit() }) {
                    errorMessage = "Password must be at least 8 characters and contain an uppercase, lowercase, and digit."
                    return@Button
                }
                if (pwd != cpwd) {
                    errorMessage = "Passwords do not match."
                    return@Button
                }
                if (!agreeTerms) {
                    errorMessage = "You must agree to Terms & Conditions."
                    return@Button
                }

                isLoading = true
                viewModel.signUp(em, fName, pwd) { success, msg ->
                    isLoading = false
                    if (success) {
                        onSuccess()
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Create Account", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        // Navigate SignIn row link
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account? ", fontSize = 13.sp, color = secTextColor)
            Text(
                text = "Sign In",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor,
                modifier = Modifier.clickable { onNavigateSignIn() }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Device shield footer info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Shield, null, tint = secTextColor, modifier = Modifier.size(14.dp))
            Text(
                text = "Your account information remains stored locally on your device.",
                fontSize = 11.sp,
                color = secTextColor
            )
        }
    }
}

@Composable
fun ForgotPasswordView(
    viewModel: AuthViewModel,
    onVerified: (String) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VesperLogo(modifier = Modifier.padding(bottom = 12.dp))

        // Titles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Recover Password",
                fontFamily = SpaceGroteskFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
            Text(
                text = "Verify your registered email Address to update your local password.",
                fontSize = 14.sp,
                color = secTextColor,
                lineHeight = 20.sp
            )
        }

        OutlinedTextField(
            value = emailInput,
            onValueChange = {
                emailInput = it
                errorMessage = ""
            },
            placeholder = { Text("Enter your email", color = secTextColor.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Outlined.Mail, null, tint = secTextColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = outlineColor,
                focusedTextColor = onBgColor,
                unfocusedTextColor = onBgColor
            )
        )

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        // Action Trigger
        Button(
            onClick = {
                val email = emailInput.trim()
                if (email.isEmpty()) {
                    errorMessage = "Email Address is required."
                    return@Button
                }

                isLoading = true
                viewModel.verifyRecovery(email) { success, msg ->
                    isLoading = false
                    if (success) {
                        onVerified(email)
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Confirm Identity", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PasswordResetView(
    viewModel: AuthViewModel,
    email: String,
    onSuccess: () -> Unit
) {
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline
    val secTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val onBgColor = MaterialTheme.colorScheme.onBackground
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VesperLogo(modifier = Modifier.padding(bottom = 12.dp))

        // Titles
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Reset Password",
                fontFamily = SpaceGroteskFamily,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = onBgColor
            )
            Text(
                text = "Set a secure new password for your account ($email).",
                fontSize = 14.sp,
                color = secTextColor,
                lineHeight = 20.sp
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = newPassword,
                onValueChange = {
                    newPassword = it
                    errorMessage = ""
                },
                placeholder = { Text("Enter new password", color = secTextColor.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = secTextColor) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null,
                            tint = secTextColor
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor,
                    focusedTextColor = onBgColor,
                    unfocusedTextColor = onBgColor
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = ""
                },
                placeholder = { Text("Confirm new password", color = secTextColor.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = secTextColor) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor,
                    focusedTextColor = onBgColor,
                    unfocusedTextColor = onBgColor
                )
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )
        }

        Button(
            onClick = {
                val pwd = newPassword.trim()
                val cpwd = confirmPassword.trim()

                if (pwd.length < 8 || !pwd.any { it.isUpperCase() } || !pwd.any { it.isLowerCase() } || !pwd.any { it.isDigit() }) {
                    errorMessage = "Password must be at least 8 characters and contain an uppercase, lowercase, and digit."
                    return@Button
                }
                if (pwd != cpwd) {
                    errorMessage = "Passwords do not match."
                    return@Button
                }

                isLoading = true
                viewModel.resetPassword(email, pwd) { success, msg ->
                    isLoading = false
                    if (success) {
                        Toast.makeText(context, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    } else {
                        errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Update Password", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}
