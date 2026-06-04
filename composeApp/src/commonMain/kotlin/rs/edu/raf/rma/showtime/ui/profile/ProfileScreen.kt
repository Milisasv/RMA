package rs.edu.raf.rma.showtime.ui.profile

import kotlin.math.roundToInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun ProfileScreen(viewModel: ProfileViewModel) {
    val state by viewModel.state.collectAsState()
    ProfileScreen(state = state, eventPublisher = viewModel::setEvent)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileScreen(
    state: ProfileContract.UiState,
    eventPublisher: (ProfileContract.UiEvent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                actions = {
                    IconButton(onClick = { eventPublisher(ProfileContract.UiEvent.Refresh) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            }
            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(state.profile?.fullName ?: "Unknown user", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("@${state.profile?.username ?: ""}")
                    if (state.isOffline) Text("Offline data", color = MaterialTheme.colorScheme.tertiary)
                    if (state.error != null) Text(state.error, color = MaterialTheme.colorScheme.error)
                }
            }
            ProfileStat(title = "Favorite movies", value = state.favoriteCount.toString())
            ProfileStat(title = "Watchlist movies", value = state.watchlistCount.toString())
            ProfileStat(title = "Best quiz score", value = state.quizStats.bestScore.formatScore())
            ProfileStat(title = "Played quizzes", value = state.quizStats.playedCount.toString())
            ProfileStat(title = "Last score", value = state.quizStats.lastScore.formatScore())
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { eventPublisher(ProfileContract.UiEvent.Logout) },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Logout") }
        }
    }
}

@Composable
private fun ProfileStat(title: String, value: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

private fun Double.formatScore(): String = (this * 100).roundToInt().let { cents ->
    val whole = cents / 100
    val decimal = (cents % 100).toString().padStart(2, '0')
    "$whole.$decimal"
}
