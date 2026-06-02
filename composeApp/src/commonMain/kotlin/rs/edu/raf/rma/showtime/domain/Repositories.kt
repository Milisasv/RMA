package rs.edu.raf.rma.showtime.domain

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(username: String, password: String)
    suspend fun signup(fullName: String, username: String, password: String)
    suspend fun loadProfile(): UserProfile
    suspend fun logout()
}

interface MoviesRepository {
    fun observeMovies(filters: MovieFilters): Flow<List<Movie>>
    fun observeMovie(id: String): Flow<Movie?>
    fun observeFavorites(): Flow<List<Movie>>
    fun observeWatchlist(): Flow<List<Movie>>
    fun observeFavoriteCount(): Flow<Int>
    fun observeWatchlistCount(): Flow<Int>
    suspend fun syncMovies(filters: MovieFilters)
    suspend fun bootstrapCatalogForQuiz()
    suspend fun syncMovie(id: String)
    suspend fun syncFavorites()
    suspend fun syncWatchlist()
    suspend fun toggleFavorite(movie: Movie)
    suspend fun toggleWatchlist(movie: Movie)
    suspend fun removeFavorite(movieId: String)
    suspend fun removeWatchlist(movieId: String)
}

interface QuizRepository {
    fun observeStats(): Flow<QuizStats>
    suspend fun buildSession(): List<QuizQuestion>
    suspend fun saveResult(result: QuizResult)
}
