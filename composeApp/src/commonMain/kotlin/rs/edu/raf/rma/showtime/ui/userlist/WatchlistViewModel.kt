package rs.edu.raf.rma.showtime.ui.userlist

import rs.edu.raf.rma.showtime.domain.MoviesRepository

class WatchlistViewModel(
    moviesRepository: MoviesRepository,
) : UserListViewModel(UserListType.Watchlist, moviesRepository)
