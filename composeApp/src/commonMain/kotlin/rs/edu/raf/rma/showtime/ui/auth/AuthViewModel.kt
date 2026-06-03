package rs.edu.raf.rma.showtime.ui.auth

import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.mvi.MviViewModel

class AuthViewModel(
    private val authRepository: AuthRepository,
) : MviViewModel<AuthContract.UiState, AuthContract.UiEvent, AuthContract.SideEffect>(AuthContract.UiState()) {

    override suspend fun handleEvent(event: AuthContract.UiEvent) {
        when (event) {
            AuthContract.UiEvent.Submit -> submit()
            else -> setState { AuthContract.Reducer.reduce(this, event) }
        }
    }

    private suspend fun submit() {
        val current = state.value
        val validationError = validate(current)
        if (validationError != null) {
            setState { copy(error = validationError) }
            return
        }

        setState { copy(isLoading = true, error = null) }
        runCatching {
            if (current.isSignup) {
                authRepository.signup(
                    fullName = current.fullName.trim(),
                    username = current.username.trim(),
                    password = current.password,
                )
            } else {
                authRepository.login(
                    username = current.username.trim(),
                    password = current.password,
                )
            }
        }.onFailure { throwable ->
            setState { copy(error = errorMessage(throwable)) }
        }
        setState { copy(isLoading = false) }
    }

    private fun validate(state: AuthContract.UiState): String? {
        val usernameRegex = Regex("^[a-zA-Z0-9_]{3,}$")
        return when {
            state.isSignup && state.fullName.isBlank() -> "Full name is required."
            state.username.isBlank() -> "Username is required."
            !usernameRegex.matches(state.username.trim()) -> "Username must have at least 3 letters, digits or underscores."
            state.password.length < 8 -> "Password must have at least 8 characters."
            else -> null
        }
    }

    private fun errorMessage(throwable: Throwable): String {
        val response = throwable as? ResponseException
        return when (response?.response?.status) {
            HttpStatusCode.Unauthorized -> "Invalid username or password."
            HttpStatusCode.Conflict -> "Username is already taken."
            HttpStatusCode.BadRequest -> "Check the entered data and try again."
            null -> throwable.message?.takeIf { it.isNotBlank() } ?: "Network error. Check your connection and try again."
            else -> "Server error: ${response.response.status.value}. Try again later."
        }
    }
}
