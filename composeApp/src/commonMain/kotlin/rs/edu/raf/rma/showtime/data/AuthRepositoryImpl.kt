package rs.edu.raf.rma.showtime.data

import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.db.MovieEntity
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.domain.UserProfile
import rs.edu.raf.rma.showtime.network.MoviesApi
import rs.edu.raf.rma.showtime.network.model.LoginBody
import rs.edu.raf.rma.showtime.network.model.SignupBody
import rs.edu.raf.rma.showtime.network.model.UserApiModel

class AuthRepositoryImpl(
    private val authStore: AuthStore,
    private val unauthenticatedApi: MoviesApi,
    private val authenticatedApi: MoviesApi,
    private val database: AppDatabase,
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val cleanUsername = username.trim()
        val response = unauthenticatedApi.login(LoginBody(username = cleanUsername, password = password))
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                refreshToken = "",
                userId = response.user?.id,
                username = response.user?.username?.takeIf { it.isNotBlank() } ?: cleanUsername,
            )
        )
        syncUserListsFromServer()
    }

    override suspend fun signup(fullName: String, username: String, password: String) {
        val cleanUsername = username.trim()
        val response = unauthenticatedApi.signup(
            SignupBody(
                fullName = fullName.trim(),
                username = cleanUsername,
                password = password,
            )
        )
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                refreshToken = "",
                userId = response.user?.id,
                username = response.user?.username?.takeIf { it.isNotBlank() } ?: cleanUsername,
            )
        )
        syncUserListsFromServer()
    }

    override suspend fun loadProfile(): UserProfile {
        val user = authenticatedApi.me()
        updateStoredUserIdentity(user)
        return user.toDomain()
    }

    override suspend fun logout() {
        authStore.clearAuthData()
        database.movieDao().clearUserLists()
    }

    private suspend fun updateStoredUserIdentity(user: UserApiModel) {
        val current = authStore.currentAuthData()
        val incomingUsername = user.username.takeIf { it.isNotBlank() }
        val incomingUserId = user.id

        if ((incomingUsername != null && incomingUsername != current.username) ||
            (incomingUserId != null && incomingUserId != current.userId)
        ) {
            authStore.setAuthData(
                current.copy(
                    username = incomingUsername ?: current.username,
                    userId = incomingUserId ?: current.userId,
                )
            )
        }
    }

    private suspend fun syncUserListsFromServer() {
        val movieDao = database.movieDao()
        movieDao.clearUserLists()

        runCatching {
            authenticatedApi.getFavorites()
                .map { it.toEntity().copy(isFavorite = true) }
                .forEach { movie -> upsertUserMovie(movie) }
        }

        runCatching {
            authenticatedApi.getWatchlist()
                .map { it.toEntity().copy(isWatchlisted = true) }
                .forEach { movie -> upsertUserMovie(movie) }
        }
    }

    private suspend fun upsertUserMovie(incoming: MovieEntity) {
        val movieDao = database.movieDao()
        val local = movieDao.findMovie(incoming.id)
        movieDao.upsertMovie(incoming.mergeWithLocal(local))
    }

    private fun MovieEntity.mergeWithLocal(local: MovieEntity?): MovieEntity = copy(
        overview = overview.ifBlank { local?.overview.orEmpty() },
        runtime = runtime ?: local?.runtime,
        backdropUrl = backdropUrl ?: local?.backdropUrl,
        actorsText = actorsText.ifBlank { local?.actorsText.orEmpty() },
        isFavorite = isFavorite || (local?.isFavorite ?: false),
        isWatchlisted = isWatchlisted || (local?.isWatchlisted ?: false),
    )
}
