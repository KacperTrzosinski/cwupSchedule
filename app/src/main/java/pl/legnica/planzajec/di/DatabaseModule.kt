package pl.legnica.planzajec.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import pl.legnica.planzajec.data.local.AppDatabase
import pl.legnica.planzajec.data.local.dao.LessonDao
import pl.legnica.planzajec.data.local.dao.MetadataDao
import pl.legnica.planzajec.data.local.dao.StructureDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "cwup_schedule.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()

    @Provides
    fun provideStructureDao(db: AppDatabase): StructureDao = db.structureDao()

    @Provides
    fun provideMetadataDao(db: AppDatabase): MetadataDao = db.metadataDao()
}
