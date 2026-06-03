package rs.edu.raf.rma.showtime.ui.userlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import rs.edu.raf.rma.showtime.ui.movies.MovieRow

@Composable
fun UserListScreen(
    viewModel: UserListViewModel,
    onMovieClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsState()
    UserListScreen(state = state, onMovieClick = onMovieClick, eventPublisher = viewModel::setEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UserListScreen(
    state: UserListContract.UiState,
    onMovieClick: (String) -> Unit,
    eventPublisher: (UserListContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.type.title) },
                actions = {
                    IconButton(onClick = { eventPublisher(UserListContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            if (state.isOffline) Text("Offline read-only data", color = MaterialTheme.colorScheme.tertiary)
            if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.movies.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No movies in ${state.type.title.lowercase()}.") }
                else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.movies, key = { it.id }) { movie ->
                        MovieRow(
                            movie = movie,
                            onClick = { onMovieClick(movie.id) },
                            trailing = {
                                OutlinedButton(onClick = { eventPublisher(UserListContract.UiEvent.Remove(movie.id)) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove")
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}
