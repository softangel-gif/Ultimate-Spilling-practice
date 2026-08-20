package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile

enum class PresetAvatar(val id: String, val displayName: String, val icon: ImageVector, val color: Color) {
    OWL("avatar_owl", "Smart Owl", Icons.Default.MenuBook, Color(0xFF6366F1)),
    FOX("avatar_fox", "Clever Fox", Icons.Default.Psychology, Color(0xFFF97316)),
    BEAR("avatar_bear", "Steady Bear", Icons.Default.School, Color(0xFF10B981)),
    ROBOT("avatar_robot", "AI Bot", Icons.Default.AutoAwesome, Color(0xFF06B6D4)),
    STAR("avatar_star", "Gold Star", Icons.Default.Star, Color(0xFFEAB308)),
    ROCKET("avatar_rocket", "Rocket Learner", Icons.Default.RocketLaunch, Color(0xFFEC4899)),
    GLOBE("avatar_globe", "Global Speaker", Icons.Default.Language, Color(0xFF3B82F6)),
    HEADPHONES("avatar_listener", "Audio Specialist", Icons.Default.Headphones, Color(0xFF8B5CF6));

    companion object {
        fun fromId(id: String): PresetAvatar {
            return entries.find { it.id == id } ?: OWL
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String,
    currentUser: UserProfile?,
    onNavigateToProfile: () -> Unit,
    onNavigateToWritingSoundToWrite: () -> Unit,
    onNavigateToWritingQuiz: () -> Unit,
    onNavigateToWritingQA: () -> Unit,
    onNavigateToWritingEssays: () -> Unit,
    onNavigateToListening: () -> Unit,
    onNavigateToMistakes: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogout: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var writingSubmenuExpanded by remember { mutableStateOf(false) }

    val avatar = remember(currentUser?.avatarIconName) {
        PresetAvatar.fromId(currentUser?.avatarIconName ?: "avatar_owl")
    }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        actions = {
            // Profile badge (Avatar + Nickname)
            if (currentUser != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
                        .clickable { onNavigateToProfile() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("profile_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(avatar.color)
                    ) {
                        Icon(
                            imageVector = avatar.icon,
                            contentDescription = avatar.displayName,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = currentUser.nickname.ifBlank { "Learner" },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            // Hamburger menu button
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier.testTag("hamburger_menu")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Main Navigation Menu"
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                        writingSubmenuExpanded = false
                    }
                ) {
                    // Writing Option (Expandable)
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Writing", fontWeight = FontWeight.SemiBold)
                                }
                                Icon(
                                    imageVector = if (writingSubmenuExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        onClick = { writingSubmenuExpanded = !writingSubmenuExpanded },
                        modifier = Modifier.testTag("menu_writing")
                    )

                    // Submenu items under Writing
                    if (writingSubmenuExpanded) {
                        DropdownMenuItem(
                            text = { Text("• Spelling (Sound-to-Write)", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                menuExpanded = false
                                writingSubmenuExpanded = false
                                onNavigateToWritingSoundToWrite()
                            },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .testTag("menu_sound_to_write")
                        )
                        DropdownMenuItem(
                            text = { Text("• Spelling Quiz (Game)", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                menuExpanded = false
                                writingSubmenuExpanded = false
                                onNavigateToWritingQuiz()
                            },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .testTag("menu_spelling_quiz")
                        )
                        DropdownMenuItem(
                            text = { Text("• Question & Answer", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                menuExpanded = false
                                writingSubmenuExpanded = false
                                onNavigateToWritingQA()
                            },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .testTag("menu_qa")
                        )
                        DropdownMenuItem(
                            text = { Text("• Small Essay Writing", style = MaterialTheme.typography.bodyMedium) },
                            onClick = {
                                menuExpanded = false
                                writingSubmenuExpanded = false
                                onNavigateToWritingEssays()
                            },
                            modifier = Modifier
                                .padding(start = 16.dp)
                                .testTag("menu_small_essays")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Listening
                    DropdownMenuItem(
                        text = { Text("Listening Practice", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToListening()
                        },
                        modifier = Modifier.testTag("menu_listening")
                    )

                    // My Mistakes
                    DropdownMenuItem(
                        text = { Text("My Mistakes Log", fontWeight = FontWeight.Medium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToMistakes()
                        },
                        modifier = Modifier.testTag("menu_mistakes")
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Settings
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onNavigateToSettings()
                        },
                        modifier = Modifier.testTag("menu_settings")
                    )

                    // Log Out
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Log Out",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onLogout()
                        },
                        modifier = Modifier.testTag("menu_logout")
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}
