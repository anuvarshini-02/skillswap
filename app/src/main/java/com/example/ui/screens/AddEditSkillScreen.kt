package com.example.ui.screens

import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SessionMode
import com.example.data.model.SkillCategory
import com.example.data.model.SkillLevel
import com.example.ui.theme.GoldPointsDark
import com.example.ui.theme.IndigoContainer
import com.example.ui.theme.IndigoPrimaryDark
import com.example.ui.viewmodel.SkillSwapViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditSkillScreen(
    viewModel: SkillSwapViewModel,
    onNavigateBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Programming") }
    var description by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf(SkillLevel.BEGINNER) }
    var durationMinutes by remember { mutableStateOf("60") }
    var pointsCost by remember { mutableStateOf("100") }
    var selectedMode by remember { mutableStateOf(SessionMode.ONLINE) }
    var outcome1 by remember { mutableStateOf("") }
    var outcome2 by remember { mutableStateOf("") }
    var outcome3 by remember { mutableStateOf("") }
    var prerequisites by remember { mutableStateOf("None, absolute beginners welcome!") }
    var availableTimes by remember { mutableStateOf("4:00 PM - 8:00 PM") }
    var imageUrl by remember { mutableStateOf("") }

    val allDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val selectedDays = remember { mutableStateListOf("Mon", "Wed", "Fri", "Sat") }

    var categoryExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teach a Skill", fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Earning points banner
            Surface(
                color = IndigoContainer,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = GoldPointsDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Teach Peers → Earn Skill Points",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = IndigoPrimaryDark
                        )
                        Text(
                            text = "Each completed session transfers the agreed points directly to your balance.",
                            fontSize = 11.sp,
                            color = IndigoPrimaryDark.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Skill Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Skill Title (e.g. Intro to Python Coding) *") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skill_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Selector
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { categoryExpanded = !categoryExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    SkillCategory.ALL_CATEGORIES.filter { it.id != "all" }.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name) },
                            onClick = {
                                selectedCategory = cat.name
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Skill Description *") },
                placeholder = { Text("Provide an overview of your session, teaching style, and goals...") },
                maxLines = 4,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("skill_description_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Level Selection Chips
            Column {
                Text(text = "Target Skill Level", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SkillLevel.values().forEach { level ->
                        FilterChip(
                            selected = selectedLevel == level,
                            onClick = { selectedLevel = level },
                            label = { Text(level.displayName, fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Session Mode
            Column {
                Text(text = "Session Mode", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SessionMode.values().forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(if (mode == SessionMode.ONLINE) "Online" else if (mode == SessionMode.IN_PERSON) "In-Person" else "Hybrid", fontSize = 12.sp) }
                        )
                    }
                }
            }

            // Duration and Points Price Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = durationMinutes,
                    onValueChange = { durationMinutes = it.filter { char -> char.isDigit() } },
                    label = { Text("Duration (Minutes)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = pointsCost,
                    onValueChange = { pointsCost = it.filter { char -> char.isDigit() } },
                    label = { Text("Skill Points Price *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("skill_points_price_input"),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Learning Outcomes
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "What Learners Will Learn (Outcomes)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                OutlinedTextField(
                    value = outcome1,
                    onValueChange = { outcome1 = it },
                    placeholder = { Text("1. e.g. Master core fundamentals") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = outcome2,
                    onValueChange = { outcome2 = it },
                    placeholder = { Text("2. e.g. Hands-on coding exercise") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = outcome3,
                    onValueChange = { outcome3 = it },
                    placeholder = { Text("3. e.g. Real project walkthrough") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Prerequisites
            OutlinedTextField(
                value = prerequisites,
                onValueChange = { prerequisites = it },
                label = { Text("Prerequisites & Required Tools") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Available Days Chips
            Column {
                Text(text = "Available Days", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allDays.forEach { day ->
                        val isSelected = selectedDays.contains(day)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedDays.remove(day)
                                else selectedDays.add(day)
                            },
                            label = { Text(day) }
                        )
                    }
                }
            }

            // Available Times
            OutlinedTextField(
                value = availableTimes,
                onValueChange = { availableTimes = it },
                label = { Text("Available Time Slots (e.g. 4:00 PM - 8:00 PM)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Optional Image URL
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text("Optional Skill Image URL") },
                placeholder = { Text("https://...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Publish Button
            Button(
                onClick = {
                    val outcomes = listOf(outcome1, outcome2, outcome3).filter { it.isNotBlank() }
                    val pts = pointsCost.toIntOrNull() ?: 100
                    val dur = durationMinutes.toIntOrNull() ?: 60

                    val success = viewModel.addSkill(
                        title = title,
                        category = selectedCategory,
                        description = description.ifBlank { "Exciting skill exchange session tailored to your learning pace." },
                        level = selectedLevel,
                        durationMinutes = dur,
                        pointsCost = pts,
                        mode = selectedMode,
                        learnOutcomes = if (outcomes.isEmpty()) listOf("Hands-on 1-on-1 peer instruction", "Practical exercises and Q&A") else outcomes,
                        prerequisites = prerequisites,
                        availableDays = selectedDays.toList(),
                        availableTimes = availableTimes,
                        imageUrl = imageUrl
                    )

                    if (success) {
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("publish_skill_button"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Publish Skill to Marketplace",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
