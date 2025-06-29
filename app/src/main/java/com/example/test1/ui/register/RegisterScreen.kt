package com.example.test1.ui.register

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.test1.ui.component.AcademyTitle
import com.example.test1.ui.login.interFontFamily
import com.example.test1.ui.login.primaryColor
import com.example.test1.ui.login.subtextColor
import com.example.test1.ui.settings.secondaryColor

@Preview(showBackground = true)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {}
) {

    var currentStep by remember { mutableIntStateOf(1) }
    var albumNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var externalPassword by remember { mutableStateOf("") }

    val context = LocalContext.current
    val activity = LocalActivity.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()), // Dodajemy przewijanie dla mniejszych ekranów
            verticalArrangement = Arrangement.Center, // To centruje całą zawartość w pionie
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            AcademyTitle(fontSize = 34.sp)
            Spacer(modifier = Modifier.height(24.dp))
            ProgressIndicator(totalSteps = 2, currentStep = currentStep)
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }, label = "StepAnimation"
            ) { step ->
                when (step) {
                    1 -> Step1Form(
                        albumNumber, { albumNumber = it },
                        password, { password = it },
                        confirmPassword, { confirmPassword = it },
                        onNextClicked = { currentStep = 2 }
                    )
                    2 -> Step2Form(
                        externalPassword, { externalPassword = it },
                        onRegisterClicked = { /* Finalna logika rejestracji */ },
                        onBackClicked = { currentStep = 1 }
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            val annotatedText = buildAnnotatedString {
                append("Masz już konto? ")
                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                withStyle(style = SpanStyle(color = primaryColor, fontWeight = FontWeight.Bold, fontFamily = interFontFamily)) {
                    append("Zaloguj się")
                }
                pop()
            }
            ClickableText(
                text = annotatedText,
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onNavigateToLogin()
                        }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun Step1Form(
    albumNumber: String, onAlbumNumberChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    confirmPassword: String, onConfirmPasswordChange: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val passwordsMatch = password.isNotEmpty() && password == confirmPassword
    val isFormValid = albumNumber.isNotBlank() && passwordsMatch

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Utwórz konto w aplikacji", style = MaterialTheme.typography.titleMedium, color = subtextColor)
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = albumNumber,
            onValueChange = onAlbumNumberChange,
            label = { Text("Numer albumu", fontFamily = interFontFamily) },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Hasło", fontFamily = interFontFamily) }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Potwierdź hasło", fontFamily = interFontFamily) },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, null) }
            },
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = { if (confirmPassword.isNotEmpty() && !passwordsMatch) { Text("Hasła nie są zgodne", color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNextClicked, enabled = isFormValid, modifier = Modifier.fillMaxWidth().height(50.dp),shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = primaryColor)) {
            Text("Dalej", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun Step2Form(
    verbisPass: String, onExternalPassChange: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Utwórz konto w aplikacji", style = MaterialTheme.typography.titleMedium, color = subtextColor)
        Spacer(modifier = Modifier.height(24.dp))

        InfoBox(
            text = "Twoje hasło do systemu Verbis nie jest nigdzie zapisywane. Zostanie użyte tylko raz, aby bezpiecznie pobrać odpowiednie dane i zweryfikować numer albumu."
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = verbisPass,
            onValueChange = onExternalPassChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hasło do systemu Verbis", fontFamily = interFontFamily) },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primaryColor,
                focusedLabelColor = primaryColor,
                cursorColor = primaryColor
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRegisterClicked,
            enabled = verbisPass.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
        ) {
            Text("Zarejestruj się i pobierz dane", fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
        }
        TextButton(onClick = onBackClicked) {
            Text("Wróć", color = subtextColor)
        }
    }
}

@Composable
fun ProgressIndicator(totalSteps: Int, currentStep: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        for (step in 1..totalSteps) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (step == currentStep) primaryColor else secondaryColor)
            )
        }
    }
}

@Composable
fun InfoBox(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(secondaryColor)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = primaryColor)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = subtextColor)
    }
}
