package rs.edu.raf.rma.showtime.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quiz_stats")
data class QuizStatsEntity(
    @PrimaryKey val id: Int = 1,
    val bestScore: Double = 0.0,
    val playedCount: Int = 0,
    val lastScore: Double = 0.0,
    val lastCorrect: Int = 0,
    val lastWrong: Int = 0,
)
