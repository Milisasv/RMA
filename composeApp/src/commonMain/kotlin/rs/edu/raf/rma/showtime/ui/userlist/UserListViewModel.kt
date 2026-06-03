package rs.edu.raf.rma.showtime.ui.userlist

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.mvi.MviViewModel

open class UserListViewModel(
    private val type: UserListType,
    private val moviesRepository: MoviesRepository,
) : MviViewModel<UserListContract.UiState, UserListContract.UiEvent, UserListContract.SideEffect>(UserListContract.UiState(type = type)) {

    init {
        observeMovies()
        refresh()
    }

    override suspend fun handleEvent(event: UserListContract.UiEvent) {
        when (event) {
            UserListContract.UiEvent.Refresh -> refresh()
            is UserListContract.UiEvent.Remove -> remove(event.movieId)
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            val flow = when (type) {
                UserListType.Favorite -> moviesRepository.observeFavorites()
                UserListType.Watchlist -> moviesRepository.observeWatchlist()
            }
            flow.collect { movies -> setState { UserListContract.Reducer.reduce(this, movies) } }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { UserListContract.Reducer.loading(this.copy(error = null), movies.isEmpty()) }
            val result = when (type) {
                UserListType.Favorite -> runCatching { moviesRepository.syncFavorites() }
                UserListType.Watchlist -> runCatching { moviesRepository.syncWatchlist() }
            }
            result.onSuccess { setState { UserListContract.Reducer.offline(this, false) } }
                .onFailure { setState { UserListContract.Reducer.offline(this, movies.isNotEmpty(), it.message ?: "Unable to sync list.") } }
            setState { UserListContract.Reducer.loading(this, false) }
        }
    }

    private fun remove(movieId: String) {
        viewModelScope.launch {
            val result = when (type) {
                UserListType.Favorite -> runCatching { moviesRepository.removeFavorite(movieId) }
                UserListType.Watchlist -> runCatching { moviesRepository.removeWatchlist(movieId) }
            }
            result.onFailure {
                setState { copy(error = "Remove failed. Data is kept from local database.") }
                setEffect(UserListContract.SideEffect.Message("Remove failed."))
            }
        }
    }
}
