import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.R
import com.example.shoeshop.ui.components.DisableButton
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.ui.viewmodel.EmailVerificationViewModel
import com.example.shoeshop.ui.viewmodel.VerificationState
import com.example.shoeshop.ui.viewmodel.OtpType
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    onSignInClick: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: EmailVerificationViewModel = viewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var resendEnabled by remember { mutableStateOf(true) }
    var countdown by remember { mutableStateOf(0) }
    val context = LocalContext.current
    val verificationState by viewModel.verificationState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userEmail = getUserEmail(context)
    }

    // Таймер для повторной отправки
    LaunchedEffect(countdown) {
        if (countdown > 0) {
            delay(1000L)
            countdown--
        } else {
            resendEnabled = true
        }
    }

    LaunchedEffect(verificationState) {
        when (verificationState) {
            is VerificationState.Loading -> {
                // Можно показать индикатор загрузки
            }
            is VerificationState.Success -> {
                when ((verificationState as VerificationState.Success).type) {
                    OtpType.EMAIL -> {
                        onVerificationSuccess()
                        viewModel.resetState()
                    }
                    OtpType.RECOVERY -> {
                        // Не должно происходить в этом экране
                        Toast.makeText(
                            context,
                            "Неправильный тип OTP",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.resetState()
                    }
                }
            }
            is VerificationState.Error -> {
                val errorMessage = (verificationState as VerificationState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Заголовок
        Text(
            text = stringResource(id = R.string.verify_your_email),
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Информационное сообщение
        Text(
            text = stringResource(id = R.string.sent_verification_code),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Email пользователя
        if (userEmail.isNotEmpty()) {
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        } else {
            Text(
                text = stringResource(id = R.string.your_email),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 40.dp)
            )
        }

        // Поле для OTP кода
        Text(
            text = stringResource(id = R.string.enter_otp_code),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    otpCode = it

                    // Автоматическая проверка при вводе 6 цифр
                    if (it.length == 6 && userEmail.isNotEmpty()) {
                        viewModel.verifyEmailOtp(userEmail, otpCode)
                    }
                }
            },
            placeholder = { Text(stringResource(id = R.string.otp_placeholder)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface
            ),
            singleLine = true,
            isError = verificationState is VerificationState.Error,
            supportingText = {
                if (verificationState is VerificationState.Error) {
                    Text(
                        text = (verificationState as VerificationState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        )

        // Подсказка под полем OTP
        Text(
            text = stringResource(id = R.string.enter_6_digit_code),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Кнопка повторной отправки
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            if (countdown > 0) {
                Text(
                    text = stringResource(id = R.string.resend_in, countdown),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            } else {
                TextButton(
                    onClick = {
                        if (resendEnabled && userEmail.isNotEmpty()) {
                            // TODO: Реализовать повторную отправку email OTP
                            Toast.makeText(
                                context,
                                "Запрос на повторную отправку отправлен",
                                Toast.LENGTH_SHORT
                            ).show()
                            resendEnabled = false
                            countdown = 60 // 60 секунд до возможности повторной отправки
                        }
                    },
                    enabled = resendEnabled && userEmail.isNotEmpty()
                ) {
                    Text(
                        text = "Resend code",
                        color = if (resendEnabled && userEmail.isNotEmpty()) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Кнопка проверки OTP
        DisableButton(
            text = stringResource(id = R.string.verify),
            enabled = otpCode.length == 6 && userEmail.isNotEmpty(),
            onClick = {
                if (otpCode.length == 6 && userEmail.isNotEmpty()) {
                    viewModel.verifyEmailOtp(userEmail, otpCode) // Используем email метод
                }
            },
            textStyle = AppTypography.bodyMedium16
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Ссылка для возврата к входу
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = onSignInClick,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.outline)) {
                            append(stringResource(id = R.string.already_verified))
                        }
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                            append(stringResource(id = R.string.sign_in))
                        }
                    },
                    fontSize = 14.sp
                )
            }
        }
    }
}

fun getUserEmail(context: Context): String {
    val sharedPreferences = context.getSharedPreferences("shoe_shop_prefs", Context.MODE_PRIVATE)
    return sharedPreferences.getString("user_email", "") ?: ""
}