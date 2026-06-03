package rs.edu.raf.rma.showtime.ui.movies

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.mvi.MviViewModel

@OptIn(ExperimentalCoroutinesApi::class)
class MoviesListViewModel(
    private val moviesRepository: MoviesRepository,
    private val authRepository: AuthRepository,
) : MviViewModel<MoviesListContract.UiState, MoviesListContract.UiEvent, MoviesListContract.SideEffect>(MoviesListContract.UiState()) {

    init {
        observeMovies()
        syncMovies(isInitial = true)
        bootstrapQuizCatalog()
    }

    override suspend fun handleEvent(event: MoviesListContract.UiEvent) {
        when (event) {
            MoviesListContract.UiEvent.Refresh -> syncMovies(isInitial = false)
            MoviesListContract.UiEvent.Logout -> authRepository.logout()
            else -> {
                setState { MoviesListContract.Reducer.reduce(this, event) }
                syncMovies(isInitial = false)
            }
        }
    }

    private fun observeMovies() {
        viewModelScope.launch {
            state
                .map { it.filters }
                .distinctUntilChanged()
                .flatMapLatest { filters -> moviesRepository.observeMovies(filters) }
                .collect { movies -> setState { copy(movies = movies) } }
        }
    }

    private fun bootstrapQuizCatalog() {
        viewModelScope.launch {
            runCatching { moviesRepository.bootstrapCatalogForQuiz() }
                .onFailure { throwable ->
                    if (state.value.movies.isEmpty()) {
                        setState { copy(error = throwable.message ?: "Quiz bootstrap failed.") }
                    }
                }
        }
    }

    private fun syncMovies(isInitial: Boolean) {
        viewModelScope.launch {
            if (isInitial && state.value.movies.isEmpty()) {
                setState { copy(isLoading = true, error = null) }
            } else {
                setState { copy(isRefreshing = true, error = null) }
            }
            runCatching { moviesRepository.syncMovies(state.value.filters) }
                .onSuccess { setState { copy(isOffline = false) } }
                .onFailure { throwable ->
                    setState { copy(isOffline = movies.isNotEmpty(), error = throwable.message ?: "Unable to sync movies.") }
                }
            setState { copy(isLoading = false, isRefreshing = false) }
        }
    }
}
