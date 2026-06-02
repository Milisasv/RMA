package rs.edu.raf.rma.showtime.domain

data class Movie(
    val id: String,
    val title: String,
    val overview: String,
    val year: Int?,
    val runtime: Int?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val genres: List<String>,
    val actors: List<String>,
    val rating: Double?,
    val imdbRating: Double?,
    val tmdbRating: Double?,
    val voteCount: Int?,
    val isFavorite: Boolean,
    val isWatchlisted: Boolean,
) {
    val hasImage: Boolean
        get() = !posterUrl.isNullOrBlank() || !backdropUrl.isNullOrBlank()
}
