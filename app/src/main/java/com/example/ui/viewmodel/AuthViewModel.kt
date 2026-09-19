package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.User
import com.example.data.repository.SkillSwapRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Authenticated(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(
    private val repository: SkillSwapRepository = SkillSwapRepository.getInstance()
) : ViewModel() {

    val currentUser: StateFlow<User?> = repository.currentUser
    val isFirebaseConnected: StateFlow<Boolean> = repository.isFirebaseConnected

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun clearToast() {
        _toastMessage.value = null
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter both email and password")
            return
        }
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.login(email, pass)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                    _toastMessage.value = "Welcome back, ${user.fullName}!"
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Login failed. Check your credentials.")
                }
            )
        }
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPass: String,
        phone: String,
        bio: String,
        location: String,
        profileImage: String,
        skillsTeaching: List<String>,
        skillsLearning: List<String>
    ) {
        if (fullName.isBlank()) {
            _uiState.value = AuthUiState.Error("Please enter your full name")
            return
        }
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        if (password.length < 6) {
            _uiState.value = AuthUiState.Error("Password must be at least 6 characters long")
            return
        }
        if (password != confirmPass) {
            _uiState.value = AuthUiState.Error("Passwords do not match")
            return
        }

        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            val result = repository.register(
                fullName = fullName,
                email = email,
                password = password,
                phone = phone,
                bio = bio,
                location = location,
                profileImage = profileImage,
                skillsTeaching = skillsTeaching,
                skillsLearning = skillsLearning
            )
            result.fold(
                onSuccess = { user ->
                    _uiState.value = AuthUiState.Authenticated(user)
                    _toastMessage.value = "Account created! 200 Welcome Points added."
                },
                onFailure = { error ->
                    _uiState.value = AuthUiState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun forgotPassword(email: String) {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _uiState.value = AuthUiState.Error("Please enter a valid email address")
            return
        }
        viewModelScope.launch {
            val res = repository.forgotPassword(email)
            res.fold(
                onSuccess = { msg ->
                    _toastMessage.value = msg
                    _uiState.value = AuthUiState.Idle
                },
                onFailure = { err ->
                    _uiState.value = AuthUiState.Error(err.message ?: "Failed to send reset link")
                }
            )
        }
    }

    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState.Idle
        _toastMessage.value = "Logged out successfully"
    }

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }
}
