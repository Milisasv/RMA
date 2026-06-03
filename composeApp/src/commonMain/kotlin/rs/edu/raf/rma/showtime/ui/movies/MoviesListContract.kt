package rs.edu.raf.rma.showtime.ui.movies

import rs.edu.raf.rma.showtime.domain.Movie
import rs.edu.raf.rma.showtime.domain.MovieFilters
import rs.edu.raf.rma.showtime.domain.MovieSort

interface MoviesListContract {
    data class UiState(
        val filters: MovieFilters = MovieFilters(),
        val movies: List<Movie> = emptyList(),
        val isLoading: Boolean = false,
        val isRefreshing: Boolean = false,
        val isOffline: Boolean = false,
        val error: String? = null,
    )

    sealed class UiEvent {
        data object Refresh : UiEvent()
        data object Logout : UiEvent()
        data object LoadNextPage : UiEvent()
        data class SearchChanged(val value: String) : UiEvent()
        data class GenreChanged(val value: String) : UiEvent()
        data class YearFromChanged(val value: String) : UiEvent()
        data class YearToChanged(val value: String) : UiEvent()
        data class MinRatingChanged(val value: String) : UiEvent()
        data class SortChanged(val value: MovieSort) : UiEvent()
    }

    sealed class SideEffect {
        data class Message(val value: String) : SideEffect()
    }

    object Reducer {
        fun reduce(state: UiState, event: UiEvent): UiState = when (event) {
            is UiEvent.SearchChanged -> state.copy(filters = state.filters.copy(search = event.value, page = 1))
            is UiEvent.GenreChanged -> state.copy(filters = state.filters.copy(genre = event.value, page = 1))
            is UiEvent.YearFromChanged -> state.copy(filters = state.filters.copy(yearFrom = event.value, page = 1))
            is UiEvent.YearToChanged -> state.copy(filters = state.filters.copy(yearTo = event.value, page = 1))
            is UiEvent.MinRatingChanged -> state.copy(filters = state.filters.copy(minRating = event.value, page = 1))
            is UiEvent.SortChanged -> state.copy(filters = state.filters.copy(sort = event.value, page = 1))
            UiEvent.LoadNextPage -> state.copy(filters = state.filters.copy(page = state.filters.page + 1))
            UiEvent.Refresh -> state
            UiEvent.Logout -> state
        }
    }
}
