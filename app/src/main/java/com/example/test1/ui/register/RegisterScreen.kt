package com.example.test1.ui.register

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.test1.ui.component.AcademyTitle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.test1.ui.component.NotificationBanner
import kotlinx.coroutines.delay


@Preview(showBackground = true)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    viewModel: RegisterViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showSuccessBanner by remember { mutableStateOf(false) }
    var showErrorBanner by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val showLoadingOverlay = uiState.status == RegisterStatus.LOADING || showSuccessBanner || showErrorBanner

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            errorMessage = error
            showErrorBanner = true
            delay(3000L)
            showErrorBanner = false
            viewModel.onErrorMessageShown()
        }
    }

    LaunchedEffect(uiState.status) {
        if (uiState.status == RegisterStatus.SUCCESS && !showSuccessBanner) {
            showSuccessBanner = true
            delay(3000L)
            onRegisterSuccess()
            showSuccessBanner = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            AcademyTitle(fontSize = 34.sp)
            Spacer(modifier = Modifier.height(24.dp))
            ProgressIndicator(totalSteps = 2, currentStep = uiState.currentStep)
            Spacer(modifier = Modifier.height(24.dp))

            AnimatedContent(
                targetState = uiState.currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                            slideOutHorizontally { width -> -width } + fadeOut()
                }, label = "StepAnimation"
            ) { step ->
                when (step) {
                    1 -> Step1Form(
                        state = uiState,
                        onAlbumNumberChange = viewModel::onAlbumNumberChange,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onNextClicked = viewModel::onNextStep
                    )
                    2 -> Step2Form(
                        state = uiState,
                        onExternalPassChange = viewModel::onVerbisPasswordChange,
                        onRegisterClicked = viewModel::onRegisterClicked,
                        onBackClicked = viewModel::onPreviousStep
                    )
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
            val annotatedText = buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                ) {
                    append("Masz już konto? ")
                }
                pushStringAnnotation("LOGIN", "login")
                withStyle(style = MaterialTheme.typography.labelMedium.toSpanStyle().copy(
                    color = MaterialTheme.colorScheme.primary)
                ) {
                    append("Zaloguj się")
                }
                pop()
            }
            ClickableText(
                text = annotatedText,
                onClick = {
                    if (uiState.status == RegisterStatus.IDLE || uiState.status == RegisterStatus.ERROR) {
                        annotatedText.getStringAnnotations("LOGIN", it, it)
                            .firstOrNull()?.let {
                                onNavigateToLogin()
                            }
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))
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
            NotificationBanner(message = "Rejestracja zakończona sukcesem!")
        }

        AnimatedVisibility(
            visible = showErrorBanner && errorMessage != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 56.dp)
                .zIndex(1f)
        ) {
            NotificationBanner(icon = Icons.Default.Block , message = errorMessage ?: "")
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

@Composable
fun Step1Form(
    state: RegisterState,
    onAlbumNumberChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }

    val passwordsMatch = state.password.isNotEmpty() && state.password == state.confirmPassword
    val areFieldsFilled = state.albumNumber.isNotBlank() && state.email.isNotBlank()

    // 2. Sprawdzamy, czy aplikacja nie jest w trakcie operacji w tle
    val isActionAllowed = state.status == RegisterStatus.IDLE || state.status == RegisterStatus.ERROR

    // 3. Łączymy warunki: przycisk jest aktywny, jeśli pola są wypełnione, hasła się zgadzają ORAZ aplikacja nie jest zajęta
    val isButtonEnabled = passwordsMatch && areFieldsFilled && isActionAllowed



    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Utwórz konto w aplikacji", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.albumNumber,
            onValueChange = onAlbumNumberChange,
            label = { Text("Numer albumu", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.albumNumberError != null,
            supportingText = {
                state.albumNumberError?.let{
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = state.email,
            onValueChange = onEmailChange,
            label = { Text("Email", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.emailError != null,
            supportingText = {
                state.emailError?.let{
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = state.password,
            onValueChange = onPasswordChange,
            label = { Text("Hasło", style = MaterialTheme.typography.labelLarge) }, leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = state.passwordError != null,
            supportingText = {
                state.passwordError?.let{
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Potwierdź hasło", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) { Icon(image, null) }
            },
            isError = state.confirmPasswordError != null,
            supportingText = {
                state.confirmPasswordError?.let{
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNextClicked, enabled = isButtonEnabled, modifier = Modifier.fillMaxWidth().height(50.dp),shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
            Text("Dalej", style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun Step2Form(
    state: RegisterState,
    onExternalPassChange: (String) -> Unit,
    onRegisterClicked: () -> Unit,
    onBackClicked: () -> Unit
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val isEnabled = state.status == RegisterStatus.IDLE || state.status == RegisterStatus.ERROR

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Weryfikacja w systemie uczelni", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Spacer(modifier = Modifier.height(24.dp))

        InfoBox(
            text = "Twoje hasło do systemu Verbis nie jest nigdzie zapisywane. Zostanie użyte tylko raz, aby bezpiecznie pobrać odpowiednie dane i zweryfikować numer albumu."
        )
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = state.verbisPassword,
            onValueChange = onExternalPassChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Hasło do systemu Verbis", style = MaterialTheme.typography.labelLarge) },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) { Icon(image, null) }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRegisterClicked,
            enabled = state.verbisPassword.isNotBlank() && isEnabled,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Zarejestruj się i pobierz dane",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center
            )
        }
        TextButton(onClick = onBackClicked, enabled = isEnabled) {
            Text("Wróć")
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
                    .background(if (step == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
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
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
}