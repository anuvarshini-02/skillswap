package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.EmptyState
import com.example.ui.components.SkillCard
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSkillDetails: (String) -> Unit,
    onNavigateToMarketplace: () -> Unit
) {
    val allSkills by viewModel.allSkills.collectAsState()
    val favorites by viewModel.favorites.collectAsState()

    val favoriteSkills = allSkills.filter { favorites.contains(it.skillId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Favorite Skills", fontWeight = FontWeight.Bold) },
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
            if (favoriteSkills.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.FavoriteBorder,
                    title = "No Favorites Saved",
                    message = "Tap the heart icon on any skill in the marketplace to bookmark it for later!",
                    actionText = "Explore Skills",
                    onActionClick = onNavigateToMarketplace
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteSkills, key = { it.skillId }) { skill ->
                        SkillCard(
                            skill = skill,
                            isFavorite = true,
                            onSkillClick = { onNavigateToSkillDetails(skill.skillId) },
                            onFavoriteClick = { viewModel.toggleFavorite(skill.skillId) }
                        )
                    }
                }
            }
        }
    }
}
