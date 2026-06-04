package rs.edu.raf.rma.networking.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.api.Send
import io.ktor.client.plugins.api.SetupRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.networking.HttpClientFactory
import rs.edu.raf.rma.showtime.network.MoviesApi
import rs.edu.raf.rma.showtime.network.createMoviesApi

private const val MOVIES_API_BASE_URL = "https://rma.finlab.rs/"

val networkingModule = module {

    single<HttpClient>(Qualifiers.Unauthenticated) {
        HttpClientFactory.createHttpClientWithDefaultConfig()
    }

    single<HttpClient>(Qualifiers.Authenticated) {
        val authStoreLazy: Lazy<AuthStore> = inject()
        val databaseLazy: Lazy<AppDatabase> = inject()
        HttpClientFactory.createHttpClientWithDefaultConfig {
            installAuthPlugin(authStoreLazy, databaseLazy)
        }
    }

    single<MoviesApi>(Qualifiers.Unauthenticated) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Unauthenticated))
            .baseUrl(MOVIES_API_BASE_URL)
            .build()
            .createMoviesApi()
    }

    single<MoviesApi>(Qualifiers.Authenticated) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl(MOVIES_API_BASE_URL)
            .build()
            .createMoviesApi()
    }
}

private fun HttpClientConfig<*>.installAuthPlugin(
    authStoreLazy: Lazy<AuthStore>,
    databaseLazy: Lazy<AppDatabase>,
) = install(createClientPlugin("ShowtimeAuthPlugin") {

    on(SetupRequest) { request ->
        val authStore = authStoreLazy.value
        val authData = runBlocking { authStore.currentAuthData() }
        if (!authData.accessToken.isNullOrBlank()) {
            request.header(
                key = HttpHeaders.Authorization,
                value = "Bearer ${authData.accessToken}",
            )
        }
    }

    on(Send) { request ->
        try {
            val call = proceed(request)
            if (call.response.status == HttpStatusCode.Unauthorized) {
                runBlocking {
                    authStoreLazy.value.clearAuthData()
                    databaseLazy.value.movieDao().clearUserLists()
                }
            }
            call
        } catch (exception: ResponseException) {
            if (exception.response.status == HttpStatusCode.Unauthorized) {
                runBlocking {
                    authStoreLazy.value.clearAuthData()
                    databaseLazy.value.movieDao().clearUserLists()
                }
            }
            throw exception
        }
    }
})
