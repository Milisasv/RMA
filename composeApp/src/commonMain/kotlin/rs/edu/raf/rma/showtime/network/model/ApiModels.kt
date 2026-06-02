package rs.edu.raf.rma.showtime.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginBody(
    val username: String,
    val password: String,
)

@Serializable
data class SignupBody(
    @SerialName("full_name") val fullName: String,
    val username: String,
    val password: String,
)

@Serializable
data class AuthResponse(
    @SerialName("access_token") val accessToken: String = "",
    @SerialName("expires_in") val expiresIn: Long? = null,
    val user: UserApiModel? = null,
)

@Serializable
data class UserApiModel(
    val id: Int? = null,
    val username: String = "",
    @SerialName("full_name") val fullName: String = "",
)

@Serializable
data class MoviePageResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<MovieApiModel> = emptyList(),
)

@Serializable
data class MovieApiModel(
    val imdbId: String,
    val tmdbId: Int? = null,
    val title: String = "",
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val imdbRating: Double? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Double? = null,
    val tmdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val genres: List<GenreApiModel> = emptyList(),
)

@Serializable
data class GenreApiModel(
    val id: Int,
    val name: String,
)

@Serializable
data class CastPageResponse(
    val page: Int = 1,
    val pageSize: Int = 20,
    val totalItems: Int = 0,
    val totalPages: Int = 0,
    val items: List<CastApiModel> = emptyList(),
)

@Serializable
data class CastApiModel(
    val imdbId: String? = null,
    val name: String = "",
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null,
)

@Serializable
data class QuizSubmitBody(
    val score: Double,
    val category: Int = 1,
)

@Serializable
data class QuizSubmitResponse(
    val ranking: Int? = null,
)
