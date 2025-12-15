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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.shoeshop.ui.viewmodel.EmailVerificationViewModel
import kotlin.text.ifEmpty

@Composable
fun EmailVerificationScreen(
    onSignInClick: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: EmailVerificationViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var otpCode by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    val context = LocalContext.current
    val verificationState by viewModel.verificationState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        userEmail = getUserEmail(context)
    }

    LaunchedEffect(verificationState) {
        when (verificationState) {
            is com.example.shoeshop.ui.viewmodel.VerificationState.Success -> {
                onVerificationSuccess()
                viewModel.resetState()
            }
            is com.example.shoeshop.ui.viewmodel.VerificationState.Error -> {
                val errorMessage = (verificationState as com.example.shoeshop.ui.viewmodel.VerificationState.Error).message
                android.widget.Toast.makeText(context, errorMessage, android.widget.Toast.LENGTH_LONG).show()
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
            text = "Verify Your Email",
            style = MaterialTheme.typography.headlineLarge,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Информационное сообщение
        Text(
            text = "We sent a 6-digit verification code to:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Email пользователя
        Text(
            text = userEmail.ifEmpty { "your email" },
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF0560FA),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        // Поле для OTP кода
        Text(
            text = "Enter OTP Code",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = otpCode,
            onValueChange = {
                if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                    otpCode = it
                }
            },
            placeholder = { Text("123456") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFA7A7A7),
                focusedBorderColor = Color(0xFF0560FA)
            )
        )

        // Подсказка под полем OTP
        Text(
            text = "Enter the 6-digit code from your email",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA7A7A7),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Spacer(modifier = Modifier.height(80.dp))

        // Кнопка Verify
        Button(
            onClick = {
                if (otpCode.length == 6 && userEmail.isNotEmpty()) {
                    viewModel.verifyOtp(userEmail, otpCode)
                } else if (userEmail.isEmpty()) {
                    android.widget.Toast.makeText(context, "Email not found. Please sign up again.", android.widget.Toast.LENGTH_LONG).show()
                } else {
                    android.widget.Toast.makeText(context, "Please enter 6-digit OTP code", android.widget.Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.extraSmall,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF0560FA)
            ),
            enabled = otpCode.length == 6
        ) {
            Text("Verify Email", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(10.dp))

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
                        withStyle(style = SpanStyle(color = Color(0xFFA7A7A7))) {
                            append("Already verified? ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF0560FA))) {
                            append("Sign In")
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