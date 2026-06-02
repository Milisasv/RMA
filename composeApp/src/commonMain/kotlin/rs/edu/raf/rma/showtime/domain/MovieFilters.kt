package rs.edu.raf.rma.showtime.domain

data class MovieFilters(
    val search: String = "",
    val genre: String = "",
    val yearFrom: String = "",
    val yearTo: String = "",
    val minRating: String = "",
    val sort: MovieSort = MovieSort.RatingDesc,
    val page: Int = 1,
    val pageSize: Int = 20,
) {
    val searchOrNull: String? get() = search.trim().takeIf { it.isNotEmpty() }
    val genreOrNull: String? get() = genre.trim().takeIf { it.isNotEmpty() }
    val genreIdInt: Int? get() = genre.trim().toIntOrNull()
    val yearFromInt: Int? get() = yearFrom.trim().toIntOrNull()
    val yearToInt: Int? get() = yearTo.trim().toIntOrNull()
    val minRatingDouble: Double? get() = minRating.trim().toDoubleOrNull()
    val offset: Int get() = (page - 1).coerceAtLeast(0) * pageSize
}

enum class MovieSort(
    val apiName: String,
    val apiOrder: String,
    val localName: String,
    val title: String,
) {
    RatingDesc("imdb_rating", "desc", "rating_desc", "Rating ↓"),
    RatingAsc("imdb_rating", "asc", "rating_asc", "Rating ↑"),
    YearDesc("year", "desc", "year_desc", "Year ↓"),
    YearAsc("year", "asc", "year_asc", "Year ↑"),
    TitleAsc("title", "asc", "title_asc", "Title A-Z"),
    TitleDesc("title", "desc", "title_desc", "Title Z-A"),
}
