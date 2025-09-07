package com.example.test1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test1.ui.auth.AppAuthViewModel
import com.example.test1.ui.settings.SettingsScreen
import com.example.test1.ui.auth.register.RegisterScreen
import com.example.test1.ui.schedule.ScheduleScreen
import com.example.test1.ui.auth.login.LoginScreen
import com.example.test1.ui.settings.SettingsViewModel
import com.example.test1.ui.settings.groupSelection.GroupSelectionScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val appAuthViewModel: AppAuthViewModel = hiltViewModel()

    val isLoggedIn by appAuthViewModel.isLoggedIn.collectAsState()

    LaunchedEffect(isLoggedIn) {
        val currentRoute = navController.currentBackStackEntry?.destination?.route

        // Jeśli stan zmienił się na "wylogowany" i nie jesteśmy na ekranie logowania/rejestracji
        if (!isLoggedIn && currentRoute != "login" && currentRoute != "register") {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId) {
                    inclusive = true
                }
            }
        }
    }
    val startDestination = if (appAuthViewModel.isInitiallyLoggedIn) {
        "schedule"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // Definicja ekranu logowania
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("schedule") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("group_selection") {
            GroupSelectionScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Definicja ekranu rejestracji
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        // Definicja ekranu rejestracji
        composable("settings") { backStackEntry ->
            val navGraphBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val settingsViewModel: SettingsViewModel = hiltViewModel(navGraphBackStackEntry)
            SettingsScreen(
                settingsViewModel = settingsViewModel,
                onLogout = {
                    // Po wylogowaniu, wróć do ekranu logowania
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToGroupSelection = {
                    navController.navigate("group_selection")
                }
            )
        }

        composable("schedule") {
            ScheduleScreen(
                onNavigateToSettings = {
                    navController.navigate("settings") {
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}