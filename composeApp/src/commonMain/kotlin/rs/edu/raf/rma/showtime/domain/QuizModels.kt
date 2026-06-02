package rs.edu.raf.rma.showtime.domain

data class QuizQuestion(
    val id: Int,
    val type: QuizQuestionType,
    val prompt: String,
    val imageUrl: String?,
    val answers: List<QuizAnswer>,
    val correctAnswerId: String,
)

data class QuizAnswer(
    val id: String,
    val text: String,
)

enum class QuizQuestionType {
    GuessMovie,
    GuessYear,
    GuessLeadActor,
}

data class QuizResult(
    val score: Double,
    val correct: Int,
    val wrong: Int,
    val usedSeconds: Int,
)

data class QuizStats(
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val lastScore: Double = 0.0,
    val lastCorrect: Int = 0,
    val lastWrong: Int = 0,
)
