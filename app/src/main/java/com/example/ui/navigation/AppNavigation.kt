package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.data.model.RequestStatus
import com.example.data.model.SessionStatus
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.SkillSwapViewModel
import kotlinx.coroutines.launch

data class BottomNavItem(
    val title: String,
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = viewModel(),
    skillSwapViewModel: SkillSwapViewModel = viewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val currentUser by authViewModel.currentUser.collectAsState()
    val allRequests by skillSwapViewModel.allRequests.collectAsState()
    val allSessions by skillSwapViewModel.allSessions.collectAsState()
    val snackbarMessage by skillSwapViewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show snackbars from ViewModel
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            scope.launch {
                snackbarHostState.showSnackbar(msg)
                skillSwapViewModel.clearSnackbar()
            }
        }
    }

    val bottomNavItems = listOf(
        BottomNavItem(
            title = "Home",
            route = Screen.Home.route,
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            testTag = "nav_home"
        ),
        BottomNavItem(
            title = "Explore",
            route = Screen.Marketplace.route,
            selectedIcon = Icons.Filled.Explore,
            unselectedIcon = Icons.Outlined.Explore,
            testTag = "nav_marketplace"
        ),
        BottomNavItem(
            title = "Requests",
            route = Screen.Requests.route,
            selectedIcon = Icons.Filled.Mail,
            unselectedIcon = Icons.Outlined.MailOutline,
            testTag = "nav_requests"
        ),
        BottomNavItem(
            title = "Sessions",
            route = Screen.Sessions.route,
            selectedIcon = Icons.Filled.CalendarMonth,
            unselectedIcon = Icons.Outlined.CalendarToday,
            testTag = "nav_sessions"
        ),
        BottomNavItem(
            title = "Profile",
            route = Screen.Profile.route,
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
            testTag = "nav_profile"
        )
    )

    // Only show bottom bar on primary top-level tabs
    val showBottomBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Marketplace.route,
        Screen.Requests.route,
        Screen.Sessions.route,
        Screen.Profile.route
    )

    // Compute badges
    val pendingReceivedCount = remember(allRequests, currentUser) {
        if (currentUser == null) 0
        else allRequests.count { it.mentorId == currentUser?.userId && it.status == RequestStatus.PENDING }
    }

    val upcomingSessionsCount = remember(allSessions, currentUser) {
        if (currentUser == null) 0
        else allSessions.count {
            it.status == SessionStatus.UPCOMING && (it.learnerId == currentUser?.userId || it.mentorId == currentUser?.userId)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                NavigationBar(
                    containerColor = SurfaceHeaderDark,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        val isSelected = currentRoute == item.route

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (item.route == Screen.Requests.route && pendingReceivedCount > 0) {
                                            Badge(containerColor = GoldPoints) {
                                                Text("$pendingReceivedCount", color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (item.route == Screen.Sessions.route && upcomingSessionsCount > 0) {
                                            Badge(containerColor = ElegantPurplePrimary) {
                                                Text("$upcomingSessionsCount", color = ElegantPurplePrimaryDark, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = ElegantPurpleOnContainer,
                                selectedTextColor = ElegantPurpleOnContainer,
                                unselectedIconColor = TextTertiaryDark,
                                unselectedTextColor = TextTertiaryDark,
                                indicatorColor = ElegantPurplePrimaryContainer
                            ),
                            modifier = Modifier.testTag(item.testTag)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Splash Screen
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        val destination = if (currentUser != null) Screen.Home.route else Screen.Login.route
                        navController.navigate(destination) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            // Login Screen
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Register Screen
            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Forgot Password Screen
            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Home Screen
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateToSkillDetails = { skillId ->
                        navController.navigate(Screen.SkillDetails.createRoute(skillId))
                    },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) },
                    onNavigateToAddSkill = { navController.navigate(Screen.AddSkill.route) },
                    onNavigateToRequests = { navController.navigate(Screen.Requests.route) },
                    onNavigateToSessions = { navController.navigate(Screen.Sessions.route) },
                    onNavigateToLeaderboard = { navController.navigate(Screen.Leaderboard.route) },
                    onNavigateToNotifications = { navController.navigate(Screen.Notifications.route) },
                    onNavigateToProfile = { navController.navigate(Screen.Profile.route) }
                )
            }

            // Marketplace Screen
            composable(Screen.Marketplace.route) {
                MarketplaceScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateToSkillDetails = { skillId ->
                        navController.navigate(Screen.SkillDetails.createRoute(skillId))
                    },
                    onNavigateToAddSkill = { navController.navigate(Screen.AddSkill.route) }
                )
            }

            // Skill Details Screen
            composable(
                route = Screen.SkillDetails.route,
                arguments = listOf(navArgument("skillId") { type = NavType.StringType })
            ) { backStackEntry ->
                val skillId = backStackEntry.arguments?.getString("skillId") ?: ""
                SkillDetailsScreen(
                    skillId = skillId,
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRequests = { navController.navigate(Screen.Requests.route) }
                )
            }

            // Add/Edit Skill Screen
            composable(Screen.AddSkill.route) {
                AddEditSkillScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Requests Screen
            composable(Screen.Requests.route) {
                RequestsScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateToSessions = { navController.navigate(Screen.Sessions.route) },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                )
            }

            // Sessions Screen
            composable(Screen.Sessions.route) {
                SessionsScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                )
            }

            // Leaderboard Screen
            composable(Screen.Leaderboard.route) {
                LeaderboardScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Notifications Screen
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToRequests = { navController.navigate(Screen.Requests.route) },
                    onNavigateToSessions = { navController.navigate(Screen.Sessions.route) }
                )
            }

            // Favorites Screen
            composable(Screen.Favorites.route) {
                FavoritesScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSkillDetails = { skillId ->
                        navController.navigate(Screen.SkillDetails.createRoute(skillId))
                    },
                    onNavigateToMarketplace = { navController.navigate(Screen.Marketplace.route) }
                )
            }

            // Profile Screen
            composable(Screen.Profile.route) {
                ProfileScreen(
                    viewModel = skillSwapViewModel,
                    authViewModel = authViewModel,
                    onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                    onNavigateToAdmin = { navController.navigate(Screen.Admin.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            // Edit Profile Screen
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Admin Screen
            composable(Screen.Admin.route) {
                AdminScreen(
                    viewModel = skillSwapViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
