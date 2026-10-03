package pl.legnica.planzajec.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pl.legnica.planzajec.data.repository.ChangeDetector
import pl.legnica.planzajec.parser.CourseGroupParser
import pl.legnica.planzajec.parser.DepartmentParser
import pl.legnica.planzajec.parser.RoomParser
import pl.legnica.planzajec.parser.ScheduleParser
import pl.legnica.planzajec.parser.TeacherParser
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideDepartmentParser(): DepartmentParser = DepartmentParser()

    @Provides
    @Singleton
    fun provideCourseGroupParser(): CourseGroupParser = CourseGroupParser()

    @Provides
    @Singleton
    fun provideScheduleParser(): ScheduleParser = ScheduleParser()

    @Provides
    @Singleton
    fun provideTeacherParser(): TeacherParser = TeacherParser()

    @Provides
    @Singleton
    fun provideRoomParser(): RoomParser = RoomParser()

    @Provides
    @Singleton
    fun provideChangeDetector(): ChangeDetector = ChangeDetector()
}
