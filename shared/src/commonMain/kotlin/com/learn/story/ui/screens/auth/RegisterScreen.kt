package com.learn.story.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.learn.story.ui.components.AppButton
import com.learn.story.ui.components.AppTextField
import com.learn.story.ui.theme.StoryTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
            onNavigateToLogin()
        }
    }

    RegisterScreenContent(
        uiState = uiState,
        onNameChanged = viewModel::onNameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onRegister = { viewModel.register(onSuccess = onNavigateToLogin) },
        onNavigateToLogin = onNavigateToLogin,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
fun RegisterScreenContent(
    uiState: RegisterUiState,
    onNameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🚀",
                    fontSize = 54.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Buat Akun Baru",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Mulai berbagi cerita seru dengan sesama coder",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Registrasi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        AppTextField(
                            value = uiState.name,
                            onValueChange = onNameChanged,
                            label = "Nama Lengkap",
                            placeholder = "John Doe",
                            errorMessage = uiState.nameError
                        )

                        AppTextField(
                            value = uiState.email,
                            onValueChange = onEmailChanged,
                            label = "Email",
                            placeholder = "nama@email.com",
                            errorMessage = uiState.emailError
                        )

                        AppTextField(
                            value = uiState.password,
                            onValueChange = onPasswordChanged,
                            label = "Password",
                            placeholder = "Minimal 8 karakter",
                            isPassword = true,
                            errorMessage = uiState.passwordError
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        AppButton(
                            text = "Daftar",
                            isLoading = uiState.isLoading,
                            onClick = onRegister
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sudah punya akun? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Masuk di sini",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun RegisterScreenPreview() {
    StoryTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(
                name = "Dicoding",
                email = "dicoding@mail.com",
                password = "secret123"
            ),
            onNameChanged = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onRegister = {},
            onNavigateToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview
@Composable
private fun RegisterScreenErrorPreview() {
    StoryTheme {
        RegisterScreenContent(
            uiState = RegisterUiState(
                name = "",
                email = "invalid",
                password = "123",
                nameError = "Nama tidak boleh kosong",
                emailError = "Format email tidak valid",
                passwordError = "Password minimal 8 karakter"
            ),
            onNameChanged = {},
            onEmailChanged = {},
            onPasswordChanged = {},
            onRegister = {},
            onNavigateToLogin = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
