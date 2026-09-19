package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldPointsDark
import com.example.ui.theme.IndigoPrimaryDark
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.SkillSwapViewModel

enum class AdminTab {
    OVERVIEW,
    SKILLS_MODERATION,
    AWARD_POINTS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val allSkills by viewModel.allSkills.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()

    var activeTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    // Award bonus state
    var selectedUserId by remember { mutableStateOf(allUsers.firstOrNull()?.userId ?: "") }
    var bonusPointsInput by remember { mutableStateOf("100") }
    var bonusReason by remember { mutableStateOf("Top Community Contributor Reward") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Control Center", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == AdminTab.OVERVIEW,
                    onClick = { activeTab = AdminTab.OVERVIEW },
                    text = { Text("Overview", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == AdminTab.SKILLS_MODERATION,
                    onClick = { activeTab = AdminTab.SKILLS_MODERATION },
                    text = { Text("Skills (${allSkills.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == AdminTab.AWARD_POINTS,
                    onClick = { activeTab = AdminTab.AWARD_POINTS },
                    text = { Text("Award Points", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            when (activeTab) {
                AdminTab.OVERVIEW -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Platform Analytics",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatCard(
                                title = "Total Students",
                                value = "${allUsers.size}",
                                icon = Icons.Default.People,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                title = "Listed Skills",
                                value = "${allSkills.size}",
                                icon = Icons.Default.LibraryBooks,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdminStatCard(
                                title = "Total Sessions",
                                value = "${allSessions.size}",
                                icon = Icons.Default.CheckCircle,
                                modifier = Modifier.weight(1f)
                            )
                            val totalPoints = allUsers.sumOf { it.skillPoints }
                            AdminStatCard(
                                title = "Points in Circulation",
                                value = "$totalPoints Pts",
                                icon = Icons.Default.MonetizationOn,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Registered Peer Accounts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        allUsers.forEach { user ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = user.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        text = "${user.skillPoints} Pts",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GoldPointsDark
                                    )
                                }
                            }
                        }
                    }
                }

                AdminTab.SKILLS_MODERATION -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allSkills, key = { it.skillId }) { skill ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = skill.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text(text = "Mentor: ${skill.mentorName} • ${skill.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }

                                        Surface(
                                            color = if (skill.isActive) Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (skill.isActive) "ACTIVE" else "DISABLED",
                                                color = if (skill.isActive) Color(0xFF065F46) else Color(0xFF991B1B),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { viewModel.adminToggleSkill(skill.skillId, !skill.isActive) }
                                        ) {
                                            Text(if (skill.isActive) "Disable Skill" else "Enable Skill")
                                        }

                                        TextButton(
                                            onClick = { viewModel.adminDeleteSkill(skill.skillId) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                        ) {
                                            Text("Delete")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.AWARD_POINTS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Grant Community Bonus Points",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Admins can credit Skill Points to active students as performance rewards or contest bonuses.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Student selection
                        Text(text = "Select Student", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        allUsers.forEach { user ->
                            val isSelected = selectedUserId == user.userId
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                onClick = { selectedUserId = user.userId }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${user.fullName} (${user.email})",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    Text(
                                        text = "${user.skillPoints} Pts",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldPointsDark
                                    )
                                }
                            }
                        }

                        // Amount
                        OutlinedTextField(
                            value = bonusPointsInput,
                            onValueChange = { bonusPointsInput = it.filter { c -> c.isDigit() } },
                            label = { Text("Bonus Points Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Reason
                        OutlinedTextField(
                            value = bonusReason,
                            onValueChange = { bonusReason = it },
                            label = { Text("Reason / Award Description") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val pts = bonusPointsInput.toIntOrNull() ?: 100
                                viewModel.adminAddBonusPoints(selectedUserId, pts, bonusReason)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("award_points_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                        ) {
                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Award $bonusPointsInput Points", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
