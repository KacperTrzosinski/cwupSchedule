package pl.legnica.planzajec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import pl.legnica.planzajec.data.local.entity.LessonEntity
import java.time.LocalDate

@Dao
interface LessonDao {

    @Query("SELECT * FROM lessons WHERE groupCode = :groupCode ORDER BY date ASC, startTime ASC")
    fun getLessonsForGroup(groupCode: String): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupCode = :groupCode AND date = :date ORDER BY startTime ASC")
    fun getLessonsForGroupAndDate(groupCode: String, date: LocalDate): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupCode = :groupCode AND date >= :fromDate ORDER BY date ASC, startTime ASC")
    fun getUpcomingLessons(groupCode: String, fromDate: LocalDate): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE groupCode = :groupCode")
    suspend fun getLessonsListForGroup(groupCode: String): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<LessonEntity>)

    @Query("DELETE FROM lessons WHERE groupCode = :groupCode")
    suspend fun deleteLessonsForGroup(groupCode: String)

    @Transaction
    suspend fun replaceLessonsForGroup(groupCode: String, lessons: List<LessonEntity>) {
        deleteLessonsForGroup(groupCode)
        insertLessons(lessons)
    }

    @Query("SELECT DISTINCT subgroup FROM lessons WHERE groupCode = :groupCode")
    suspend fun getSubgroupsForGroup(groupCode: String): List<String>
}
