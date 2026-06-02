package rs.edu.raf.rma.showtime.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.showtime.network.model.AuthResponse
import rs.edu.raf.rma.showtime.network.model.CastPageResponse
import rs.edu.raf.rma.showtime.network.model.LoginBody
import rs.edu.raf.rma.showtime.network.model.MovieApiModel
import rs.edu.raf.rma.showtime.network.model.MoviePageResponse
import rs.edu.raf.rma.showtime.network.model.QuizSubmitBody
import rs.edu.raf.rma.showtime.network.model.QuizSubmitResponse
import rs.edu.raf.rma.showtime.network.model.SignupBody
import rs.edu.raf.rma.showtime.network.model.UserApiModel

interface MoviesApi {

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): AuthResponse

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupBody): AuthResponse

    @GET("me")
    suspend fun me(): UserApiModel

    @GET("movies")
    suspend fun getMovies(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Double? = null,
        @Query("sort_by") sortBy: String? = null,
        @Query("sort_order") sortOrder: String? = null,
    ): MoviePageResponse

    @GET("movies/{id}")
    suspend fun getMovie(@Path("id") id: String): MovieApiModel

    @GET("movies/{id}/cast")
    suspend fun getMovieCast(
        @Path("id") id: String,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 12,
    ): CastPageResponse

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieApiModel>

    @POST("me/favorites/{id}")
    suspend fun addFavorite(@Path("id") id: String)

    @DELETE("me/favorites/{id}")
    suspend fun removeFavorite(@Path("id") id: String)

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieApiModel>

    @POST("me/watchlist/{id}")
    suspend fun addWatchlist(@Path("id") id: String)

    @DELETE("me/watchlist/{id}")
    suspend fun removeWatchlist(@Path("id") id: String)

    @POST("leaderboard")
    suspend fun submitQuizResult(@Body body: QuizSubmitBody): QuizSubmitResponse
}
