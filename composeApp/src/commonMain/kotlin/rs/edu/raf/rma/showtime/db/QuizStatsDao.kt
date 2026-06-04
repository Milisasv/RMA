package rs.edu.raf.rma.showtime.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizStatsDao {

    @Query("SELECT * FROM quiz_stats WHERE id = :id")
    fun observeStats(id: Int): Flow<QuizStatsEntity?>

    @Query("SELECT * FROM quiz_stats WHERE id = :id")
    suspend fun getStats(id: Int): QuizStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStats(stats: QuizStatsEntity)
}
