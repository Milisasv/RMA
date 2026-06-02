package rs.edu.raf.rma.showtime.di

import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel
import rs.edu.raf.rma.networking.di.Qualifiers
import rs.edu.raf.rma.showtime.data.AuthRepositoryImpl
import rs.edu.raf.rma.showtime.data.MoviesRepositoryImpl
import rs.edu.raf.rma.showtime.data.QuizRepositoryImpl
import rs.edu.raf.rma.showtime.domain.AuthRepository
import rs.edu.raf.rma.showtime.domain.MoviesRepository
import rs.edu.raf.rma.showtime.domain.QuizRepository
import rs.edu.raf.rma.showtime.ui.auth.AuthViewModel
import rs.edu.raf.rma.showtime.ui.movies.MovieDetailViewModel
import rs.edu.raf.rma.showtime.ui.movies.MoviesListViewModel
import rs.edu.raf.rma.showtime.ui.profile.ProfileViewModel
import rs.edu.raf.rma.showtime.ui.quiz.QuizViewModel
import rs.edu.raf.rma.showtime.ui.userlist.FavoritesViewModel
import rs.edu.raf.rma.showtime.ui.userlist.WatchlistViewModel

val showtimeModule = module {
    single {
        AuthRepositoryImpl(
            authStore = get(),
            unauthenticatedApi = get(Qualifiers.Unauthenticated),
            authenticatedApi = get(Qualifiers.Authenticated),
            database = get(),
        )
    } bind AuthRepository::class

    single {
        MoviesRepositoryImpl(
            database = get(),
            publicApi = get(Qualifiers.Unauthenticated),
            privateApi = get(Qualifiers.Authenticated),
        )
    } bind MoviesRepository::class

    single {
        QuizRepositoryImpl(
            database = get(),
            privateApi = get(Qualifiers.Authenticated),
        )
    } bind QuizRepository::class

    viewModelOf(::AuthViewModel)
    viewModelOf(::MoviesListViewModel)
    viewModel { params ->
        MovieDetailViewModel(
            movieId = params.get(),
            moviesRepository = get(),
        )
    }
    viewModelOf(::FavoritesViewModel)
    viewModelOf(::WatchlistViewModel)
    viewModelOf(::QuizViewModel)
    viewModelOf(::ProfileViewModel)
}
