package com.example.test1.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test1.R
import com.example.test1.ui.component.AcademyTitle


val primaryColor = Color(0xFF212C5D)
val subtextColor = Color.Gray

val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val interFontFamily = FontFamily(
    Font(GoogleFont("Inter"), provider)
)


@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {

    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    // Pobieramy stan z ViewModelu i obserwujemy jego zmiany
    val uiState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current


    // Efekt uboczny: nawigacja po udanym logowaniu
    LaunchedEffect(key1 = uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            Toast.makeText(context, "Logowanie udane!", Toast.LENGTH_SHORT).show()
            onLoginSuccess()
        }
    }


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
                color = subtextColor
            )
            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = uiState.studentId,
                onValueChange = { loginViewModel.onStudentIdChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Numer albumu", fontFamily = interFontFamily) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                ),
                isError = uiState.studentIdError != null, // Zmień kolor pola na czerwony, jeśli jest błąd
                supportingText = {
                    // Pokaż tekst błędu pod polem
                    if (uiState.studentIdError != null) {
                        Text(
                            text = uiState.studentIdError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { loginViewModel.onPasswordChange(it) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Hasło", fontFamily = interFontFamily) },
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
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    cursorColor = primaryColor
                ),
                isError = uiState.passwordError != null,
                supportingText = {
                    if (uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
            )
            // --- NOWA SEKCJA DLA BŁĘDÓW OGÓLNYCH ---
            if (uiState.genericError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.genericError!!,
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
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                enabled = !uiState.isLoading
            ) {
                Text(
                    text = "Zaloguj się",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            val annotatedText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = subtextColor, fontFamily = interFontFamily)) {
                    append("Nie masz konta? ")
                }
                // Dodajemy adnotację, aby uczynić tekst klikalnym
                pushStringAnnotation(tag = "REGISTER", annotation = "register")
                withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontFamily = interFontFamily)) {
                    append("Zarejestruj się")
                }
                pop()
            }

            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "REGISTER", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToRegister()
                        }
                }
            )
        }
    }
}
