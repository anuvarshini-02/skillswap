package com.example.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Marketplace : Screen("marketplace")
    object SkillDetails : Screen("skill_details/{skillId}") {
        fun createRoute(skillId: String) = "skill_details/$skillId"
    }
    object AddSkill : Screen("add_skill")
    object Requests : Screen("requests")
    object Sessions : Screen("sessions")
    object Leaderboard : Screen("leaderboard")
    object Notifications : Screen("notifications")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object EditProfile : Screen("edit_profile")
    object Admin : Screen("admin")
}
