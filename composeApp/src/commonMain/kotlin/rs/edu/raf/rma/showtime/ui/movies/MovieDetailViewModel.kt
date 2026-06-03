package rs.edu.raf.rma.showtime.ui.movies

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.mvi.MviViewModel

class MovieDetailViewModel(
    private val movieId: String,
    private val moviesRepository: MoviesRepository,
) : MviViewModel<MovieDetailContract.UiState, MovieDetailContract.UiEvent, MovieDetailContract.SideEffect>(MovieDetailContract.UiState()) {

    init {
        observeMovie()
        refresh()
    }

    override suspend fun handleEvent(event: MovieDetailContract.UiEvent) {
        when (event) {
            MovieDetailContract.UiEvent.Refresh -> refresh()
            MovieDetailContract.UiEvent.ToggleFavorite -> toggleFavorite()
            MovieDetailContract.UiEvent.ToggleWatchlist -> toggleWatchlist()
        }
    }

    private fun observeMovie() {
        viewModelScope.launch {
            moviesRepository.observeMovie(movieId).collect { movie ->
                setState { MovieDetailContract.Reducer.movieObserved(this, movie) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { MovieDetailContract.Reducer.loading(this.copy(error = null), movie == null) }
            runCatching { moviesRepository.syncMovie(movieId) }
                .onSuccess { setState { MovieDetailContract.Reducer.syncSuccess(this) } }
                .onFailure { error -> setState { MovieDetailContract.Reducer.syncFailed(this, error.message) } }
            setState { MovieDetailContract.Reducer.loading(this, false) }
        }
    }

    private fun toggleFavorite() {
        val movie = state.value.movie ?: return
        viewModelScope.launch {
            setState { MovieDetailContract.Reducer.favoriteLoading(this.copy(error = null), true) }
            runCatching { moviesRepository.toggleFavorite(movie) }
                .onFailure {
                    setState { copy(error = "Favorite change failed. Previous state restored.") }
                    setEffect(MovieDetailContract.SideEffect.Message("Favorite change failed."))
                }
            setState { MovieDetailContract.Reducer.favoriteLoading(this, false) }
        }
    }

    private fun toggleWatchlist() {
        val movie = state.value.movie ?: return
        viewModelScope.launch {
            setState { MovieDetailContract.Reducer.watchlistLoading(this.copy(error = null), true) }
            runCatching { moviesRepository.toggleWatchlist(movie) }
                .onFailure {
                    setState { copy(error = "Watchlist change failed. Previous state restored.") }
                    setEffect(MovieDetailContract.SideEffect.Message("Watchlist change failed."))
                }
            setState { MovieDetailContract.Reducer.watchlistLoading(this, false) }
        }
    }
}
