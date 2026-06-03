package rs.edu.raf.rma.showtime.ui.userlist

import rs.edu.raf.rma.showtime.domain.MoviesRepository

class FavoritesViewModel(
    moviesRepository: MoviesRepository,
) : UserListViewModel(UserListType.Favorite, moviesRepository)
