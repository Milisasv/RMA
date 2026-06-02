package rs.edu.raf.rma.showtime.data

import rs.edu.raf.rma.showtime.db.MovieEntity
import rs.edu.raf.rma.showtime.db.QuizStatsEntity
import rs.edu.raf.rma.showtime.domain.Movie
import rs.edu.raf.rma.showtime.domain.QuizStats
import rs.edu.raf.rma.showtime.domain.UserProfile
import rs.edu.raf.rma.showtime.network.model.CastApiModel
import rs.edu.raf.rma.showtime.network.model.MovieApiModel
import rs.edu.raf.rma.showtime.network.model.UserApiModel

private const val LIST_SEPARATOR = " • "

fun MovieEntity.toDomain(): Movie = Movie(
    id = id,
    title = title,
    overview = overview,
    year = year,
    runtime = runtime,
    posterUrl = posterUrl,
    backdropUrl = backdropUrl,
    genres = genresText.splitList(),
    actors = actorsText.splitList(),
    rating = rating,
    imdbRating = imdbRating,
    tmdbRating = tmdbRating,
    voteCount = voteCount,
    isFavorite = isFavorite,
    isWatchlisted = isWatchlisted,
)

fun MovieApiModel.toEntity(actors: List<String> = emptyList()): MovieEntity = MovieEntity(
    id = imdbId,
    title = title.ifBlank { originalTitle.orEmpty().ifBlank { imdbId } },
    overview = overview.orEmpty(),
    year = year ?: releaseDate?.take(4)?.toIntOrNull(),
    runtime = runtime,
    posterUrl = absoluteImageUrl(posterPath, size = "w500"),
    backdropUrl = absoluteImageUrl(backdropPath, size = "w780"),
    genresText = genres.map { it.name }.cleanJoin(),
    genreIdsText = genres.map { it.id.toString() }.cleanJoin(),
    actorsText = actors.cleanJoin(),
    rating = imdbRating ?: tmdbRating,
    imdbRating = imdbRating,
    tmdbRating = tmdbRating,
    voteCount = imdbVotes ?: tmdbVotes,
)

fun List<CastApiModel>.toActorNames(): List<String> =
    filter { it.department.equals("Acting", ignoreCase = true) || it.department == null }
        .map { it.name.trim() }
        .filter { it.isNotEmpty() }
        .distinct()

fun UserApiModel.toDomain(): UserProfile = UserProfile(
    fullName = fullName.ifBlank { username },
    username = username,
)

fun QuizStatsEntity.toDomain(): QuizStats = QuizStats(
    bestScore = bestScore,
    playedCount = playedCount,
    lastScore = lastScore,
    lastCorrect = lastCorrect,
    lastWrong = lastWrong,
)

private fun List<String>.cleanJoin(): String =
    map { it.trim() }.filter { it.isNotEmpty() }.distinct().joinToString(LIST_SEPARATOR)

private fun String.splitList(): List<String> =
    split(LIST_SEPARATOR).map { it.trim() }.filter { it.isNotEmpty() }

private fun absoluteImageUrl(value: String?, size: String): String? {
    val raw = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    return when {
        raw.startsWith("http://") || raw.startsWith("https://") -> raw
        raw.startsWith("/") -> "https://image.tmdb.org/t/p/$size$raw"
        else -> raw
    }
}
