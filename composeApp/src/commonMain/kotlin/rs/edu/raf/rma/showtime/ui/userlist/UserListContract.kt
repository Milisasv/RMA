package rs.edu.raf.rma.showtime.ui.userlist

import rs.edu.raf.rma.showtime.domain.Movie

interface UserListContract {
    data class UiState(
        val type: UserListType,
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data class Remove(val movieId: String) : UiEvent()
    }

    sealed class SideEffect {
        data class Message(val value: String) : SideEffect()
    }

    object Reducer {
        fun reduce(state: UiState, movies: List<Movie>): UiState = state.copy(movies = movies)
        fun loading(state: UiState, value: Boolean): UiState = state.copy(isLoading = value)
        fun offline(state: UiState, value: Boolean, message: String? = null): UiState =
            state.copy(isOffline = value, error = message)
    }
}
