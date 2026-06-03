package rs.edu.raf.rma.showtime.ui.auth

interface AuthContract {
    data class UiState(
        val isSignup: Boolean = false,
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object SwitchMode : UiEvent()
        data class FullNameChanged(val value: String) : UiEvent()
        data class UsernameChanged(val value: String) : UiEvent()
        data class PasswordChanged(val value: String) : UiEvent()
        data object Submit : UiEvent()
    }

    sealed class SideEffect {
        data class Message(val text: String) : SideEffect()
    }

    object Reducer {
        fun reduce(state: UiState, event: UiEvent): UiState = when (event) {
            UiEvent.SwitchMode -> state.copy(isSignup = !state.isSignup, error = null)
            is UiEvent.FullNameChanged -> state.copy(fullName = event.value, error = null)
            is UiEvent.UsernameChanged -> state.copy(username = event.value, error = null)
            is UiEvent.PasswordChanged -> state.copy(password = event.value, error = null)
            UiEvent.Submit -> state
        }
    }
}
