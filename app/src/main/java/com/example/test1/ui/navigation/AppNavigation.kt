package com.example.test1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test1.ui.settings.SettingsScreen
import com.example.test1.ui.auth.register.RegisterScreen
import com.example.test1.ui.schedule.ScheduleScreen
import com.example.test1.ui.auth.login.LoginScreen
import com.example.test1.ui.settings.groupSelection.GroupSelectionScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val firebaseAuth = remember { Firebase.auth }

    DisposableEffect(key1 = navController) {
        // Tworzymy listener
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            // Pobieramy aktualną ścieżkę w nawigacji
            val currentRoute = navController.currentBackStackEntry?.destination?.route

            // Jeśli użytkownik jest null (wylogowany) I nie jesteśmy już na ekranie logowania lub rejestracji
            if (auth.currentUser == null && currentRoute != "login" && currentRoute != "register") {
                navController.navigate("login") {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                }
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        onDispose {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }
    val startDestination = if (Firebase.auth.currentUser != null) {
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
        composable("settings") {
            SettingsScreen(
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