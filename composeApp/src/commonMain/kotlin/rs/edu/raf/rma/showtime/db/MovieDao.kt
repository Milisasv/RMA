package rs.edu.raf.rma.showtime.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query(
        """
        SELECT * FROM movies
        WHERE (:search IS NULL OR title LIKE '%' || :search || '%' OR overview LIKE '%' || :search || '%')
          AND (:genre IS NULL OR genresText LIKE '%' || :genre || '%' OR genreIdsText LIKE '%' || :genre || '%')
          AND (:yearFrom IS NULL OR year >= :yearFrom)
          AND (:yearTo IS NULL OR year <= :yearTo)
          AND (:minRating IS NULL OR rating >= :minRating)
        ORDER BY
          CASE WHEN :sort = 'title_asc' THEN title END COLLATE NOCASE ASC,
          CASE WHEN :sort = 'title_desc' THEN title END COLLATE NOCASE DESC,
          CASE WHEN :sort = 'year_asc' THEN year END ASC,
          CASE WHEN :sort = 'year_desc' THEN year END DESC,
          CASE WHEN :sort = 'rating_asc' THEN rating END ASC,
          CASE WHEN :sort = 'rating_desc' THEN rating END DESC,
          title COLLATE NOCASE ASC
        LIMIT :limit OFFSET :offset
        """
    )
    fun observeMovies(
        search: String?,
        genre: String?,
        yearFrom: Int?,
        yearTo: Int?,
        minRating: Double?,
        sort: String,
        limit: Int,
        offset: Int,
    ): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :id")
    fun observeMovie(id: String): Flow<MovieEntity?>

    @Query("SELECT * FROM movies WHERE id = :id")
    suspend fun getMovie(id: String): MovieEntity?

    @Query("SELECT * FROM movies WHERE (posterUrl IS NOT NULL OR backdropUrl IS NOT NULL) ORDER BY rating DESC LIMIT :limit")
    suspend fun getQuizPool(limit: Int = 120): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE")
    fun observeFavorites(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isWatchlisted = 1 ORDER BY title COLLATE NOCASE")
    fun observeWatchlist(): Flow<List<MovieEntity>>

    @Query("SELECT COUNT(*) FROM movies WHERE isFavorite = 1")
    fun observeFavoriteCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM movies WHERE isWatchlisted = 1")
    fun observeWatchlistCount(): Flow<Int>

    @Query("UPDATE movies SET isFavorite = :value WHERE id = :movieId")
    suspend fun setFavorite(movieId: String, value: Boolean)

    @Query("UPDATE movies SET isWatchlisted = :value WHERE id = :movieId")
    suspend fun setWatchlisted(movieId: String, value: Boolean)

    @Query("UPDATE movies SET isFavorite = 0")
    suspend fun clearFavorites()

    @Query("UPDATE movies SET isWatchlisted = 0")
    suspend fun clearWatchlist()

    @Query("UPDATE movies SET isFavorite = 0, isWatchlisted = 0")
    suspend fun clearUserLists()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMovie(movie: MovieEntity)

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun findMovie(movieId: String): MovieEntity?
}
