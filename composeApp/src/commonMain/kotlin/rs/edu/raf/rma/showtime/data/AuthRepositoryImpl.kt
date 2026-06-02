package rs.edu.raf.rma.showtime.data

import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.domain.UserProfile
import rs.edu.raf.rma.showtime.network.MoviesApi
import rs.edu.raf.rma.showtime.network.model.LoginBody
import rs.edu.raf.rma.showtime.network.model.SignupBody

class AuthRepositoryImpl(
    private val authStore: AuthStore,
    private val unauthenticatedApi: MoviesApi,
    private val authenticatedApi: MoviesApi,
    private val database: AppDatabase,
) : AuthRepository {

    override suspend fun login(username: String, password: String) {
        val response = unauthenticatedApi.login(LoginBody(username = username, password = password))
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                refreshToken = "",
            )
        )
    }

    override suspend fun signup(fullName: String, username: String, password: String) {
        val response = unauthenticatedApi.signup(
            SignupBody(
                fullName = fullName,
                username = username,
                password = password,
            )
        )
        authStore.setAuthData(
            AuthData(
                accessToken = response.accessToken,
                refreshToken = "",
            )
        )
    }

    override suspend fun loadProfile(): UserProfile = authenticatedApi.me().toDomain()

    override suspend fun logout() {
        authStore.clearAuthData()
        database.movieDao().clearUserLists()
    }
}
