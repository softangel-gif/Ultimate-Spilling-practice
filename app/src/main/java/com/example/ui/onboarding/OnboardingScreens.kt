package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.components.PresetAvatar

@Composable
fun OnboardingFlow(
    onCompleteOnboarding: (UserProfile) -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Sign in, 2: Profile, 3: Skills

    // User draft state
    var email by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf("Google") } // "Google", "Apple", "Email"
    var nickname by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(PresetAvatar.OWL.id) }
    var selectedSkills by remember { mutableStateOf(setOf("Writing", "Listening")) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        AnimatedContent(
            targetState = step,
            label = "OnboardingStepTransition"
        ) { currentStep ->
            when (currentStep) {
                1 -> OnboardingWelcomeScreen(
                    onGoogleSignIn = {
                        accountType = "Google"
                        email = "user.google@example.com"
                        nickname = "Alex Learner"
                        step = 2
                    },
                    onAppleSignIn = {
                        accountType = "Apple"
                        email = "user.apple@example.com"
                        nickname = "Alex Learner"
                        step = 2
                    },
                    onEmailAuthSubmit = { userEmail, isNewAccount ->
                        accountType = "Email"
                        email = userEmail
                        nickname = userEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
                        step = 2
                    }
                )

                2 -> OnboardingProfileScreen(
                    initialNickname = nickname,
                    initialAvatarId = selectedAvatar,
                    onSaveProfile = { newNickname, avatarId ->
                        nickname = newNickname
                        selectedAvatar = avatarId
                        step = 3
                    }
                )

                3 -> OnboardingSkillsScreen(
                    selectedSkills = selectedSkills,
                    onToggleSkill = { skill ->
                        selectedSkills = if (selectedSkills.contains(skill)) {
                            if (selectedSkills.size > 1) selectedSkills - skill else selectedSkills
                        } else {
                            selectedSkills + skill
                        }
                    },
                    onGetStarted = {
                        val profile = UserProfile(
                            id = email.ifBlank { "user_${System.currentTimeMillis()}" },
                            email = email,
                            nickname = nickname.ifBlank { "Learner" },
                            avatarIconName = selectedAvatar,
                            selectedSkills = selectedSkills.joinToString(","),
                            accountType = accountType,
                            isLoggedIn = true
                        )
                        onCompleteOnboarding(profile)
                    }
                )
            }
        }
    }
}

// --- SCREEN 1: Welcome / Sign In ---
@Composable
fun OnboardingWelcomeScreen(
    onGoogleSignIn: () -> Unit,
    onAppleSignIn: () -> Unit,
    onEmailAuthSubmit: (email: String, isNewAccount: Boolean) -> Unit
) {
    var showEmailModal by remember { mutableStateOf(false) }
    var isCreateAccount by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Master English Writing & Listening",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Practice spelling with speech-to-text sound checks, play mistake quizzes, write short essays, and sharpen comprehension skills.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Google Button
            Button(
                onClick = onGoogleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_continue_google"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GTranslate,
                        contentDescription = null,
                        tint = Color(0xFFEA4335)
                    )
                    Text(
                        "Continue with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Apple Button
            Button(
                onClick = onAppleSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("button_continue_apple"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = null
                    )
                    Text(
                        "Continue with Apple",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Email link
            TextButton(
                onClick = { showEmailModal = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("link_sign_in_email")
            ) {
                Text(
                    "Sign in with email",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    // Email Dialog
    if (showEmailModal) {
        AlertDialog(
            onDismissRequest = { showEmailModal = false },
            title = {
                Text(
                    text = if (isCreateAccount) "Create Account" else "Sign In with Email",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it; errorMessage = null },
                        label = { Text("Email address") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_email")
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it; errorMessage = null },
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_password")
                    )

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCreateAccount) "Already have an account? " else "Don't have an account? ",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        TextButton(
                            onClick = {
                                isCreateAccount = !isCreateAccount
                                errorMessage = null
                            }
                        ) {
                            Text(
                                if (isCreateAccount) "Log in" else "Create account",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (emailInput.isBlank() || !emailInput.contains("@")) {
                            errorMessage = "Please enter a valid email address."
                        } else if (passwordInput.length < 4) {
                            errorMessage = "Password must be at least 4 characters."
                        } else {
                            showEmailModal = false
                            onEmailAuthSubmit(emailInput.trim(), isCreateAccount)
                        }
                    },
                    modifier = Modifier.testTag("button_submit_email_auth")
                ) {
                    Text(if (isCreateAccount) "Create Account" else "Log In")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- SCREEN 2: Profile Setup ---
@Composable
fun OnboardingProfileScreen(
    initialNickname: String,
    initialAvatarId: String,
    onSaveProfile: (nickname: String, avatarId: String) -> Unit
) {
    var nicknameInput by remember { mutableStateOf(initialNickname) }
    var selectedAvatarId by remember { mutableStateOf(initialAvatarId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Profile Setup",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Choose your nickname and avatar icon.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Nickname field
            OutlinedTextField(
                value = nicknameInput,
                onValueChange = { nicknameInput = it },
                label = { Text("Nickname") },
                placeholder = { Text("e.g. Alex") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_nickname")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Select Avatar Icon",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            // Avatar Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(PresetAvatar.entries) { avatar ->
                    val isSelected = (avatar.id == selectedAvatarId)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) avatar.color.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) avatar.color else Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { selectedAvatarId = avatar.id }
                            .testTag("avatar_${avatar.id}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = avatar.icon,
                                contentDescription = avatar.displayName,
                                tint = avatar.color,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // Continue Button
        Button(
            onClick = {
                val finalNickname = if (nicknameInput.isBlank()) "Learner" else nicknameInput.trim()
                onSaveProfile(finalNickname, selectedAvatarId)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("button_profile_continue"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Continue",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// --- SCREEN 3: Choose Your Skills ---
@Composable
fun OnboardingSkillsScreen(
    selectedSkills: Set<String>,
    onToggleSkill: (String) -> Unit,
    onGetStarted: () -> Unit
) {
    val skillsList = listOf(
        SkillCardInfo("Writing", "Practice spelling, sound-to-text dictation, Q&A, and small essay composition.", Icons.Default.Edit, MaterialTheme.colorScheme.primary),
        SkillCardInfo("Listening", "Listen to conversations and recordings with British Council style comprehension tests.", Icons.Default.Headphones, MaterialTheme.colorScheme.secondary)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Choose Your Skills",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 16.dp)
            )

            Text(
                text = "Select what skills you want to focus on (multi-select).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                skillsList.forEach { skill ->
                    val isSelected = selectedSkills.contains(skill.title)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToggleSkill(skill.title) }
                            .testTag("skill_card_${skill.title.lowercase()}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) skill.themeColor.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(skill.themeColor))
                        else null,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(skill.themeColor)
                            ) {
                                Icon(
                                    imageVector = skill.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = skill.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = skill.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { onToggleSkill(skill.title) }
                            )
                        }
                    }
                }
            }
        }

        // Get Started Button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("button_get_started"),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "Get Started",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class SkillCardInfo(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val themeColor: Color
)
