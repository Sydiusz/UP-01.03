package com.example.shoeshop.ui.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.R
import com.example.shoeshop.ui.components.BackButton
import com.example.shoeshop.ui.components.DisableButton
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.theme.ShoeShopTheme
import com.example.shoeshop.ui.viewmodel.ChangePasswordState
import com.example.shoeshop.ui.viewmodel.SignInState
import com.example.shoeshop.ui.viewmodel.SignInViewModel

@Composable
fun CreateNewPasswordScreen(
    modifier: Modifier = Modifier,
    userToken: String? = null, // Добавьте параметр для токена
    onPasswordChanged: () -> Unit = {},
    onForgotPasswordClick: () -> Unit = {},
    onSignInClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {},
    viewModel: SignInViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Состояние для отслеживания ошибок валидации
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // Отслеживаем состояние смены пароля
    val changePasswordState by viewModel.changePasswordState.collectAsStateWithLifecycle()

    // AlertDialog для отображения результатов
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(changePasswordState) {
        when (changePasswordState) {
            is ChangePasswordState.Success -> {
                showSuccessDialog = true
                viewModel.resetChangePasswordState()
            }
            is ChangePasswordState.Error -> {
                errorMessage = (changePasswordState as ChangePasswordState.Error).message
                showErrorDialog = true
                viewModel.resetChangePasswordState()
            }
            else -> {}
        }
    }

    // Функция валидации пароля
    fun validatePasswords(): Boolean {
        var isValid = true

        // Валидация пароля
        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            isValid = false
        } else {
            passwordError = null
        }

        // Валидация подтверждения пароля
        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            isValid = false
        } else {
            confirmPasswordError = null
        }

        return isValid
    }

    // Диалог успеха
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onPasswordChanged() // Переходим на другой экран
            },
            title = { Text("Success") },
            text = { Text("Password changed successfully") },
            confirmButton = {
                Button(
                    onClick = {
                        showSuccessDialog = false
                        onPasswordChanged()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0560FA))
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Диалог ошибки
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                Button(
                    onClick = { showErrorDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0560FA))
                ) {
                    Text("OK")
                }
            }
        )
    }

    // Loading состояние
    if (changePasswordState is ChangePasswordState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(23.dp)
            .background(Color.White),
        verticalArrangement = Arrangement.Center,
    ) {
        BackButton(
            onClick = { /* Навигация назад */ }
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.set_new_password),
                style = AppTypography.headingRegular32,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = stringResource(id = R.string.set_password_description),
                style = AppTypography.subtitleRegular16,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 54.dp)
            )
        }

        // Поле "Пароль"
        Text(
            text = stringResource(id = R.string.pass),
            style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = null // Сбрасываем ошибку при изменении
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = if (passwordError != null) Color.Red else MaterialTheme.colorScheme.primary,
            ),
            textStyle = AppTypography.bodyRegular16,
            singleLine = true,
            isError = passwordError != null,
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.eye_close else R.drawable.eye_open
                        ),
                        contentDescription = if (passwordVisible) "Скрыть пароль" else "Показать пароль",
                    )
                }
            }
        )

        // Отображение ошибки для пароля
        passwordError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = AppTypography.bodyRegular14,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // Поле "Подтверждение пароля"
        Text(
            text = stringResource(id = R.string.confirm_password),
            style = AppTypography.bodyMedium16.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp, top = 20.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                confirmPasswordError = null // Сбрасываем ошибку при изменении
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = if (confirmPasswordError != null) Color.Red else MaterialTheme.colorScheme.primary,
            ),
            textStyle = AppTypography.bodyRegular16,
            singleLine = true,
            isError = confirmPasswordError != null,
            trailingIcon = {
                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (confirmPasswordVisible) R.drawable.eye_close else R.drawable.eye_open
                        ),
                        contentDescription = if (confirmPasswordVisible) "Скрыть пароль" else "Показать пароль",
                    )
                }
            }
        )

        // Отображение ошибки для подтверждения пароля
        confirmPasswordError?.let {
            Text(
                text = it,
                color = Color.Red,
                style = AppTypography.bodyRegular14,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Кнопка сохранения
        DisableButton(
            text = stringResource(id = R.string.save_now),
            enabled = password.isNotEmpty() && confirmPassword.isNotEmpty(),
            onClick = {
                if (validatePasswords() && userToken != null) {
                    viewModel.changePassword(userToken, password)
                } else if (userToken == null) {
                    errorMessage = "Authentication token is missing"
                    showErrorDialog = true
                }
            },
            textStyle = AppTypography.bodyMedium16
        )

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateNewPasswordScreenPreview() {
    ShoeShopTheme {
        CreateNewPasswordScreen()
    }
}