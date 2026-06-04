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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
                title = { Text("ShowTimeRMA") },
                actions = {
                    IconButton(onClick = { eventPublisher(MoviesListContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { eventPublisher(MoviesListContract.UiEvent.Logout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilterPanel(state = state, eventPublisher = eventPublisher)

            if (state.isOffline) {
                Text("Offline data from local database", color = MaterialTheme.colorScheme.tertiary)
            }
            if (state.error != null) {
                Text(state.error, color = MaterialTheme.colorScheme.error)
            }

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.movies.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No movies found.")
                }

                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(state.movies, key = { it.id }) { movie ->
                        MovieRow(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                        )
                    }

                    item {
                        Button(
                            onClick = { eventPublisher(MoviesListContract.UiEvent.LoadNextPage) },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !state.isRefreshing,
                        ) {
                            if (state.isRefreshing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Load more")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterPanel(
    state: MoviesListContract.UiState,
    eventPublisher: (MoviesListContract.UiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = state.filters.search,
            onValueChange = { eventPublisher(MoviesListContract.UiEvent.SearchChanged(it)) },
            label = { Text("Search") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.filters.genre,
                onValueChange = { eventPublisher(MoviesListContract.UiEvent.GenreChanged(it)) },
                label = { Text("Genre id") },
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
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = state.filters.yearFrom,
                onValueChange = { eventPublisher(MoviesListContract.UiEvent.YearFromChanged(it)) },
                label = { Text("From year") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.filters.yearTo,
                onValueChange = { eventPublisher(MoviesListContract.UiEvent.YearToChanged(it)) },
                label = { Text("To year") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MovieSort.entries.forEach { sort ->
                FilterChip(
                    selected = state.filters.sort == sort,
                    onClick = { eventPublisher(MoviesListContract.UiEvent.SortChanged(sort)) },
                    label = { Text(sort.title) },
                )
            }
        }
    }
}

@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    trailing: @Composable (() -> Unit)? = null,
) {
    Surface(
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = movie.posterUrl ?: movie.backdropUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(width = 72.dp, height = 104.dp).clip(MaterialTheme.shapes.small),
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(movie.title, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    listOfNotNull(movie.year?.toString(), movie.runtime?.let { "$it min" }).joinToString("  •  ").ifBlank { "Movie" },
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    movie.genres.joinToString(", ").ifBlank { "Genres unavailable" },
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    movie.imdbRating?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("IMDb $it", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (movie.isFavorite) Icon(Icons.Default.Favorite, contentDescription = "Favorite", modifier = Modifier.size(16.dp))
                    if (movie.isWatchlisted) Icon(Icons.Default.PlaylistAdd, contentDescription = "Watchlist", modifier = Modifier.size(16.dp))
                }
            }
            if (trailing != null) {
                Spacer(Modifier.width(8.dp))
                trailing()
            }
        }
    }
}
