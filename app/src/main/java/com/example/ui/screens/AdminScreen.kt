package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillSwapViewModel
import java.text.SimpleDateFormat
import java.util.*

enum class AdminTab(val title: String) {
    USERS("Users (Firestore)"),
    OVERVIEW("Metrics"),
    SKILLS_MODERATION("Skills"),
    AWARD_POINTS("Award Points")
}

enum class UserStatusFilter {
    ALL,
    ACTIVE_ONLY,
    INACTIVE_ONLY,
    ADMINS_ONLY
}

enum class UserSortOption(val displayName: String) {
    NAME_ASC("Name (A-Z)"),
    POINTS_DESC("Points (Highest)"),
    RATING_DESC("Rating (Highest)"),
    RECENT("Newest First")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AdminScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allSkills by viewModel.allSkills.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()
    val allTransactions by viewModel.allTransactions.collectAsState()
    val isFetchingFirestoreUsers by viewModel.isFetchingFirestoreUsers.collectAsState()
    val firestoreUsersStatus by viewModel.firestoreUsersStatus.collectAsState()

    // Authorization Guard: Only authorized admins can view or access this dashboard
    if (currentUser?.isAdmin != true) {
        UnauthorizedAccessView(
            userEmail = currentUser?.email ?: "Guest",
            onNavigateBack = onNavigateBack
        )
        return
    }

    var activeTab by remember { mutableStateOf(AdminTab.USERS) }

    // User Management states
    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(UserStatusFilter.ALL) }
    var sortOption by remember { mutableStateOf(UserSortOption.NAME_ASC) }
    var selectedUserForDetails by remember { mutableStateOf<User?>(null) }
    var userToToggleStatus by remember { mutableStateOf<User?>(null) }

    // Award bonus states
    var selectedUserId by remember { mutableStateOf(allUsers.firstOrNull()?.userId ?: "") }
    var bonusPointsInput by remember { mutableStateOf("100") }
    var bonusReason by remember { mutableStateOf("Top Community Contributor Reward") }

    // Filter and sort users
    val filteredUsers = remember(allUsers, searchQuery, statusFilter, sortOption) {
        var list = allUsers.filter { user ->
            val matchesQuery = searchQuery.isBlank() ||
                user.fullName.contains(searchQuery, ignoreCase = true) ||
                user.email.contains(searchQuery, ignoreCase = true) ||
                user.location.contains(searchQuery, ignoreCase = true) ||
                user.userId.contains(searchQuery, ignoreCase = true) ||
                user.skillsTeaching.any { it.contains(searchQuery, ignoreCase = true) }

            val matchesStatus = when (statusFilter) {
                UserStatusFilter.ALL -> true
                UserStatusFilter.ACTIVE_ONLY -> user.isActive
                UserStatusFilter.INACTIVE_ONLY -> !user.isActive
                UserStatusFilter.ADMINS_ONLY -> user.isAdmin
            }

            matchesQuery && matchesStatus
        }

        when (sortOption) {
            UserSortOption.NAME_ASC -> list.sortedBy { it.fullName.lowercase() }
            UserSortOption.POINTS_DESC -> list.sortedByDescending { it.skillPoints }
            UserSortOption.RATING_DESC -> list.sortedByDescending { it.averageRating }
            UserSortOption.RECENT -> list.sortedByDescending { it.createdAt }
        }
    }

    val activeCount = allUsers.count { it.isActive }
    val inactiveCount = allUsers.count { !it.isActive }
    val adminCount = allUsers.count { it.isAdmin }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Admin Control Center", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            text = "Superuser: ${currentUser?.fullName ?: "Administrator"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "AUTHORIZED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
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
            ScrollableTabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                edgePadding = 12.dp
            ) {
                AdminTab.values().forEach { tab ->
                    Tab(
                        selected = activeTab == tab,
                        onClick = { activeTab = tab },
                        text = {
                            val badge = when (tab) {
                                AdminTab.USERS -> " (${allUsers.size})"
                                AdminTab.SKILLS_MODERATION -> " (${allSkills.size})"
                                else -> ""
                            }
                            Text(
                                text = "${tab.title}$badge",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            when (activeTab) {
                AdminTab.USERS -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Firestore Sync Header Card
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(SuccessGreen)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Cloud Firestore Sync Engine",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Surface(
                                            color = SuccessGreen.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "FIRESTORE READY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = firestoreUsersStatus ?: "Syncing user profiles with Firestore",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Button(
                                        onClick = { viewModel.fetchUsersFromFirestore() },
                                        enabled = !isFetchingFirestoreUsers,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                            .testTag("fetch_firestore_users_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        if (isFetchingFirestoreUsers) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Fetching from Firestore...", fontSize = 12.sp)
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudDownload,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Fetch All User Profiles from Firestore", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // Metrics Summary Chips
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AdminStatChip(
                                    label = "Total Users",
                                    value = "${allUsers.size}",
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatChip(
                                    label = "Active",
                                    value = "$activeCount",
                                    color = SuccessGreen,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatChip(
                                    label = "Suspended",
                                    value = "$inactiveCount",
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                AdminStatChip(
                                    label = "Admins",
                                    value = "$adminCount",
                                    color = GoldPointsDark,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Search and Filter Bar
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_user_search"),
                                    placeholder = { Text("Search by name, email, skill, or ID...") },
                                    leadingIcon = {
                                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { searchQuery = "" }) {
                                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Status Filter Chips
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = statusFilter == UserStatusFilter.ALL,
                                        onClick = { statusFilter = UserStatusFilter.ALL },
                                        label = { Text("All (${allUsers.size})", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    FilterChip(
                                        selected = statusFilter == UserStatusFilter.ACTIVE_ONLY,
                                        onClick = { statusFilter = UserStatusFilter.ACTIVE_ONLY },
                                        label = { Text("Active ($activeCount)", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    FilterChip(
                                        selected = statusFilter == UserStatusFilter.INACTIVE_ONLY,
                                        onClick = { statusFilter = UserStatusFilter.INACTIVE_ONLY },
                                        label = { Text("Suspended ($inactiveCount)", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    FilterChip(
                                        selected = statusFilter == UserStatusFilter.ADMINS_ONLY,
                                        onClick = { statusFilter = UserStatusFilter.ADMINS_ONLY },
                                        label = { Text("Admins ($adminCount)", fontSize = 11.sp) },
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                }
                            }
                        }

                        // Results Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Showing ${filteredUsers.size} User Accounts",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                TextButton(
                                    onClick = {
                                        sortOption = when (sortOption) {
                                            UserSortOption.NAME_ASC -> UserSortOption.POINTS_DESC
                                            UserSortOption.POINTS_DESC -> UserSortOption.RATING_DESC
                                            UserSortOption.RATING_DESC -> UserSortOption.RECENT
                                            UserSortOption.RECENT -> UserSortOption.NAME_ASC
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sort,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(sortOption.displayName, fontSize = 11.sp)
                                }
                            }
                        }

                        if (filteredUsers.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.PersonOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            text = "No user profiles found",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Try adjusting your search query or status filter.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            items(filteredUsers, key = { it.userId }) { user ->
                                AdminUserCard(
                                    user = user,
                                    isCurrentAdmin = user.userId == currentUser?.userId,
                                    onViewDetails = { selectedUserForDetails = user },
                                    onToggleActive = { userToToggleStatus = user }
                                )
                            }
                        }
                    }
                }

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
                            val completedSessions = allSessions.count { it.status.name == "COMPLETED" }
                            AdminStatCard(
                                title = "Completed Sessions",
                                value = "$completedSessions",
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
                            text = "Quick Account Actions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Manage User Profiles & Status",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "View full profiles, fetch Firestore records, and toggle active/suspended states.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { activeTab = AdminTab.USERS },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.ManageAccounts, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Open User Management Tab")
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
                                            Text(
                                                text = "Mentor: ${skill.mentorName} • ${skill.category}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Surface(
                                            color = if (skill.isActive) Color(0xFF1B4332) else Color(0xFF601410),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (skill.isActive) "ACTIVE" else "DISABLED",
                                                color = if (skill.isActive) SuccessGreen else Color(0xFFF2B8B5),
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

    // User Details Dialog
    selectedUserForDetails?.let { user ->
        UserDetailsDialog(
            user = user,
            isCurrentAdmin = user.userId == currentUser?.userId,
            onDismiss = { selectedUserForDetails = null },
            onToggleStatus = {
                viewModel.toggleUserActiveStatus(user.userId, !user.isActive)
                selectedUserForDetails = user.copy(isActive = !user.isActive)
            }
        )
    }

    // Toggle Status Confirmation Dialog
    userToToggleStatus?.let { user ->
        val willBeActive = !user.isActive
        AlertDialog(
            onDismissRequest = { userToToggleStatus = null },
            icon = {
                Icon(
                    imageVector = if (willBeActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (willBeActive) SuccessGreen else MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(if (willBeActive) "Reactivate Account?" else "Suspend Account?")
            },
            text = {
                Text(
                    if (willBeActive) {
                        "Are you sure you want to restore active status for ${user.fullName}? They will regain full access to listing skills and booking learning sessions."
                    } else {
                        "Are you sure you want to suspend ${user.fullName} (${user.email})? Suspended accounts cannot book sessions or offer mentoring skills."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.toggleUserActiveStatus(user.userId, willBeActive)
                        userToToggleStatus = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (willBeActive) SuccessGreen else MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(if (willBeActive) "Confirm Reactivate" else "Confirm Suspend")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToToggleStatus = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminUserCard(
    user: User,
    isCurrentAdmin: Boolean,
    onViewDetails: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() }
            .testTag("admin_user_card_${user.userId}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (user.isActive) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)
            }
        ),
        border = if (!user.isActive) {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
        } else null
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // User Avatar
                if (user.profileImage.isNotBlank()) {
                    AsyncImage(
                        model = user.profileImage,
                        contentDescription = "Avatar for ${user.fullName}",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.fullName.take(1).uppercase(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Name & Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.isAdmin) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = GoldPointsDark.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "ADMIN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldPointsDark,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = user.email,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${user.skillPoints} Pts",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPointsDark
                        )
                        Text(
                            text = " • ★ ${String.format(Locale.US, "%.1f", user.averageRating)}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = " • ${user.completedSessions} taught",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Active Switch Control
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = if (user.isActive) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (user.isActive) "ACTIVE" else "SUSPENDED",
                            color = if (user.isActive) SuccessGreen else MaterialTheme.colorScheme.error,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Switch(
                        checked = user.isActive,
                        onCheckedChange = { onToggleActive() },
                        enabled = !isCurrentAdmin,
                        modifier = Modifier.testTag("toggle_active_${user.userId}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isCurrentAdmin) {
                    Text(
                        text = "Your Admin Account (Protected)",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "ID: ${user.userId.take(8)}...",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onViewDetails,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text("View Full Profile", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(14.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UserDetailsDialog(
    user: User,
    isCurrentAdmin: Boolean,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(user.createdAt) { dateFormat.format(Date(user.createdAt)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Close")
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("User Profile Details", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Surface(
                    color = if (user.isActive) SuccessGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (user.isActive) "ACTIVE" else "SUSPENDED",
                        color = if (user.isActive) SuccessGreen else MaterialTheme.colorScheme.error,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Avatar & Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user.profileImage.isNotBlank()) {
                        AsyncImage(
                            model = user.profileImage,
                            contentDescription = user.fullName,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(text = user.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = if (user.isAdmin) "System Administrator" else "Peer Student Member",
                            fontSize = 12.sp,
                            color = if (user.isAdmin) GoldPointsDark else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(text = "Member since $formattedDate", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Divider()

                // Account Status Toggle Action Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Account Access Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (user.isActive) {
                                    "User can book sessions and publish skills."
                                } else {
                                    "Account is suspended from platform actions."
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onToggleStatus,
                            enabled = !isCurrentAdmin,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.isActive) MaterialTheme.colorScheme.error else SuccessGreen
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (user.isActive) "Suspend" else "Activate",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatBox(label = "Points", value = "${user.skillPoints} Pts", modifier = Modifier.weight(1f))
                    ProfileStatBox(label = "Rating", value = "★ ${String.format(Locale.US, "%.1f", user.averageRating)}", modifier = Modifier.weight(1f))
                    ProfileStatBox(label = "Taught", value = "${user.completedSessions}", modifier = Modifier.weight(1f))
                }

                // Contact & Location
                DetailInfoItem(label = "Email Address", value = user.email, icon = Icons.Default.Email)
                if (user.phone.isNotBlank()) {
                    DetailInfoItem(label = "Phone", value = user.phone, icon = Icons.Default.Phone)
                }
                DetailInfoItem(label = "Campus Location", value = user.location, icon = Icons.Default.LocationOn)
                DetailInfoItem(label = "Firestore User ID", value = user.userId, icon = Icons.Default.Fingerprint)

                // Bio
                if (user.bio.isNotBlank()) {
                    Column {
                        Text(text = "About / Bio", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = user.bio,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Skills Teaching
                if (user.skillsTeaching.isNotEmpty()) {
                    Column {
                        Text(text = "Skills Teaching (${user.skillsTeaching.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            user.skillsTeaching.forEach { skill ->
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Skills Learning
                if (user.skillsLearning.isNotEmpty()) {
                    Column {
                        Text(text = "Skills Learning (${user.skillsLearning.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            user.skillsLearning.forEach { skill ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = skill,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun ProfileStatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun DetailInfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AdminStatChip(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = color)
            Text(text = label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

@Composable
private fun UnauthorizedAccessView(
    userEmail: String,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Access Restricted", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.error)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Text(
                        text = "Administrator Privileges Required",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Account: $userEmail\n\nYou do not have administrative permissions to view or manage user accounts, toggle account statuses, or access the Firestore synchronization engine.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Return to Profile", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
