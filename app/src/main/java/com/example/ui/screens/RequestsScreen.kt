package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.RequestStatus
import com.example.data.model.SkillRequest
import com.example.ui.components.EmptyState
import com.example.ui.theme.*
import com.example.ui.viewmodel.SkillSwapViewModel

enum class RequestTab {
    RECEIVED,
    SENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestsScreen(
    viewModel: SkillSwapViewModel,
    onNavigateToSessions: () -> Unit,
    onNavigateToMarketplace: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allRequests by viewModel.allRequests.collectAsState()

    var activeTab by remember { mutableStateOf(RequestTab.RECEIVED) }

    val receivedRequests = allRequests.filter { it.mentorId == currentUser?.userId }
    val sentRequests = allRequests.filter { it.learnerId == currentUser?.userId }

    val currentList = if (activeTab == RequestTab.RECEIVED) receivedRequests else sentRequests

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
                            text = "Learning Requests",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )

                // Tab Row: Received (Mentor) vs Sent (Learner)
                TabRow(
                    selectedTabIndex = if (activeTab == RequestTab.RECEIVED) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = activeTab == RequestTab.RECEIVED,
                        onClick = { activeTab = RequestTab.RECEIVED },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Received (Mentor)", fontWeight = FontWeight.Bold)
                                val pendingReceived = receivedRequests.count { it.status == RequestStatus.PENDING }
                                if (pendingReceived > 0) {
                                    Badge(containerColor = ErrorRed) {
                                        Text("$pendingReceived", color = Color.White)
                                    }
                                }
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == RequestTab.SENT,
                        onClick = { activeTab = RequestTab.SENT },
                        text = { Text("Sent (Learner)", fontWeight = FontWeight.Bold) }
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
                    icon = Icons.Default.MailOutline,
                    title = if (activeTab == RequestTab.RECEIVED) "No Received Requests" else "No Sent Requests",
                    message = if (activeTab == RequestTab.RECEIVED)
                        "When learners request your teaching sessions, they will appear here for you to accept."
                    else
                        "Explore the marketplace and request a session with a peer mentor!",
                    actionText = if (activeTab == RequestTab.SENT) "Browse Skills" else null,
                    onActionClick = if (activeTab == RequestTab.SENT) onNavigateToMarketplace else null
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(currentList, key = { it.requestId }) { request ->
                        RequestCard(
                            request = request,
                            isMentorView = activeTab == RequestTab.RECEIVED,
                            onAccept = {
                                viewModel.acceptRequest(request.requestId)
                                onNavigateToSessions()
                            },
                            onReject = { viewModel.rejectRequest(request.requestId) },
                            onCancel = { viewModel.cancelRequest(request.requestId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: SkillRequest,
    isMentorView: Boolean,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onCancel: () -> Unit
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
            // Header: User photo + Skill title + Status badge
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
                    val userPhoto = if (isMentorView) request.learnerPhoto else request.mentorPhoto
                    val userName = if (isMentorView) request.learnerName else request.mentorName
                    val userRole = if (isMentorView) "Learner" else "Mentor"

                    AsyncImage(
                        model = userPhoto.ifBlank { "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&auto=format&fit=crop&q=80" },
                        contentDescription = userName,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Column {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$userRole • ${request.pointsCost} Points",
                            fontSize = 12.sp,
                            color = GoldPointsDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                StatusBadge(status = request.status)
            }

            // Skill Title & Requested Schedule
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Skill: ${request.skillTitle}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${request.scheduledDate} at ${request.scheduledTime}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Learner Message
            if (request.message.isNotBlank()) {
                Text(
                    text = "\"${request.message}\"",
                    fontSize = 13.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Action Buttons
            if (request.status == RequestStatus.PENDING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isMentorView) {
                        OutlinedButton(
                            onClick = onReject,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Decline")
                        }

                        Button(
                            onClick = onAccept,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("accept_request_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Accept")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onCancel,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Request")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: RequestStatus) {
    val (bgColor, textColor, text) = when (status) {
        RequestStatus.PENDING -> Triple(WarningOrange.copy(alpha = 0.2f), WarningOrange, "PENDING")
        RequestStatus.ACCEPTED -> Triple(SuccessGreenContainer, Color(0xFF065F46), "ACCEPTED")
        RequestStatus.REJECTED -> Triple(ErrorRedContainer, ErrorRed, "DECLINED")
        RequestStatus.COMPLETED -> Triple(IndigoContainer, IndigoPrimaryDark, "COMPLETED")
        RequestStatus.CANCELLED -> Triple(DividerLight, TextTertiaryLight, "CANCELLED")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
