package rs.edu.raf.rma.showtime.ui.profile

import rs.edu.raf.rma.showtime.domain.QuizStats
import rs.edu.raf.rma.showtime.domain.UserProfile

interface ProfileContract {
    data class UiState(
        val profile: UserProfile? = null,
        val quizStats: QuizStats = QuizStats(),
        val favoriteCount: Int = 0,
        val watchlistCount: Int = 0,
        val isLoading: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data object Logout : UiEvent()
    }

    sealed class SideEffect

    object Reducer {
        fun reduceLocal(state: UiState, stats: QuizStats, favorites: Int, watchlist: Int): UiState =
            state.copy(quizStats = stats, favoriteCount = favorites, watchlistCount = watchlist)

        fun loading(state: UiState, value: Boolean): UiState = state.copy(isLoading = value)

        fun profileLoaded(state: UiState, profile: UserProfile): UiState =
            state.copy(profile = profile, isOffline = false, error = null)

        fun profileFailed(state: UiState, message: String): UiState =
            state.copy(isOffline = state.profile != null, error = message)
    }
}
