package rs.edu.raf.rma.showtime.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state by viewModel.state.collectAsState()
    AuthScreen(
        state = state,
        eventPublisher = viewModel::setEvent,
    )
}

@Composable
private fun AuthScreen(
    state: AuthContract.UiState,
    eventPublisher: (AuthContract.UiEvent) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "ShowTimeRMA",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = if (state.isSignup) "Create account" else "Login to continue",
            style = MaterialTheme.typography.bodyLarge,
        )
        Spacer(Modifier.height(24.dp))

        if (state.isSignup) {
            OutlinedTextField(
                value = state.fullName,
                onValueChange = { eventPublisher(AuthContract.UiEvent.FullNameChanged(it)) },
                label = { Text("Full name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = state.username,
            onValueChange = { eventPublisher(AuthContract.UiEvent.UsernameChanged(it)) },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = state.password,
            onValueChange = { eventPublisher(AuthContract.UiEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        if (state.error != null) {
            Spacer(Modifier.height(12.dp))
            Text(text = state.error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { eventPublisher(AuthContract.UiEvent.Submit) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.isLoading) CircularProgressIndicator() else Text(if (state.isSignup) "Sign up" else "Login")
        }

        OutlinedButton(
            onClick = { eventPublisher(AuthContract.UiEvent.SwitchMode) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (state.isSignup) "Already have account? Login" else "Need account? Sign up")
        }
    }
}
