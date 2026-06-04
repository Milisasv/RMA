package rs.edu.raf.rma.showtime

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.showtime.ui.auth.AuthScreen
import rs.edu.raf.rma.showtime.ui.auth.AuthViewModel
import rs.edu.raf.rma.showtime.ui.movies.MovieDetailScreen
import rs.edu.raf.rma.showtime.ui.movies.MovieDetailViewModel
import rs.edu.raf.rma.showtime.ui.movies.MoviesListScreen
import rs.edu.raf.rma.showtime.ui.movies.MoviesListViewModel
import rs.edu.raf.rma.showtime.ui.profile.ProfileScreen
import rs.edu.raf.rma.showtime.ui.profile.ProfileViewModel
import rs.edu.raf.rma.showtime.ui.quiz.QuizScreen
import rs.edu.raf.rma.showtime.ui.quiz.QuizViewModel
import rs.edu.raf.rma.showtime.ui.userlist.FavoritesViewModel
import rs.edu.raf.rma.showtime.ui.userlist.UserListScreen
import rs.edu.raf.rma.showtime.ui.userlist.WatchlistViewModel

private const val MOVIE_DETAIL_ROUTE = "movie"

@Composable
fun ShowtimeApp() {
    val authStore = koinInject<AuthStore>()
    val authState by authStore.authState.collectAsState()

    MaterialTheme {
        when (authState) {
            is AuthState.Authenticated -> MainNavigation()
            AuthState.Unauthenticated -> {
                val viewModel = koinViewModel<AuthViewModel>()
                AuthScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun MainNavigation() {
    val navController = rememberNavController()
    var selectedMovieId by rememberSaveable { mutableStateOf("") }

    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route.orEmpty()
    val tabs = MainTab.entries
    val showBottomBar = tabs.any { it.route == currentRoute }

    val openMovie: (String) -> Unit = { movieId ->
        selectedMovieId = movieId
        navController.navigate(MOVIE_DETAIL_ROUTE)
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = currentRoute == tab.route,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title) },
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = MainTab.Movies.route,
            modifier = Modifier.padding(padding),
        ) {
            mainGraph(
                navController = navController,
                selectedMovieIdProvider = { selectedMovieId },
                onMovieClick = openMovie,
            )
        }
    }
}

private fun NavGraphBuilder.mainGraph(
    navController: NavController,
    selectedMovieIdProvider: () -> String,
    onMovieClick: (String) -> Unit,
) {
    composable(MainTab.Movies.route) {
        val viewModel = koinViewModel<MoviesListViewModel>()
        MoviesListScreen(viewModel = viewModel, onMovieClick = onMovieClick)
    }
    composable(MainTab.Favorite.route) {
        val viewModel = koinViewModel<FavoritesViewModel>()
        UserListScreen(viewModel = viewModel, onMovieClick = onMovieClick)
    }
    composable(MainTab.Watchlist.route) {
        val viewModel = koinViewModel<WatchlistViewModel>()
        UserListScreen(viewModel = viewModel, onMovieClick = onMovieClick)
    }
    composable(MainTab.Quiz.route) {
        val viewModel = koinViewModel<QuizViewModel>()
        QuizScreen(viewModel = viewModel, onExit = { navController.navigateToTab(MainTab.Movies.route) })
    }
    composable(MainTab.Profile.route) {
        val viewModel = koinViewModel<ProfileViewModel>()
        ProfileScreen(viewModel = viewModel)
    }
    composable(MOVIE_DETAIL_ROUTE) {
        val movieId = selectedMovieIdProvider()
        val viewModel = koinViewModel<MovieDetailViewModel>(parameters = { parametersOf(movieId) })
        MovieDetailScreen(viewModel = viewModel, onBack = { navController.navigateUp() })
    }
}

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(MainTab.Movies.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

private enum class MainTab(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    Movies("movies", "Movies", Icons.Default.Movie),
    Favorite("favorite", "Favorite", Icons.Default.Favorite),
    Watchlist("watchlist", "Watchlist", Icons.Default.LiveTv),
    Quiz("quiz", "Quiz", Icons.Default.Quiz),
    Profile("profile", "Profile", Icons.Default.AccountCircle),
}
