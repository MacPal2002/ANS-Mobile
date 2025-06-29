package com.example.test1.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.test1.ui.settings.SettingsScreen
import com.example.test1.ui.register.RegisterScreen
import com.example.test1.ui.schedule.ScheduleScreen
import com.example.test1.ui.login.LoginScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val firebaseAuth = remember { Firebase.auth } // Pobieramy instancję auth

    // --- NOWA, KLUCZOWA SEKCJA ---
    // Ten efekt będzie nasłuchiwał na zmiany stanu zalogowania przez cały czas,
    // gdy AppNavigation jest aktywne na ekranie.
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

        // Dodajemy listener do Firebase Auth
        firebaseAuth.addAuthStateListener(authStateListener)

        // onDispose zostanie wywołane, gdy komponent zniknie z ekranu.
        // To jest BARDZO WAŻNE, aby uniknąć wycieków pamięci.
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
                    // Po udanym logowaniu, przejdź do ekranu głównego
                    // i wyczyść stos nawigacji, aby użytkownik nie mógł
                    // wrócić do ekranu logowania przyciskiem "wstecz".
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

        // Definicja ekranu rejestracji
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Definicja ekranu rejestracji
        composable("settings") {
            SettingsScreen(
                onLogout = {
                    // Po wylogowaniu, wróć do ekranu logowania
                    Firebase.auth.signOut()
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        // Definicja ekranu głównego (po zalogowaniu)
        composable("schedule") {
            ScheduleScreen(
                onNavigateToSettings = {
                    // Przejdź do ekranu ustawień
                    navController.navigate("settings")
                }
            )
        }
    }
}