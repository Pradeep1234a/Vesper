package com.vesper.ledger.ui.profile

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.components.ShCard
import com.vesper.ledger.ui.settings.SettingsViewModel
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileManagementScreen(
    settingsViewModel: SettingsViewModel,
    onBackClick: () -> Unit = {},
    onSignOutClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val userName by settingsViewModel.userName.collectAsState()
    val userEmail by settingsViewModel.userEmail.collectAsState()
    val currencySymbol by settingsViewModel.currencySymbol.collectAsState()
    val selectedAccent by settingsViewModel.accentColor.collectAsState()

    var isEditProfileDialogOpen by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail) }

    // Edit Profile Modal
    if (isEditProfileDialogOpen) {
        AlertDialog(
            onDismissRequest = { isEditProfileDialogOpen = false },
            title = {
                Text(
                    text = "Edit Profile",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.saveUserName(editName)
                        settingsViewModel.saveUserEmail(editEmail)
                        isEditProfileDialogOpen = false
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Save", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditProfileDialogOpen = false }) {
                    Text("Cancel", fontFamily = SpaceGroteskFamily)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. PROFILE HEADER CARD
            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userName.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.background
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = userEmail.ifBlank { "Personal Space" },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = PlusJakartaSansFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    IconButton(onClick = {
                        editName = userName
                        editEmail = userEmail
                        isEditProfileDialogOpen = true
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit Profile",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // 2. ACCOUNT & PREFERENCE SETTINGS
            Text(
                text = "ACCOUNT PREFERENCES",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column {
                    // Currency Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCurrencyClick() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Outlined.AttachMoney, contentDescription = "Currency", tint = MaterialTheme.colorScheme.onSurface)
                            Text("Primary Currency", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text(currencySymbol, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // 3. SECURITY & DATA
            Text(
                text = "SECURITY & BACKUP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            ShCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { Toast.makeText(context, "Data Backup Initiated", Toast.LENGTH_SHORT).show() }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(imageVector = Icons.Outlined.CloudDownload, contentDescription = "Backup Data", tint = MaterialTheme.colorScheme.onSurface)
                            Text("Export Ledger Backup", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = "Go", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SIGN OUT BUTTON
            Button(
                onClick = onSignOutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Icon(imageVector = Icons.Outlined.Logout, contentDescription = "Log Out")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
