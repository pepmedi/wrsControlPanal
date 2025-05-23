package login.presentation

import PrimaryAppColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dokar.sonner.TextToastAction
import com.dokar.sonner.ToastType
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import wrscontrolpanel.composeapp.generated.resources.Res
import wrscontrolpanel.composeapp.generated.resources.visibility
import wrscontrolpanel.composeapp.generated.resources.visibilityoff

@Composable
fun LoginScreen(
    loginViewModel: LoginViewModel = koinViewModel(),
    onLogin: (Boolean) -> Unit
) {

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var isPasswordVisible by remember { mutableStateOf(false) }

    val toaster = rememberToasterState()

    LaunchedEffect(uiState.user?.id) {
        if (uiState.loginSuccess && uiState.user != null) {
            loginViewModel.resetLoginUiState()
            toaster.show(
                message = "Login Successfully",
                type = ToastType.Success,
                action = TextToastAction(
                    text = "Done",
                    onClick = {
                        toaster.dismissAll()
                    }
                )
            )
            onLogin(true)
        }
    }

    LaunchedEffect(Unit) {
        loginViewModel.resetLoginUiState()
    }

    LaunchedEffect(uiState.errorMessage.id) {
        if (uiState.errorMessage.message.isNotEmpty()) {
            toaster.show(
                message = uiState.errorMessage.message,
                type = ToastType.Error,
                action = TextToastAction(
                    text = "Done",
                    onClick = { toaster.dismissAll() }
                )
            )
        }
    }

    // Main Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PrimaryAppColor),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = 8.dp,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .width(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Welcome Back!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryAppColor
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Username Field
                OutlinedTextField(
                    value = uiState.username,
                    onValueChange = { loginViewModel.onUsernameChange(it) },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.Person,
                                contentDescription = "Username"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = { loginViewModel.onPasswordChange(it) },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                painter = painterResource(if (isPasswordVisible) Res.drawable.visibility else Res.drawable.visibilityoff),
                                contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = PrimaryAppColor)
                } else {
                    Button(
                        onClick = {
                            loginViewModel.login()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryAppColor)
                    ) {
                        Text(text = "Login", color = Color.White)
                    }
                }
            }
        }

        Toaster(
            state = toaster,
            richColors = true,
            alignment = Alignment.TopEnd
        )
    }
}