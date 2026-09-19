package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var fullName by remember { mutableStateOf(currentUser?.fullName ?: "") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var location by remember { mutableStateOf(currentUser?.location ?: "") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }
    var profileImage by remember { mutableStateOf(currentUser?.profileImage ?: "") }

    val defaultTeachOptions = listOf("Python", "Java", "Kotlin", "JavaScript", "UI/UX", "Guitar", "Photography", "Speaking", "Calculus", "Physics")
    val defaultLearnOptions = listOf("Web Dev", "Figma", "Data Science", "Machine Learning", "Music", "Fitness", "Spanish", "Cooking", "AI Tools")

    val selectedTeaching = remember { mutableStateListOf<String>().apply { currentUser?.skillsTeaching?.let { addAll(it) } } }
    val selectedLearning = remember { mutableStateListOf<String>().apply { currentUser?.skillsLearning?.let { addAll(it) } } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Full Name") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_name_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Campus / Location") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("About You (Bio)") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = profileImage,
                onValueChange = { profileImage = it },
                label = { Text("Profile Image URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Skills Teaching
            Text(
                text = "Skills I Teach (Mentoring)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                defaultTeachOptions.forEach { skill ->
                    val isSelected = selectedTeaching.contains(skill)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedTeaching.remove(skill)
                            else selectedTeaching.add(skill)
                        },
                        label = { Text(skill) }
                    )
                }
            }

            // Skills Learning
            Text(
                text = "Skills I Want to Learn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                defaultLearnOptions.forEach { skill ->
                    val isSelected = selectedLearning.contains(skill)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (isSelected) selectedLearning.remove(skill)
                            else selectedLearning.add(skill)
                        },
                        label = { Text(skill) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val current = currentUser ?: return@Button
                    val updated = current.copy(
                        fullName = fullName,
                        phone = phone,
                        location = location,
                        bio = bio,
                        profileImage = profileImage,
                        skillsTeaching = selectedTeaching.toList(),
                        skillsLearning = selectedLearning.toList()
                    )
                    viewModel.updateProfile(updated)
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_profile_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Profile Changes", fontWeight = FontWeight.Bold)
            }
        }
    }
}
