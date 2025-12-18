package com.example.shoeshop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.shoeshop.ui.theme.AppTypography
import com.example.shoeshop.R
import com.example.shoeshop.ui.components.DisableButton
import com.example.shoeshop.ui.viewmodel.ProfileViewModel
import com.example.shoeshop.data.SessionManager

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    var isEditing by remember { mutableStateOf(false) }

    val currentProfile = uiState.profile

    var name by remember(currentProfile?.firstname) {
        mutableStateOf(currentProfile?.firstname ?: "")
    }
    var lastName by remember(currentProfile?.lastname) {
        mutableStateOf(currentProfile?.lastname ?: "")
    }
    var address by remember(currentProfile?.address) {
        mutableStateOf(currentProfile?.address ?: "")
    }
    var phone by remember(currentProfile?.phone) {
        mutableStateOf(currentProfile?.phone ?: "")
    }

    val original = currentProfile
    val hasChanges by remember(name, lastName, address, phone, original) {
        derivedStateOf {
            if (original == null) {
                // Первый раз создаём профиль: включаем кнопку,
                // когда пользователь что‑то ввёл
                name.isNotBlank() ||
                        lastName.isNotBlank() ||
                        address.isNotBlank() ||
                        phone.isNotBlank()
            } else {
                // Профиль уже есть: сравниваем с тем, что пришло с сервера
                name != (original.firstname ?: "") ||
                        lastName != (original.lastname ?: "") ||
                        address != (original.address ?: "") ||
                        phone != (original.phone ?: "")
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        if (uiState.isLoading && currentProfile == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(40.dp))
                    Text(
                        text = stringResource(id = R.string.profile),
                        style = AppTypography.headingSemiBold16,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            if (isEditing && original != null) {
                                name = original.firstname ?: ""
                                lastName = original.lastname ?: ""
                                address = original.address ?: ""
                                phone = original.phone ?: ""
                            }
                            isEditing = !isEditing
                        },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.edit),
                            contentDescription = if (isEditing) stringResource(R.string.cancel) else "Изменить",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0E0E0))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = listOf(name, lastName).filter { it.isNotBlank() }.joinToString(" "),
                        style = AppTypography.bodyRegular20
                    )
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (isEditing) {
                        EditableField(
                            label = stringResource(id = R.string.your_name),
                            value = name,
                            onValueChange = { name = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EditableField(
                            label = stringResource(id = R.string.last_name),
                            value = lastName,
                            onValueChange = { lastName = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EditableField(
                            label = stringResource(id = R.string.address),
                            value = address,
                            onValueChange = { address = it }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        EditableField(
                            label = stringResource(id = R.string.phone_number),
                            value = phone,
                            onValueChange = { phone = it }
                        )
                    } else {
                        InputField(stringResource(id = R.string.your_name), name)
                        Spacer(modifier = Modifier.height(16.dp))
                        InputField(stringResource(id = R.string.last_name), lastName)
                        Spacer(modifier = Modifier.height(16.dp))
                        InputField(stringResource(id = R.string.address), address)
                        Spacer(modifier = Modifier.height(16.dp))
                        InputField(stringResource(id = R.string.phone_number), phone)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                if (isEditing) {
                    DisableButton(
                        text = stringResource(id = R.string.save_now),
                        onClick = {
                            viewModel.saveProfile(
                                firstname = name,
                                lastname = lastName,
                                address = address,
                                phone = phone
                            )
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = hasChanges
                    )
                }
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Подпись
        Text(
            text = label,
            style = AppTypography.bodyMedium16.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Поле (non-editable)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF5F5F5),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = if (value.isNotEmpty()) value else stringResource(R.string.not_specified),
                    style = AppTypography.bodyRegular16.copy(
                        color = if (value.isNotEmpty()) Color.Black else Color.Gray
                    )
                )
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Подпись
        Text(
            text = label,
            style = AppTypography.bodyMedium16.copy(
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Поле для редактирования
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = AppTypography.bodyRegular16,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
