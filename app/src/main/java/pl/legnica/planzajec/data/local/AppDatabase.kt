package pl.legnica.planzajec.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pl.legnica.planzajec.data.local.dao.LessonDao
import pl.legnica.planzajec.data.local.dao.MetadataDao
import pl.legnica.planzajec.data.local.dao.StructureDao
import pl.legnica.planzajec.data.local.entity.DepartmentEntity
import pl.legnica.planzajec.data.local.entity.LessonEntity
import pl.legnica.planzajec.data.local.entity.RoomEntity
import pl.legnica.planzajec.data.local.entity.ScheduleMetadataEntity
import pl.legnica.planzajec.data.local.entity.StudyCourseEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.local.entity.TeacherEntity

@Database(
    entities = [
        LessonEntity::class,
        DepartmentEntity::class,
        StudyCourseEntity::class,
        StudyGroupEntity::class,
        ScheduleMetadataEntity::class,
        TeacherEntity::class,
        RoomEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun lessonDao(): LessonDao
    abstract fun structureDao(): StructureDao
    abstract fun metadataDao(): MetadataDao
}
