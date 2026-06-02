package rs.edu.raf.rma.showtime.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.db.MovieEntity
import rs.edu.raf.rma.showtime.domain.Movie
import rs.edu.raf.rma.showtime.domain.MovieFilters
import rs.edu.raf.rma.showtime.domain.MovieSort
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.network.MoviesApi

class MoviesRepositoryImpl(
    private val database: AppDatabase,
    private val publicApi: MoviesApi,
    private val privateApi: MoviesApi,
) : MoviesRepository {

    private val movieDao = database.movieDao()

    override fun observeMovies(filters: MovieFilters): Flow<List<Movie>> =
        movieDao.observeMovies(
            search = filters.searchOrNull,
            genre = filters.genreOrNull,
            yearFrom = filters.yearFromInt,
            yearTo = filters.yearToInt,
            minRating = filters.minRatingDouble,
            sort = filters.sort.localName,
            limit = filters.page * filters.pageSize,
            offset = 0,
        ).map { rows -> rows.map { it.toDomain() } }

    override fun observeMovie(id: String): Flow<Movie?> =
        movieDao.observeMovie(id).map { it?.toDomain() }

    override fun observeFavorites(): Flow<List<Movie>> =
        movieDao.observeFavorites().map { rows -> rows.map { it.toDomain() } }

    override fun observeWatchlist(): Flow<List<Movie>> =
        movieDao.observeWatchlist().map { rows -> rows.map { it.toDomain() } }

    override fun observeFavoriteCount(): Flow<Int> = movieDao.observeFavoriteCount()

    override fun observeWatchlistCount(): Flow<Int> = movieDao.observeWatchlistCount()

    override suspend fun syncMovies(filters: MovieFilters) {
        val response = publicApi.getMovies(
            page = filters.page,
            pageSize = filters.pageSize,
            query = filters.searchOrNull,
            genreId = filters.genreIdInt,
            minYear = filters.yearFromInt,
            maxYear = filters.yearToInt,
            minRating = filters.minRatingDouble,
            sortBy = filters.sort.apiName,
            sortOrder = filters.sort.apiOrder,
        )
        upsertKeepingExistingData(response.items.map { it.toEntity() })
    }

    override suspend fun bootstrapCatalogForQuiz() {
        val movies = mutableListOf<MovieEntity>()
        for (page in 1..5) {
            val response = publicApi.getMovies(
                page = page,
                pageSize = 20,
                sortBy = MovieSort.RatingDesc.apiName,
                sortOrder = MovieSort.RatingDesc.apiOrder,
            )
            if (response.items.isEmpty()) break
            movies += response.items.map { it.toEntity() }
        }
        upsertKeepingExistingData(movies)

        movies.take(60).forEach { movie ->
            runCatching { syncMovie(movie.id) }
        }
    }

    override suspend fun syncMovie(id: String) {
        val details = publicApi.getMovie(id)
        val actors = runCatching { publicApi.getMovieCast(id, pageSize = 12).items.toActorNames() }
            .getOrDefault(emptyList())
        val incoming = details.toEntity(actors = actors)
        val local = movieDao.findMovie(incoming.id)
        movieDao.upsertMovie(incoming.mergeWithLocal(local))
    }

    override suspend fun syncFavorites() {
        val favorites = privateApi.getFavorites().map { it.toEntity().copy(isFavorite = true) }
        upsertKeepingExistingData(favorites)
        movieDao.clearFavorites()
        favorites.forEach { movieDao.setFavorite(it.id, true) }
    }

    override suspend fun syncWatchlist() {
        val watchlist = privateApi.getWatchlist().map { it.toEntity().copy(isWatchlisted = true) }
        upsertKeepingExistingData(watchlist)
        movieDao.clearWatchlist()
        watchlist.forEach { movieDao.setWatchlisted(it.id, true) }
    }

    override suspend fun toggleFavorite(movie: Movie) {
        val next = !movie.isFavorite
        movieDao.setFavorite(movie.id, next)
        runCatching {
            if (next) privateApi.addFavorite(movie.id) else privateApi.removeFavorite(movie.id)
        }.onFailure {
            movieDao.setFavorite(movie.id, movie.isFavorite)
            throw it
        }
    }

    override suspend fun toggleWatchlist(movie: Movie) {
        val next = !movie.isWatchlisted
        movieDao.setWatchlisted(movie.id, next)
        runCatching {
            if (next) privateApi.addWatchlist(movie.id) else privateApi.removeWatchlist(movie.id)
        }.onFailure {
            movieDao.setWatchlisted(movie.id, movie.isWatchlisted)
            throw it
        }
    }

    override suspend fun removeFavorite(movieId: String) {
        val old = movieDao.getMovie(movieId)?.isFavorite ?: true
        movieDao.setFavorite(movieId, false)
        runCatching { privateApi.removeFavorite(movieId) }
            .onFailure {
                movieDao.setFavorite(movieId, old)
                throw it
            }
    }

    override suspend fun removeWatchlist(movieId: String) {
        val old = movieDao.getMovie(movieId)?.isWatchlisted ?: true
        movieDao.setWatchlisted(movieId, false)
        runCatching { privateApi.removeWatchlist(movieId) }
            .onFailure {
                movieDao.setWatchlisted(movieId, old)
                throw it
            }
    }

    private suspend fun upsertKeepingExistingData(incoming: List<MovieEntity>) {
        val merged = incoming.map { movie -> movie.mergeWithLocal(movieDao.findMovie(movie.id)) }
        movieDao.upsertMovies(merged)
    }

    private fun MovieEntity.mergeWithLocal(local: MovieEntity?): MovieEntity = copy(
        overview = overview.ifBlank { local?.overview.orEmpty() },
        runtime = runtime ?: local?.runtime,
        backdropUrl = backdropUrl ?: local?.backdropUrl,
        actorsText = actorsText.ifBlank { local?.actorsText.orEmpty() },
        isFavorite = local?.isFavorite ?: isFavorite,
        isWatchlisted = local?.isWatchlisted ?: isWatchlisted,
    )
}
