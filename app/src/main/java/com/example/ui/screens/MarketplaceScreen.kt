package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionMode
import com.example.data.model.SkillCategory
import com.example.data.model.SkillLevel
import com.example.ui.components.CategoryChip
import com.example.ui.components.EmptyState
import com.example.ui.components.SkillCard
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: SkillSwapViewModel,
    onNavigateToSkillDetails: (String) -> Unit,
    onNavigateToAddSkill: () -> Unit
) {
    val filteredSkills by viewModel.filteredSkills.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()
    val maxPoints by viewModel.maxPointsFilter.collectAsState()
    val minRating by viewModel.minRatingFilter.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    var showFilterBottomSheet by remember { mutableStateOf(false) }

    val hasActiveFilters = selectedLevel != null || selectedMode != null || maxPoints != null || minRating != null

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
                            text = "Explore Skills",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        IconButton(
                            onClick = { showFilterBottomSheet = true },
                            modifier = Modifier.testTag("filter_dialog_button")
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Icon(
                                    imageVector = Icons.Outlined.FilterList,
                                    contentDescription = "Filters",
                                    tint = if (hasActiveFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                if (hasActiveFilters) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                }
                            }
                        }
                    }
                )

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search skill, mentor, or topic...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("marketplace_search_input")
                )

                // Categories Scrollable Bar
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SkillCategory.ALL_CATEGORIES) { cat ->
                        val isSelected = (cat.id == "all" && selectedCategory == "all") ||
                                selectedCategory.equals(cat.name, ignoreCase = true) ||
                                selectedCategory.equals(cat.id, ignoreCase = true)

                        CategoryChip(
                            category = cat,
                            isSelected = isSelected,
                            onSelect = {
                                viewModel.setCategory(if (cat.id == "all") "all" else cat.name)
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAddSkill,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_skill_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Teach a Skill")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Teach Skill", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Active filters summary pill row (if any)
            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Filtered Results (${filteredSkills.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Reset All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.clickable { viewModel.resetFilters() }
                    )
                }
            }

            if (filteredSkills.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.SearchOff,
                    title = "No Skills Found",
                    message = "No matching skills found for '$searchQuery'. Try adjusting your filters or search keywords.",
                    actionText = "Clear Filters",
                    onActionClick = { viewModel.resetFilters() }
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Text(
                            text = "Available Peer Skills (${filteredSkills.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    items(filteredSkills, key = { it.skillId }) { skill ->
                        SkillCard(
                            skill = skill,
                            isFavorite = favorites.contains(skill.skillId),
                            onSkillClick = { onNavigateToSkillDetails(skill.skillId) },
                            onFavoriteClick = { viewModel.toggleFavorite(skill.skillId) }
                        )
                    }
                }
            }
        }
    }

    // Filter Bottom Sheet Modal
    if (showFilterBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Filter Skills",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { viewModel.resetFilters() }) {
                        Text("Reset All")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Level Filter
                Text(
                    text = "Skill Level",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        null to "All",
                        SkillLevel.BEGINNER to "Beginner",
                        SkillLevel.INTERMEDIATE to "Intermediate",
                        SkillLevel.ADVANCED to "Advanced"
                    ).forEach { (level, name) ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { viewModel.setSkillLevel(level) },
                            label = { Text(name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mode Filter
                Text(
                    text = "Session Mode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        null to "All",
                        SessionMode.ONLINE to "Online",
                        SessionMode.IN_PERSON to "In-Person"
                    ).forEach { (mode, name) ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { viewModel.setSessionMode(mode) },
                            label = { Text(name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Max Point Cost
                Text(
                    text = "Max Point Cost: ${if (maxPoints == null) "Any" else "$maxPoints Points"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        null to "Any",
                        100 to "≤ 100 Pts",
                        150 to "≤ 150 Pts",
                        200 to "≤ 200 Pts"
                    ).forEach { (pts, label) ->
                        FilterChip(
                            selected = maxPoints == pts,
                            onClick = { viewModel.setMaxPoints(pts) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Rating Filter
                Text(
                    text = "Minimum Mentor Rating",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        null to "Any",
                        4.0f to "4.0 ★+",
                        4.5f to "4.5 ★+",
                        4.8f to "4.8 ★+"
                    ).forEach { (rat, label) ->
                        FilterChip(
                            selected = minRating == rat,
                            onClick = { viewModel.setMinRating(rat) },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { showFilterBottomSheet = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Apply Filters (${filteredSkills.size} skills)")
                }
            }
        }
    }
}
