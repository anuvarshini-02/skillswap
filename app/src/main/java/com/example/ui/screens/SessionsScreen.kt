package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Session
import com.example.data.model.SessionMode
import com.example.data.model.SessionStatus
import com.example.ui.components.EmptyState
import com.example.ui.components.RatingBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillSwapViewModel

enum class SessionTab {
    UPCOMING,
    COMPLETED,
    CANCELLED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: SkillSwapViewModel,
    onNavigateToMarketplace: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allSessions by viewModel.allSessions.collectAsState()

    var activeTab by remember { mutableStateOf(SessionTab.UPCOMING) }
    var reviewSessionTarget by remember { mutableStateOf<Session?>(null) }
    var reviewRating by remember { mutableStateOf(5.0f) }
    var reviewComment by remember { mutableStateOf("") }

    val userSessions = allSessions.filter {
        it.learnerId == currentUser?.userId || it.mentorId == currentUser?.userId
    }

    val upcomingSessions = userSessions.filter { it.status == SessionStatus.UPCOMING }
    val completedSessions = userSessions.filter { it.status == SessionStatus.COMPLETED }
    val cancelledSessions = userSessions.filter { it.status == SessionStatus.CANCELLED }

    val currentList = when (activeTab) {
        SessionTab.UPCOMING -> upcomingSessions
        SessionTab.COMPLETED -> completedSessions
        SessionTab.CANCELLED -> cancelledSessions
    }

    val clipboardManager = LocalClipboardManager.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                TopAppBar(
                    title = {
                        Text(
                            text = "My Skill Sessions",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )

                TabRow(
                    selectedTabIndex = activeTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = activeTab == SessionTab.UPCOMING,
                        onClick = { activeTab = SessionTab.UPCOMING },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Upcoming", fontWeight = FontWeight.Bold)
                                if (upcomingSessions.isNotEmpty()) {
                                    Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                        Text("${upcomingSessions.size}", color = Color.White)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == SessionTab.COMPLETED,
                        onClick = { activeTab = SessionTab.COMPLETED },
                        text = { Text("Completed", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = activeTab == SessionTab.CANCELLED,
                        onClick = { activeTab = SessionTab.CANCELLED },
                        text = { Text("Cancelled", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (currentList.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.CalendarToday,
                    title = when (activeTab) {
                        SessionTab.UPCOMING -> "No Upcoming Sessions"
                        SessionTab.COMPLETED -> "No Completed Sessions"
                        SessionTab.CANCELLED -> "No Cancelled Sessions"
                    },
                    message = when (activeTab) {
                        SessionTab.UPCOMING -> "Accept a request or book a mentor to start exchanging knowledge!"
                        SessionTab.COMPLETED -> "Sessions you complete will show here along with point records and reviews."
                        SessionTab.CANCELLED -> "No cancelled sessions."
                    },
                    actionText = if (activeTab == SessionTab.UPCOMING) "Find a Skill" else null,
                    onActionClick = if (activeTab == SessionTab.UPCOMING) onNavigateToMarketplace else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(currentList, key = { it.sessionId }) { session ->
                        val isCurrentUserMentor = session.mentorId == currentUser?.userId
                        val partnerName = if (isCurrentUserMentor) session.learnerName else session.mentorName
                        val partnerPhoto = if (isCurrentUserMentor) session.learnerPhoto else session.mentorPhoto

                        SessionCard(
                            session = session,
                            isMentor = isCurrentUserMentor,
                            partnerName = partnerName,
                            partnerPhoto = partnerPhoto,
                            onComplete = {
                                viewModel.completeSession(session.sessionId)
                                if (!isCurrentUserMentor) {
                                    reviewSessionTarget = session
                                }
                            },
                            onCancel = { viewModel.cancelSession(session.sessionId) },
                            onReviewClick = { reviewSessionTarget = session },
                            onCopyMeetingLink = {
                                clipboardManager.setText(AnnotatedString(session.meetingLinkOrLocation))
                                viewModel.setSnackbarMessage("Meeting link copied to clipboard 📋")
                            }
                        )
                    }
                }
            }
        }
    }

    // Review & Rating Modal Dialog
    if (reviewSessionTarget != null) {
        val targetSession = reviewSessionTarget!!
        AlertDialog(
            onDismissRequest = { reviewSessionTarget = null },
            title = {
                Text(
                    text = "Rate Mentor & Session",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "How was your learning session for '${targetSession.skillTitle}' with ${targetSession.mentorName}?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    RatingBar(
                        rating = reviewRating,
                        onRatingChanged = { reviewRating = it },
                        starSize = 32.dp
                    )

                    Text(
                        text = "%.1f Stars".format(reviewRating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = GoldPointsDark
                    )

                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("Feedback & Review") },
                        placeholder = { Text("Great session! Taught concepts clearly and patiently...") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val commentToSave = if (reviewComment.isBlank()) "Excellent session! Very helpful mentor." else reviewComment
                        viewModel.submitReview(targetSession.sessionId, reviewRating, commentToSave)
                        reviewSessionTarget = null
                        reviewComment = ""
                    },
                    modifier = Modifier.testTag("submit_review_button")
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { reviewSessionTarget = null }) {
                    Text("Later")
                }
            }
        )
    }
}

@Composable
private fun SessionCard(
    session: Session,
    isMentor: Boolean,
    partnerName: String,
    partnerPhoto: String,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
    onReviewClick: () -> Unit,
    onCopyMeetingLink: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Partner & Skill title
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = partnerPhoto.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
                        contentDescription = partnerName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = session.skillTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMentor) "Learner: $partnerName" else "Mentor: $partnerName",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Points Pill
                Surface(
                    color = if (isMentor) SuccessGreenContainer else GoldPointsContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isMentor) "+${session.skillPoints} Pts" else "-${session.skillPoints} Pts",
                        color = if (isMentor) Color(0xFF065F46) else GoldPointsDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Schedule info
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${session.scheduledDate} • ${session.scheduledTime}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = "${session.durationMinutes} min",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Meeting Link / Location
            if (session.status == SessionStatus.UPCOMING) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (session.mode == SessionMode.ONLINE) Icons.Default.Videocam else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (session.mode == SessionMode.ONLINE) "Online: ${session.meetingLinkOrLocation}" else "In-Person: ${session.meetingLinkOrLocation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1
                            )
                        }

                        if (session.mode == SessionMode.ONLINE) {
                            Text(
                                text = "Copy",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onCopyMeetingLink() }
                            )
                        }
                    }
                }
            }

            // Action Buttons for Upcoming
            if (session.status == SessionStatus.UPCOMING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = onComplete,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .testTag("complete_session_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark Completed", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Action for Completed
            if (session.status == SessionStatus.COMPLETED && !isMentor && !session.isReviewed) {
                Button(
                    onClick = onReviewClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPointsDark)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Leave Rating & Review")
                }
            }
        }
    }
}
