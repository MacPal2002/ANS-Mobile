package com.example.test1.ui.auth.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.test1.ui.component.AcademyTitle
import com.example.test1.ui.component.NotificationBanner
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Pobieramy stan z ViewModelu i obserwujemy jego zmiany
    val uiState by loginViewModel.uiState.collectAsState()


    // Krok 1: Dodajemy stan do kontrolowania widoczności banera
    var showSuccessBanner by remember { mutableStateOf(false) }
    val showLoadingOverlay = uiState.status == LoginStatus.LOADING || showSuccessBanner

    LaunchedEffect(uiState.status) {
        if (uiState.status == LoginStatus.SUCCESS && !showSuccessBanner) {
            showSuccessBanner = true
            delay(3000L)
            onLoginSuccess()
            showSuccessBanner = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                AcademyTitle(fontSize = 34.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Zaloguj się, aby kontynuować",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(48.dp))

                OutlinedTextField(
                    value = uiState.albumNumber,
                    enabled = (uiState.status != LoginStatus.LOADING),
                    onValueChange = { loginViewModel.onAlbumNumberChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Numer albumu", style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    isError = uiState.albumNumberError != null,
                    supportingText = {
                        // Pokaż tekst błędu pod polem
                        uiState.albumNumberError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = uiState.password,
                    enabled = (uiState.status != LoginStatus.LOADING),
                    onValueChange = { loginViewModel.onPasswordChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Hasło", style = MaterialTheme.typography.labelLarge) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    isError = uiState.passwordError != null,
                    supportingText = {
                        uiState.passwordError?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                )
                // --- NOWA SEKCJA DLA BŁĘDÓW OGÓLNYCH ---
                uiState.genericError?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { loginViewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = (uiState.status == LoginStatus.IDLE),
                ) {
                    Text(
                        text = "Zaloguj się",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                val annotatedText = buildAnnotatedString {
                    withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    ) {
                        append("Nie masz konta? ")
                    }
                    pushStringAnnotation("REGISTER", "register")
                    withStyle(style = MaterialTheme.typography.labelMedium.toSpanStyle().copy(
                        color = MaterialTheme.colorScheme.primary)
                    ) {
                        append("Zarejestruj się")
                    }
                    pop()
                }

                var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

                Text(
                    text = annotatedText,
                    onTextLayout = { layoutResult = it },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures { offsetPosition ->
                            layoutResult?.let { layout ->
                                val offset = layout.getOffsetForPosition(offsetPosition)
                                annotatedText.getStringAnnotations("REGISTER", offset, offset)
                                    .firstOrNull()?.let {
                                        onNavigateToRegister()
                                    }
                            }
                        }
                    }
                )
            }
        }
        AnimatedVisibility(
            visible = showSuccessBanner,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .zIndex(1f)
        ) {
            NotificationBanner(message = "Logowanie udane!")
        }
        if (showLoadingOverlay) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
                    .pointerInput(Unit) {} // blokuje wszystkie dotyki
            )

        }
    }
}
