package pl.legnica.planzajec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.legnica.planzajec.data.local.entity.ScheduleMetadataEntity

@Dao
interface MetadataDao {

    @Query("SELECT * FROM schedule_metadata WHERE groupCode = :groupCode LIMIT 1")
    fun getMetadata(groupCode: String): Flow<ScheduleMetadataEntity?>

    @Query("SELECT * FROM schedule_metadata WHERE groupCode = :groupCode LIMIT 1")
    suspend fun getMetadataDirect(groupCode: String): ScheduleMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetadata(metadata: ScheduleMetadataEntity)
}
