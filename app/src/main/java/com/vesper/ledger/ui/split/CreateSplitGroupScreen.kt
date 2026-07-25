package com.vesper.ledger.ui.split

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vesper.ledger.ui.theme.PlusJakartaSansFamily
import com.vesper.ledger.ui.theme.SpaceGroteskFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateSplitGroupScreen(
    onBackClick: () -> Unit = {},
    onGroupCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    var groupName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Trip") }
    var newMemberName by remember { mutableStateOf("") }
    val membersList = remember { mutableStateListOf("You", "Rahul", "Ananya") }

    val groupCategories = listOf(
        "Trip" to Icons.Outlined.FlightTakeoff,
        "Home" to Icons.Outlined.Home,
        "Dining" to Icons.Outlined.Restaurant,
        "Event" to Icons.Outlined.Celebration,
        "Other" to Icons.Outlined.Group
    )

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
            // 1. GROUP NAME INPUT
            Text(
                text = "GROUP NAME",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("e.g. Goa Trip 2026, Apartment Expenses", fontFamily = PlusJakartaSansFamily) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // 2. CATEGORY SELECTOR
            Text(
                text = "GROUP CATEGORY",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupCategories.forEach { (catName, catIcon) ->
                    val selected = selectedCategory == catName
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = catName },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = catIcon,
                                contentDescription = catName,
                                tint = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = catName,
                                fontSize = 11.sp,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // 3. MEMBERS SECTION
            Text(
                text = "GROUP MEMBERS (${membersList.size})",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 1.2.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // Add member row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newMemberName,
                    onValueChange = { newMemberName = it },
                    placeholder = { Text("Enter member name", fontFamily = PlusJakartaSansFamily) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Button(
                    onClick = {
                        if (newMemberName.isNotBlank()) {
                            membersList.add(newMemberName.trim())
                            newMemberName = ""
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(54.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Member")
                }
            }

            // Member Chips
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                membersList.forEachIndexed { index, member ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.take(1).uppercase(),
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = member,
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (member != "You") {
                            IconButton(
                                onClick = { membersList.removeAt(index) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // CREATE BUTTON
            Button(
                onClick = {
                    if (groupName.isBlank()) {
                        Toast.makeText(context, "Please enter a group name", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Group '$groupName' created!", Toast.LENGTH_SHORT).show()
                        onGroupCreated()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground,
                    contentColor = MaterialTheme.colorScheme.background
                )
            ) {
                Text(
                    text = "Create Split Group",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}
