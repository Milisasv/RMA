package rs.edu.raf.rma.showtime.ui.movies

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import rs.edu.raf.rma.showtime.domain.Movie
import rs.edu.raf.rma.showtime.domain.MovieSort

@Composable
fun MoviesListScreen(
    viewModel: MoviesListViewModel,
    onMovieClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    MoviesListScreen(
        state = state,
        onMovieClick = onMovieClick,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoviesListScreen(
    state: MoviesListContract.UiState,
    onMovieClick: (String) -> Unit,
    eventPublisher: (MoviesListContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Movies") },
                actions = {
                    IconButton(onClick = { eventPublisher(MoviesListContract.UiEvent.Logout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                    IconButton(onClick = { eventPublisher(MoviesListContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
        ) {
            FiltersPanel(state = state, eventPublisher = eventPublisher)
            if (state.isOffline) {
                Text("Offline data", color = MaterialTheme.colorScheme.tertiary)
            }
            when {
                state.isLoading -> CenterBox { CircularProgressIndicator() }
                state.error != null && state.movies.isEmpty() -> CenterBox {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { eventPublisher(MoviesListContract.UiEvent.Refresh) }) { Text("Retry") }
                    }
                }
                state.movies.isEmpty() -> CenterBox { Text("No movies for selected filters.") }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.movies, key = { it.id }) { movie ->
                        MovieRow(movie = movie, onClick = { onMovieClick(movie.id) })
                    }
                    item {
                        OutlinedButton(
                            onClick = { eventPublisher(MoviesListContract.UiEvent.LoadNextPage) },
                            enabled = !state.isRefreshing,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        ) { Text(if (state.isRefreshing) "Loading..." else "Load more") }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersPanel(
    state: MoviesListContract.UiState,
    eventPublisher: (MoviesListContract.UiEvent) -> Unit,
) {
    OutlinedTextField(
        value = state.filters.search,
        onValueChange = { eventPublisher(MoviesListContract.UiEvent.SearchChanged(it)) },
        label = { Text("Search") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.filters.genre,
            onValueChange = { eventPublisher(MoviesListContract.UiEvent.GenreChanged(it)) },
            label = { Text("Genre id/name") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = state.filters.minRating,
            onValueChange = { eventPublisher(MoviesListContract.UiEvent.MinRatingChanged(it)) },
            label = { Text("Min rating") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.filters.yearFrom,
            onValueChange = { eventPublisher(MoviesListContract.UiEvent.YearFromChanged(it)) },
            label = { Text("Year from") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        OutlinedTextField(
            value = state.filters.yearTo,
            onValueChange = { eventPublisher(MoviesListContract.UiEvent.YearToChanged(it)) },
            label = { Text("Year to") },
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(Modifier.height(6.dp))
    OutlinedButton(
        onClick = {
            val next = MovieSort.entries[(MovieSort.entries.indexOf(state.filters.sort) + 1) % MovieSort.entries.size]
            eventPublisher(MoviesListContract.UiEvent.SortChanged(next))
        },
        modifier = Modifier.fillMaxWidth(),
    ) { Text("Sort: ${state.filters.sort.title}") }
    Spacer(Modifier.height(8.dp))
}

@Composable
fun MovieRow(movie: Movie, onClick: () -> Unit, trailing: @Composable (() -> Unit)? = null) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = movie.posterUrl ?: movie.backdropUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 70.dp, height = 105.dp),
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 10.dp),
            ) {
                Text(movie.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(listOfNotNull(movie.year?.toString(), movie.rating?.let { "★ ${it}" }).joinToString("  "))
                Text(movie.genres.joinToString(", "), maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(movie.overview, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun CenterBox(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { content() }
}
