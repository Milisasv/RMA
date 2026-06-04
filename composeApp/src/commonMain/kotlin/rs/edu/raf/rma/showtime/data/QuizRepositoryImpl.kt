package rs.edu.raf.rma.showtime.data

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.showtime.db.MovieEntity
import rs.edu.raf.rma.showtime.db.QuizStatsEntity
import rs.edu.raf.rma.showtime.domain.QuizAnswer
import rs.edu.raf.rma.showtime.domain.QuizQuestion
import rs.edu.raf.rma.showtime.domain.QuizQuestionType
import rs.edu.raf.rma.showtime.domain.QuizRepository
import rs.edu.raf.rma.showtime.domain.QuizResult
import rs.edu.raf.rma.showtime.domain.QuizStats
import rs.edu.raf.rma.showtime.network.MoviesApi
import rs.edu.raf.rma.showtime.network.model.QuizSubmitBody

class QuizRepositoryImpl(
    private val database: AppDatabase,
    private val privateApi: MoviesApi,
    private val authStore: AuthStore,
) : QuizRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeStats(): Flow<QuizStats> =
        authStore.authState
            .map { state ->
                (state as? AuthState.Authenticated)
                    ?.data
                    ?.quizStatsId()
            }
            .distinctUntilChanged()
            .flatMapLatest { statsId ->
                if (statsId == null) {
                    flowOf(QuizStats())
                } else {
                    database.quizStatsDao()
                        .observeStats(statsId)
                        .map { it?.toDomain() ?: QuizStats() }
                }
            }

    override suspend fun buildSession(): List<QuizQuestion> {
        val pool = database.movieDao()
            .getQuizPool(limit = 200)
            .filter { movie ->
                movie.title.isNotBlank() &&
                    (!movie.posterUrl.isNullOrBlank() || !movie.backdropUrl.isNullOrBlank())
            }

        if (pool.size < 10) {
            error("Browse the catalog first to populate your quiz pool.")
        }

        val questions = mutableListOf<QuizQuestion>()
        val usedMovieIds = mutableSetOf<String>()
        val usedImages = mutableSetOf<String>()
        val typeCounter = mutableMapOf<QuizQuestionType, Int>()
        var attempts = 0

        while (questions.size < 10 && attempts < 1000) {
            attempts++
            val type = QuizQuestionType.entries
                .filter { (typeCounter[it] ?: 0) < 4 }
                .shuffled()
                .firstOrNull() ?: break

            val movie = pool
                .filter { it.id !in usedMovieIds }
                .ifEmpty { pool }
                .shuffled()
                .firstOrNull() ?: break

            val question = createQuestion(
                number = questions.size + 1,
                type = type,
                movie = movie,
                pool = pool,
                usedImages = usedImages,
            ) ?: continue

            questions += question
            usedMovieIds += movie.id
            typeCounter[type] = (typeCounter[type] ?: 0) + 1
        }

        if (questions.size < 10) {
            error("Not enough local movie data for a full quiz session.")
        }

        return questions
    }

    override suspend fun saveResult(result: QuizResult) {
        val statsId = authStore.currentAuthData().quizStatsId()
        val dao = database.quizStatsDao()
        val current = dao.getStats(statsId) ?: QuizStatsEntity(id = statsId)
        dao.upsertStats(
            current.copy(
                bestScore = kotlin.math.max(current.bestScore, result.score),
                playedCount = current.playedCount + 1,
                lastScore = result.score,
                lastCorrect = result.correct,
                lastWrong = result.wrong,
            )
        )
        runCatching { privateApi.submitQuizResult(QuizSubmitBody(score = result.score)) }
    }

    private fun createQuestion(
        number: Int,
        type: QuizQuestionType,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
    ): QuizQuestion? = when (type) {
        QuizQuestionType.GuessMovie -> guessMovie(number, movie, pool, usedImages)
        QuizQuestionType.GuessYear -> guessYear(number, movie, usedImages)
        QuizQuestionType.GuessLeadActor -> guessLeadActor(number, movie, pool, usedImages)
    }

    private fun guessMovie(
        number: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val image = movie.backdropUrl?.takeIf { it !in usedImages } ?: return null
        usedImages += image
        val wrongTitles = pool
            .filter { it.id != movie.id }
            .map { it.title }
            .filter { it.isNotBlank() }
            .distinct()
            .shuffled()
            .take(3)
        if (wrongTitles.size < 3) return null

        return QuizQuestion(
            id = number,
            type = QuizQuestionType.GuessMovie,
            prompt = "Which movie is shown in the image?",
            imageUrl = image,
            answers = (wrongTitles + movie.title).shuffled().map { QuizAnswer(id = it, text = it) },
            correctAnswerId = movie.title,
        )
    }

    private fun guessYear(
        number: Int,
        movie: MovieEntity,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val year = movie.year ?: return null
        val image = chooseUnusedImage(movie, usedImages, preferPoster = true) ?: return null
        val wrongYears = (-10..10)
            .filter { it != 0 }
            .map { year + it }
            .filter { it > 1800 }
            .distinct()
            .shuffled()
            .take(3)
        if (wrongYears.size < 3) return null

        return QuizQuestion(
            id = number,
            type = QuizQuestionType.GuessYear,
            prompt = "In which year was '${movie.title}' released?",
            imageUrl = image,
            answers = (wrongYears + year).shuffled().map { QuizAnswer(id = it.toString(), text = it.toString()) },
            correctAnswerId = year.toString(),
        )
    }

    private fun guessLeadActor(
        number: Int,
        movie: MovieEntity,
        pool: List<MovieEntity>,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val movieActors = movie.actorsText.toParts()
        val correct = movieActors.take(3).filter { it.isNotBlank() }.randomOrNull() ?: return null
        val wrongActors = pool
            .flatMap { it.actorsText.toParts() }
            .filter { it !in movieActors }
            .distinct()
            .shuffled()
            .take(3)
        if (wrongActors.size < 3) return null
        val image = chooseUnusedImage(movie, usedImages, preferPoster = true) ?: return null

        return QuizQuestion(
            id = number,
            type = QuizQuestionType.GuessLeadActor,
            prompt = "Which lead actor appears in '${movie.title}'?",
            imageUrl = image,
            answers = (wrongActors + correct).shuffled().map { QuizAnswer(id = it, text = it) },
            correctAnswerId = correct,
        )
    }

    private fun chooseUnusedImage(
        movie: MovieEntity,
        usedImages: MutableSet<String>,
        preferPoster: Boolean,
    ): String? {
        val images = if (preferPoster) {
            listOfNotNull(movie.posterUrl, movie.backdropUrl)
        } else {
            listOfNotNull(movie.backdropUrl, movie.posterUrl).shuffled()
        }
        val image = images.firstOrNull { it !in usedImages } ?: return null
        usedImages += image
        return image
    }

    private fun AuthData.quizStatsId(): Int {
        val key = when {
            username.isNotBlank() -> "username:${username.trim().lowercase()}"
            userId != null -> "id:$userId"
            !accessToken.isNullOrBlank() -> "token:${accessToken.takeLast(24)}"
            else -> "guest"
        }
        return key.stablePositiveHash()
    }

    private fun String.stablePositiveHash(): Int {
        var hash = 17
        for (char in this) {
            hash = hash * 31 + char.code
        }
        return hash and Int.MAX_VALUE
    }

    private fun String.toParts(): List<String> =
        split(" • ").map { it.trim() }.filter { it.isNotEmpty() }
}
