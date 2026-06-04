package rs.edu.raf.rma.showtime.ui.profile

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.domain.QuizRepository
import rs.edu.raf.rma.showtime.mvi.MviViewModel

class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val moviesRepository: MoviesRepository,
    private val quizRepository: QuizRepository,
) : MviViewModel<ProfileContract.UiState, ProfileContract.UiEvent, ProfileContract.SideEffect>(ProfileContract.UiState()) {

    init {
        observeLocalData()
        refresh()
    }

    override suspend fun handleEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.Refresh -> refresh()
            ProfileContract.UiEvent.Logout -> authRepository.logout()
        }
    }

    private fun observeLocalData() {
        viewModelScope.launch {
            combine(
                quizRepository.observeStats(),
                moviesRepository.observeFavoriteCount(),
                moviesRepository.observeWatchlistCount(),
            ) { stats, favoriteCount, watchlistCount ->
                Triple(stats, favoriteCount, watchlistCount)
            }.collect { (stats, favorites, watchlist) ->
                setState { ProfileContract.Reducer.reduceLocal(this, stats, favorites, watchlist) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { ProfileContract.Reducer.loading(this.copy(error = null), profile == null) }
            runCatching {
                moviesRepository.syncFavorites()
                moviesRepository.syncWatchlist()
            }
            runCatching { authRepository.loadProfile() }
                .onSuccess { user -> setState { ProfileContract.Reducer.profileLoaded(this, user) } }
                .onFailure { throwable -> setState { ProfileContract.Reducer.profileFailed(this, throwable.message ?: "Profile sync failed.") } }
            setState { ProfileContract.Reducer.loading(this, false) }
        }
    }
}
