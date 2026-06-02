package rs.edu.raf.rma.showtime.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val overview: String,
    val year: Int?,
    val runtime: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genresText: String,
    val genreIdsText: String,
    val actorsText: String,
    val rating: Double?,
    val imdbRating: Double?,
    val tmdbRating: Double?,
    val voteCount: Int?,
    val isFavorite: Boolean = false,
    val isWatchlisted: Boolean = false,
)
