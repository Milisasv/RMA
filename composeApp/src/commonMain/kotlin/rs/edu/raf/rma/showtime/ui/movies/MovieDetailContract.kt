package rs.edu.raf.rma.showtime.ui.movies

import rs.edu.raf.rma.showtime.domain.Movie

interface MovieDetailContract {
    data class UiState(
        val movie: Movie? = null,
        val isLoading: Boolean = false,
        val isFavoriteLoading: Boolean = false,
        val isWatchlistLoading: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data object ToggleFavorite : UiEvent()
        data object ToggleWatchlist : UiEvent()
    }

    sealed class SideEffect {
        data class Message(val value: String) : SideEffect()
    }

    object Reducer {
        fun movieObserved(state: UiState, movie: Movie?): UiState = state.copy(movie = movie)
        fun loading(state: UiState, value: Boolean): UiState = state.copy(isLoading = value)
        fun favoriteLoading(state: UiState, value: Boolean): UiState = state.copy(isFavoriteLoading = value)
        fun watchlistLoading(state: UiState, value: Boolean): UiState = state.copy(isWatchlistLoading = value)
        fun syncSuccess(state: UiState): UiState = state.copy(isOffline = false, error = null)
        fun syncFailed(state: UiState, message: String?): UiState = state.copy(isOffline = state.movie != null, error = message)
    }
}
