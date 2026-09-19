package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SkillSwapRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class LeaderboardTab {
    POINTS_EARNED,
    SESSIONS_TAUGHT,
    RATING
}

class SkillSwapViewModel(
    private val repository: SkillSwapRepository = SkillSwapRepository.getInstance()
) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser
    val allUsers: StateFlow<List<User>> = repository.allUsers
    val allSkills: StateFlow<List<Skill>> = repository.allSkills
    val allRequests: StateFlow<List<SkillRequest>> = repository.allRequests
    val allSessions: StateFlow<List<Session>> = repository.allSessions
    val allReviews: StateFlow<List<Review>> = repository.allReviews
    val allTransactions: StateFlow<List<PointTransaction>> = repository.allTransactions
    val allNotifications: StateFlow<List<AppNotification>> = repository.allNotifications
    val favorites: StateFlow<Set<String>> = repository.favorites
    val isFirebaseConnected: StateFlow<Boolean> = repository.isFirebaseConnected
    val isDatabaseConnected: StateFlow<Boolean> = repository.isDatabaseConnected
    val apiSyncStatus: StateFlow<String> = repository.apiSyncStatus
    val isSyncing: StateFlow<Boolean> = repository.isSyncing
    val isFetchingFirestoreUsers: StateFlow<Boolean> = repository.isFetchingFirestoreUsers
    val firestoreUsersStatus: StateFlow<String?> = repository.firestoreUsersStatus

    // Filter states for Marketplace
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("all")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedLevel = MutableStateFlow<SkillLevel?>(null)
    val selectedLevel: StateFlow<SkillLevel?> = _selectedLevel.asStateFlow()

    private val _selectedMode = MutableStateFlow<SessionMode?>(null)
    val selectedMode: StateFlow<SessionMode?> = _selectedMode.asStateFlow()

    private val _maxPointsFilter = MutableStateFlow<Int?>(null)
    val maxPointsFilter: StateFlow<Int?> = _maxPointsFilter.asStateFlow()

    private val _minRatingFilter = MutableStateFlow<Float?>(null)
    val minRatingFilter: StateFlow<Float?> = _minRatingFilter.asStateFlow()

    // Leaderboard active tab
    private val _leaderboardTab = MutableStateFlow(LeaderboardTab.POINTS_EARNED)
    val leaderboardTab: StateFlow<LeaderboardTab> = _leaderboardTab.asStateFlow()

    // Feedback message (Snackbar/Toast)
    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    fun setSnackbarMessage(msg: String) {
        _snackbarMessage.value = msg
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    // Filtered skills
    val filteredSkills: StateFlow<List<Skill>> = combine(
        allSkills,
        searchQuery,
        selectedCategory,
        selectedLevel,
        selectedMode
    ) { skills, query, cat, level, mode ->
        skills.filter { skill ->
            val matchesQuery = query.isBlank() ||
                    skill.title.contains(query, ignoreCase = true) ||
                    skill.description.contains(query, ignoreCase = true) ||
                    skill.mentorName.contains(query, ignoreCase = true) ||
                    skill.category.contains(query, ignoreCase = true)

            val matchesCategory = cat.equals("all", ignoreCase = true) ||
                    skill.category.equals(cat, ignoreCase = true) ||
                    (cat.equals("programming", ignoreCase = true) && skill.category.contains("code", ignoreCase = true))

            val matchesLevel = level == null || skill.skillLevel == level || skill.skillLevel == SkillLevel.ALL_LEVELS
            val matchesMode = mode == null || skill.mode == mode || skill.mode == SessionMode.HYBRID

            matchesQuery && matchesCategory && matchesLevel && matchesMode && skill.isActive
        }
    }.combine(maxPointsFilter) { list, maxPts ->
        if (maxPts == null) list else list.filter { it.pointsCost <= maxPts }
    }.combine(minRatingFilter) { list, minRat ->
        if (minRat == null) list else list.filter { it.mentorRating >= minRat }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unread notification count
    val unreadNotificationCount: StateFlow<Int> = combine(
        allNotifications,
        currentUser
    ) { notifs, user ->
        if (user == null) 0
        else notifs.count { it.userId == user.userId && !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Pending requests count for current user as mentor
    val pendingReceivedRequestsCount: StateFlow<Int> = combine(
        allRequests,
        currentUser
    ) { reqs, user ->
        if (user == null) 0
        else reqs.count { it.mentorId == user.userId && it.status == RequestStatus.PENDING }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Marketplace filter actions
    fun setSearchQuery(q: String) { _searchQuery.value = q }
    fun setCategory(cat: String) { _selectedCategory.value = cat }
    fun setSkillLevel(level: SkillLevel?) { _selectedLevel.value = level }
    fun setSessionMode(mode: SessionMode?) { _selectedMode.value = mode }
    fun setMaxPoints(pts: Int?) { _maxPointsFilter.value = pts }
    fun setMinRating(rat: Float?) { _minRatingFilter.value = rat }
    fun resetFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "all"
        _selectedLevel.value = null
        _selectedMode.value = null
        _maxPointsFilter.value = null
        _minRatingFilter.value = null
    }

    fun setLeaderboardTab(tab: LeaderboardTab) {
        _leaderboardTab.value = tab
    }

    // Favorites
    fun toggleFavorite(skillId: String) {
        repository.toggleFavorite(skillId)
        val isFav = repository.favorites.value.contains(skillId)
        _snackbarMessage.value = if (isFav) "Added to Favorites ❤️" else "Removed from Favorites"
    }

    // Skill CRUD
    fun addSkill(
        title: String,
        category: String,
        description: String,
        level: SkillLevel,
        durationMinutes: Int,
        pointsCost: Int,
        mode: SessionMode,
        learnOutcomes: List<String>,
        prerequisites: String,
        availableDays: List<String>,
        availableTimes: String,
        imageUrl: String
    ): Boolean {
        val result = repository.addSkill(
            title = title,
            category = category,
            description = description,
            level = level,
            durationMinutes = durationMinutes,
            pointsCost = pointsCost,
            mode = mode,
            learnOutcomes = learnOutcomes,
            prerequisites = prerequisites,
            availableDays = availableDays,
            availableTimes = availableTimes,
            imageUrl = imageUrl
        )
        return result.fold(
            onSuccess = {
                _snackbarMessage.value = "Skill '${it.title}' published successfully! 🎉"
                true
            },
            onFailure = {
                _snackbarMessage.value = it.message ?: "Failed to add skill"
                false
            }
        )
    }

    fun updateSkill(skill: Skill) {
        repository.updateSkill(skill)
        _snackbarMessage.value = "Skill updated successfully"
    }

    fun deleteSkill(skillId: String) {
        repository.deleteSkill(skillId)
        _snackbarMessage.value = "Skill removed"
    }

    fun toggleSkillAvailability(skillId: String) {
        repository.toggleSkillAvailability(skillId)
    }

    // Request actions
    fun sendSessionRequest(
        skillId: String,
        date: String,
        time: String,
        message: String
    ): Boolean {
        val res = repository.sendRequest(skillId, date, time, message)
        return res.fold(
            onSuccess = {
                _snackbarMessage.value = "Request sent to mentor! 🚀"
                true
            },
            onFailure = {
                _snackbarMessage.value = it.message ?: "Could not send request"
                false
            }
        )
    }

    fun acceptRequest(requestId: String) {
        val res = repository.acceptRequest(requestId)
        res.fold(
            onSuccess = { session ->
                _snackbarMessage.value = "Request accepted! Scheduled session on ${session.scheduledDate} 📅"
            },
            onFailure = {
                _snackbarMessage.value = it.message ?: "Failed to accept request"
            }
        )
    }

    fun rejectRequest(requestId: String) {
        repository.rejectRequest(requestId)
        _snackbarMessage.value = "Request declined"
    }

    fun cancelRequest(requestId: String) {
        repository.cancelRequest(requestId)
        _snackbarMessage.value = "Request cancelled"
    }

    // Session completion
    fun completeSession(sessionId: String) {
        val res = repository.completeSession(sessionId)
        res.fold(
            onSuccess = { session ->
                _snackbarMessage.value = "Session completed! ${session.skillPoints} Skill Points transferred 💎"
            },
            onFailure = {
                _snackbarMessage.value = it.message ?: "Failed to complete session"
            }
        )
    }

    fun cancelSession(sessionId: String) {
        repository.cancelSession(sessionId)
        _snackbarMessage.value = "Session cancelled"
    }

    // Reviews
    fun submitReview(sessionId: String, rating: Float, comment: String): Boolean {
        val res = repository.submitReview(sessionId, rating, comment)
        return res.fold(
            onSuccess = {
                _snackbarMessage.value = "Review submitted! Thank you for rating your mentor ⭐"
                true
            },
            onFailure = {
                _snackbarMessage.value = it.message ?: "Failed to submit review"
                false
            }
        )
    }

    // Profile Edit
    fun updateProfile(user: User) {
        repository.updateProfile(user)
        _snackbarMessage.value = "Profile updated successfully ✨"
    }

    // Notifications
    fun markNotificationAsRead(id: String) {
        repository.markNotificationRead(id)
    }

    fun markAllNotificationsRead() {
        val uid = currentUser.value?.userId ?: return
        repository.markAllNotificationsRead(uid)
    }

    fun clearAllNotifications() {
        val uid = currentUser.value?.userId ?: return
        repository.clearAllNotifications(uid)
        _snackbarMessage.value = "Notifications cleared"
    }

    // Admin Panel Actions
    fun adminToggleSkill(skillId: String, isActive: Boolean) {
        repository.adminToggleSkill(skillId, isActive)
        _snackbarMessage.value = if (isActive) "Skill re-enabled" else "Skill disabled by Admin"
    }

    fun adminDeleteSkill(skillId: String) {
        repository.adminDeleteSkill(skillId)
        _snackbarMessage.value = "Skill permanently deleted by Admin"
    }

    fun adminAddBonusPoints(userId: String, points: Int, reason: String) {
        repository.adminAddBonusPoints(userId, points, reason)
        _snackbarMessage.value = "Awarded $points Bonus Points to user 🎁"
    }

    fun resetToDemoData() {
        repository.loadDemoData()
        _snackbarMessage.value = "Reset to full demo dataset"
    }

    fun syncWithApi() {
        viewModelScope.launch {
            val result = repository.syncWithRemoteApi()
            result.onSuccess { msg ->
                _snackbarMessage.value = msg
            }.onFailure { err ->
                _snackbarMessage.value = "Database active: ${err.localizedMessage ?: "Sync completed"}"
            }
        }
    }

    fun fetchUsersFromFirestore() {
        viewModelScope.launch {
            val result = repository.fetchUsersFromFirestore()
            result.onSuccess { users ->
                _snackbarMessage.value = "Loaded ${users.size} user profiles from Firestore"
            }.onFailure { err ->
                _snackbarMessage.value = "Firestore: ${err.localizedMessage ?: "Loaded local profiles"}"
            }
        }
    }

    fun toggleUserActiveStatus(userId: String, isActive: Boolean) {
        val user = allUsers.value.find { it.userId == userId }
        if (currentUser.value?.userId == userId && !isActive) {
            _snackbarMessage.value = "Security: You cannot deactivate your own active Administrator account"
            return
        }
        viewModelScope.launch {
            val result = repository.toggleUserActiveStatus(userId, isActive)
            result.onSuccess { active ->
                val statusText = if (active) "Active / Allowed" else "Suspended / Inactive"
                _snackbarMessage.value = "Account status updated: ${user?.fullName ?: "User"} is now $statusText"
            }.onFailure { err ->
                _snackbarMessage.value = "Failed to update status: ${err.localizedMessage}"
            }
        }
    }
}
