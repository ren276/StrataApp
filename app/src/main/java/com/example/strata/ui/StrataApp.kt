package com.example.strata.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.strata.data.repository.ActivityRepository
import com.example.strata.ui.auth.AuthViewModel
import com.example.strata.ui.auth.LoginScreen
import com.example.strata.ui.editor.EditorScreen
import com.example.strata.ui.feed.FeedScreen
import com.example.strata.ui.settings.SettingsScreen

object Destinations {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val FEED = "feed"
    const val TEMPLATES = "templates"
    const val EDITOR = "editor"
    const val EDITOR_ROUTE = "editor/{activityId}?templateId={templateId}"
    const val SETTINGS = "settings"
    const val TERMS = "terms"
    const val IMAGE_EDITOR = "image_editor"
    const val IMAGE_EDITOR_ROUTE = "image_editor/{imageUri}"
}

@Composable
fun StrataApp(
    authViewModel: AuthViewModel,
    activityRepository: ActivityRepository
) {
    val navController = rememberNavController()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isGuestMode by authViewModel.isGuestMode.collectAsState()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Auto-navigate to Feed when logged in
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && (currentRoute == Destinations.LOGIN || currentRoute == Destinations.SPLASH)) {
            navController.navigate(Destinations.FEED) {
                popUpTo(Destinations.SPLASH) { inclusive = true }
            }
        }
    }

    // Auto-navigate to Feed when entering guest mode
    LaunchedEffect(isGuestMode) {
        if (isGuestMode && (currentRoute == Destinations.LOGIN || currentRoute == Destinations.SPLASH)) {
            navController.navigate(Destinations.FEED) {
                popUpTo(Destinations.SPLASH) { inclusive = true }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            val bottomNavRoutes = listOf(Destinations.FEED, Destinations.TEMPLATES, Destinations.SETTINGS)
            if (currentRoute?.split("?")?.first() in bottomNavRoutes) {
                val isDark = MaterialTheme.colorScheme.background == com.example.strata.ui.theme.DarkBackground
                val navContainerColor = if (isDark) Color(0xFF23170F) else MaterialTheme.colorScheme.surfaceVariant
                val navContentColor = MaterialTheme.colorScheme.onBackground

                NavigationBar(
                    containerColor = navContainerColor,
                    contentColor = navContentColor
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                        label = { Text("Home") },
                        selected = currentRoute == Destinations.FEED,
                        onClick = {
                            navController.navigate(Destinations.FEED) {
                                popUpTo(Destinations.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = navContentColor.copy(alpha = 0.6f),
                            unselectedTextColor = navContentColor.copy(alpha = 0.6f),
                            indicatorColor = navContentColor.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Templates") },
                        label = { Text("Templates") },
                        selected = currentRoute == Destinations.TEMPLATES,
                        onClick = {
                            navController.navigate(Destinations.TEMPLATES) {
                                popUpTo(Destinations.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = navContentColor.copy(alpha = 0.6f),
                            unselectedTextColor = navContentColor.copy(alpha = 0.6f),
                            indicatorColor = navContentColor.copy(alpha = 0.1f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                        label = { Text("Profile") },
                        selected = currentRoute == Destinations.SETTINGS,
                        onClick = {
                            navController.navigate(Destinations.SETTINGS) {
                                popUpTo(Destinations.FEED) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = navContentColor.copy(alpha = 0.6f),
                            unselectedTextColor = navContentColor.copy(alpha = 0.6f),
                            indicatorColor = navContentColor.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.SPLASH,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Destinations.SPLASH) {
                // rememberUpdatedState ensures the lambda always reads the latest value
                val currentIsLoggedIn by androidx.compose.runtime.rememberUpdatedState(isLoggedIn)
                SplashScreen(
                    onComplete = {
                        if (currentIsLoggedIn) {
                            navController.navigate(Destinations.FEED) {
                                popUpTo(Destinations.SPLASH) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Destinations.LOGIN) {
                                popUpTo(Destinations.SPLASH) { inclusive = true }
                            }
                        }
                    }
                )
            }
            composable(Destinations.LOGIN) {
                LoginScreen(viewModel = authViewModel)
            }
            composable(Destinations.FEED) {
                FeedScreen(
                    onActivityClick = { activityId ->
                        navController.navigate("editor/$activityId")
                    },
                    authViewModel = authViewModel
                )
            }
            composable(Destinations.TEMPLATES) {
                // Replace TrainingScreen with TemplatesScreen
                com.example.strata.ui.training.TemplatesScreen(
                    onTemplateSelected = { templateId, activityId ->
                        navController.navigate("editor/$activityId?templateId=$templateId")
                    }
                )
            }
            composable(
                route = Destinations.EDITOR_ROUTE,
                arguments = listOf(
                    navArgument("activityId") { type = NavType.LongType },
                    navArgument("templateId") { type = NavType.StringType; nullable = true }
                )
            ) { backStackEntry ->
                val activityId = backStackEntry.arguments?.getLong("activityId") ?: 0L
                val templateId = backStackEntry.arguments?.getString("templateId")
                EditorScreen(
                    activityId = activityId,
                    templateId = templateId,
                    repository = activityRepository,
                    navController = navController,
                    onBack = { navController.popBackStack() },
                    onSave = { /* TODO */ }
                )
            }
            composable(
                route = Destinations.IMAGE_EDITOR_ROUTE,
                arguments = listOf(
                    navArgument("imageUri") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri") ?: return@composable
                // Retrieve the EditorViewModel from the editor back-stack entry
                // so the baked bitmap is shared with the editor that launched us.
                val editorEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Destinations.EDITOR_ROUTE)
                }
                val editorViewModel: com.example.strata.ui.editor.EditorViewModel =
                    androidx.lifecycle.viewmodel.compose.viewModel(
                        viewModelStoreOwner = editorEntry,
                        factory = com.example.strata.ui.editor.EditorViewModel.Factory(
                            activityId = editorEntry.arguments?.getLong("activityId") ?: 0L,
                            templateId = editorEntry.arguments?.getString("templateId"),
                            repository = activityRepository,
                            authRepository = (androidx.compose.ui.platform.LocalContext.current.applicationContext
                                as com.example.strata.StrataApplication).container.authRepository
                        )
                    )
                com.example.strata.ui.editor.ImageEditorScreen(
                    imageUri = imageUri,
                    editorViewModel = editorViewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Destinations.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { 
                        authViewModel.logout()
                        navController.navigate(Destinations.LOGIN) {
                            popUpTo(0)
                        }
                    },
                    onTermsClick = {
                        navController.navigate(Destinations.TERMS)
                    },
                    authViewModel = authViewModel
                )
            }
            composable(Destinations.TERMS) {
                com.example.strata.ui.settings.TermsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
