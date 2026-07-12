package com.vesper.ledger.ui.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

enum class AuthTab {
    SIGN_IN, SIGN_UP, FORGOT_PASSWORD, PASSWORD_RESET
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: AuthViewModel,
    onAuthSuccess: (Boolean) -> Unit, // passes isNewUser to decide if Personalization is needed
    onContinueAsGuest: () -> Unit
) {
    var activeTab by remember { mutableStateOf(AuthTab.SIGN_IN) }
    var resetEmailTarget by remember { mutableStateOf("") }

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
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VESPER",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 3.sp
                )
            }

            // Main Content Area
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
                        },
                        onNavigateSignIn = { activeTab = AuthTab.SIGN_IN }
                    )
                    AuthTab.PASSWORD_RESET -> PasswordResetView(
                        viewModel = viewModel,
                        email = resetEmailTarget,
                        onSuccess = { activeTab = AuthTab.SIGN_IN }
                    )
                }
            }

            // Bottom trust label
            Text(
                text = "Vesper operates 100% offline. No credentials ever leave your device.",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )
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
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val context = LocalContext.current
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Welcome
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Welcome Back",
                fontFamily = SpaceGroteskFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter your password to unlock your offline ledger session.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Form fields
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { 
                    emailInput = it
                    errorMessage = ""
                },
                label = { Text("Email Address") },
                placeholder = { Text("email@example.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = passwordInput,
                onValueChange = { 
                    passwordInput = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Action Trigger
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
            Text("Unlock Ledger", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
        }

        // Secondary Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onNavigateForgot() }
                    .padding(vertical = 4.dp)
            )
            Text(
                text = "Create Account",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateSignUp() }
                    .padding(vertical = 4.dp)
            )
        }

        // Guest session option
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .border(1.dp, outlineColor, RoundedCornerShape(8.dp))
                .clickable { onContinueAsGuest() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Continue as Guest",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Recovery Details
    var recoveryPhrase by remember { mutableStateOf("vesper local offline security key") }
    var recoveryPin by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("What was your first pet's name?") }
    var securityAnswer by remember { mutableStateOf("") }

    var agreePrivacy by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Create Local Account",
                fontFamily = SpaceGroteskFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Credentials will be cryptographically hashed and stored on-device.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Form fields
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { 
                    fullName = it
                    errorMessage = ""
                },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = email,
                onValueChange = { 
                    email = it
                    errorMessage = ""
                },
                label = { Text("Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { 
                    password = it
                    errorMessage = ""
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    errorMessage = ""
                },
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp), color = outlineColor)

            // Offline Recovery config
            Text(
                text = "Configure Recovery Parameters (Mandatory)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            OutlinedTextField(
                value = recoveryPin,
                onValueChange = { recoveryPin = it },
                label = { Text("Recovery PIN (4 Digits)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = { recoveryPhrase = it },
                label = { Text("Recovery Phrase Word") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = securityAnswer,
                onValueChange = { securityAnswer = it },
                label = { Text("Answer: $securityQuestion") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )
        }

        // Checkboxes
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreePrivacy, onCheckedChange = { agreePrivacy = it })
                Text(
                    text = "I agree to Privacy Policy",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = agreeTerms, onCheckedChange = { agreeTerms = it })
                Text(
                    text = "I agree to Terms and Conditions",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Button
        Button(
            onClick = {
                val fName = fullName.trim()
                val em = email.trim()
                val pwd = password.trim()
                val cpwd = confirmPassword.trim()
                val phrase = recoveryPhrase.trim()
                val pin = recoveryPin.trim()
                val answer = securityAnswer.trim()

                // Validations
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
                if (pin.length != 4) {
                    errorMessage = "PIN must be exactly 4 digits."
                    return@Button
                }
                if (phrase.isEmpty() || answer.isEmpty()) {
                    errorMessage = "All recovery fields are required."
                    return@Button
                }
                if (!agreePrivacy || !agreeTerms) {
                    errorMessage = "You must agree to Privacy Policy and Terms."
                    return@Button
                }

                isLoading = true
                viewModel.signUp(em, fName, pwd, phrase, pin, securityQuestion, answer) { success, msg ->
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
            Text("Create Account", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Already have an account? Sign In",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateSignIn() }
                    .padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ForgotPasswordView(
    viewModel: AuthViewModel,
    onVerified: (String) -> Unit,
    onNavigateSignIn: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var recoveryPhrase by remember { mutableStateOf("") }
    var recoveryPin by remember { mutableStateOf("") }
    var recoveryAnswer by remember { mutableStateOf("") }
    var securityQuestion by remember { mutableStateOf("What was your first pet's name?") }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Recover Password",
                fontFamily = SpaceGroteskFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Match recovery PIN, phrase, and security answer to reset.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Account Email Address") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = recoveryPin,
                onValueChange = { recoveryPin = it },
                label = { Text("Recovery PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = recoveryPhrase,
                onValueChange = { recoveryPhrase = it },
                label = { Text("Recovery Phrase Word") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = recoveryAnswer,
                onValueChange = { recoveryAnswer = it },
                label = { Text("Answer: $securityQuestion") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = {
                val em = email.trim()
                val pin = recoveryPin.trim()
                val phrase = recoveryPhrase.trim()
                val ans = recoveryAnswer.trim()

                if (em.isEmpty() || pin.isEmpty() || phrase.isEmpty() || ans.isEmpty()) {
                    errorMessage = "All parameters are required."
                    return@Button
                }

                isLoading = true
                viewModel.verifyRecovery(em, phrase, pin, securityQuestion, ans) { success, msg ->
                    isLoading = false
                    if (success) {
                        onVerified(em)
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
            Text("Verify Recovery", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Back to Sign In",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clickable { onNavigateSignIn() }
                    .padding(vertical = 4.dp)
            )
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
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val outlineColor = MaterialTheme.colorScheme.outline
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Reset Password",
                fontFamily = SpaceGroteskFamily,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Set a secure new password for your account ($email).",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                label = { Text("New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm New Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = outlineColor
                )
            )
        }

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
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
            Text("Update Password", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
        }
    }
}
