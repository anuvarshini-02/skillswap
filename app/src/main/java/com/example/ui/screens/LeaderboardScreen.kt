package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.User
import com.example.ui.theme.*
import com.example.ui.viewmodel.LeaderboardTab
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit
) {
    val allUsers by viewModel.allUsers.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeTab by viewModel.leaderboardTab.collectAsState()

    val sortedUsers = remember(allUsers, activeTab) {
        when (activeTab) {
            LeaderboardTab.POINTS_EARNED -> allUsers.sortedByDescending { it.skillPoints }
            LeaderboardTab.SESSIONS_TAUGHT -> allUsers.sortedByDescending { it.completedSessions }
            LeaderboardTab.RATING -> allUsers.sortedWith(compareByDescending<User> { it.averageRating }.thenByDescending { it.totalReviews })
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Peer Leaderboard", fontWeight = FontWeight.Bold) },
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
            // Tab row
            TabRow(
                selectedTabIndex = activeTab.ordinal,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeTab == LeaderboardTab.POINTS_EARNED,
                    onClick = { viewModel.setLeaderboardTab(LeaderboardTab.POINTS_EARNED) },
                    text = { Text("Points Earned", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == LeaderboardTab.SESSIONS_TAUGHT,
                    onClick = { viewModel.setLeaderboardTab(LeaderboardTab.SESSIONS_TAUGHT) },
                    text = { Text("Sessions Taught", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == LeaderboardTab.RATING,
                    onClick = { viewModel.setLeaderboardTab(LeaderboardTab.RATING) },
                    text = { Text("Highest Rated", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Top 3 Podium
                if (sortedUsers.size >= 3) {
                    item {
                        PodiumView(
                            first = sortedUsers[0],
                            second = sortedUsers[1],
                            third = sortedUsers[2],
                            activeTab = activeTab
                        )
                    }
                }

                item {
                    Text(
                        text = "All Ranked Peers (${sortedUsers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                itemsIndexed(sortedUsers) { index, user ->
                    val isCurrentUser = user.userId == currentUser?.userId
                    LeaderboardUserRow(
                        rank = index + 1,
                        user = user,
                        isCurrentUser = isCurrentUser,
                        activeTab = activeTab
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumView(
    first: User,
    second: User,
    third: User,
    activeTab: LeaderboardTab
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🏆 Top Peer Mentors",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place
                PodiumColumn(user = second, rank = 2, height = 90.dp, color = Color(0xFF94A3B8), activeTab = activeTab)

                // 1st Place
                PodiumColumn(user = first, rank = 1, height = 120.dp, color = GoldPoints, isFirst = true, activeTab = activeTab)

                // 3rd Place
                PodiumColumn(user = third, rank = 3, height = 75.dp, color = Color(0xFFCD7F32), activeTab = activeTab)
            }
        }
    }
}

@Composable
private fun PodiumColumn(
    user: User,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color,
    isFirst: Boolean = false,
    activeTab: LeaderboardTab
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        if (isFirst) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = GoldPoints,
                modifier = Modifier.size(24.dp)
            )
        }

        AsyncImage(
            model = user.profileImage.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
            contentDescription = user.fullName,
            modifier = Modifier
                .size(if (isFirst) 56.dp else 46.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.fullName.split(" ").first(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Text(
            text = when (activeTab) {
                LeaderboardTab.POINTS_EARNED -> "${user.skillPoints} Pts"
                LeaderboardTab.SESSIONS_TAUGHT -> "${user.completedSessions} Taught"
                LeaderboardTab.RATING -> "${user.averageRating} ★"
            },
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Pedestal
        Box(
            modifier = Modifier
                .width(if (isFirst) 80.dp else 65.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.6f))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun LeaderboardUserRow(
    rank: Int,
    user: User,
    isCurrentUser: Boolean,
    activeTab: LeaderboardTab
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("leaderboard_row_$rank"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "#$rank",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (rank <= 3) GoldPointsDark else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(28.dp)
                )

                AsyncImage(
                    model = user.profileImage.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
                    contentDescription = user.fullName,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.fullName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "YOU",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "${user.completedSessions} sessions taught • ⭐ ${user.averageRating}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stat Value Pill
            Surface(
                color = GoldPointsContainer,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (activeTab) {
                        LeaderboardTab.POINTS_EARNED -> "${user.skillPoints} Pts"
                        LeaderboardTab.SESSIONS_TAUGHT -> "${user.completedSessions} Taught"
                        LeaderboardTab.RATING -> "${user.averageRating} ★"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GoldPointsDark,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
