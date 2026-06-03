package rs.edu.raf.rma.showtime.ui.movies

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MovieDetailScreen(
    viewModel: MovieDetailViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    MovieDetailScreen(
        state = state,
        onBack = onBack,
        eventPublisher = viewModel::setEvent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MovieDetailScreen(
    state: MovieDetailContract.UiState,
    onBack: () -> Unit,
    eventPublisher: (MovieDetailContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.movie?.title ?: "Movie detail") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = { eventPublisher(MovieDetailContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            state.movie == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { Text(state.error ?: "Movie not found.") }
            else -> {
                val movie = state.movie
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState()),
                ) {
                    AsyncImage(
                        model = movie.backdropUrl ?: movie.posterUrl,
                        contentDescription = movie.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(210.dp),
                    )
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(movie.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(listOfNotNull(movie.year?.toString(), movie.runtime?.let { "$it min" }).joinToString("  •  "))
                        Text(listOfNotNull(
                            movie.imdbRating?.let { "IMDb $it" },
                            movie.tmdbRating?.let { "TMDb $it" },
                            movie.voteCount?.let { "$it votes" },
                        ).joinToString("  •  ").ifBlank { "Ratings unavailable" })
                        Text(movie.genres.joinToString(", "))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { eventPublisher(MovieDetailContract.UiEvent.ToggleFavorite) },
                                enabled = !state.isFavoriteLoading,
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(if (movie.isFavorite) "Unfavorite" else "Favorite")
                            }
                            OutlinedButton(
                                onClick = { eventPublisher(MovieDetailContract.UiEvent.ToggleWatchlist) },
                                enabled = !state.isWatchlistLoading,
                            ) {
                                Icon(Icons.Default.PlaylistAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(if (movie.isWatchlisted) "Remove" else "Watchlist")
                            }
                        }
                        if (state.isOffline) Text("Offline data", color = MaterialTheme.colorScheme.tertiary)
                        if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)
                        Text("Overview", fontWeight = FontWeight.Bold)
                        Text(movie.overview.ifBlank { "No overview available." })
                        Text("Cast", fontWeight = FontWeight.Bold)
                        Text(movie.actors.take(10).joinToString(", ").ifBlank { "No cast information." })
                    }
                }
            }
        }
    }
}
