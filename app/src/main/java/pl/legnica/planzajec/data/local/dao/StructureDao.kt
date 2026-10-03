package pl.legnica.planzajec.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pl.legnica.planzajec.data.local.entity.DepartmentEntity
import pl.legnica.planzajec.data.local.entity.RoomEntity
import pl.legnica.planzajec.data.local.entity.StudyCourseEntity
import pl.legnica.planzajec.data.local.entity.StudyGroupEntity
import pl.legnica.planzajec.data.local.entity.TeacherEntity

@Dao
interface StructureDao {

    // Departments
    @Query("SELECT * FROM departments ORDER BY id ASC")
    fun getAllDepartments(): Flow<List<DepartmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepartments(departments: List<DepartmentEntity>)

    // Courses
    @Query("SELECT * FROM study_courses WHERE departmentId = :departmentId ORDER BY name ASC")
    fun getCoursesForDepartment(departmentId: Int): Flow<List<StudyCourseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<StudyCourseEntity>)

    // Groups
    @Query("SELECT * FROM study_groups WHERE departmentId = :departmentId ORDER BY name ASC")
    fun getGroupsForDepartment(departmentId: Int): Flow<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE courseName = :courseName ORDER BY name ASC")
    fun getGroupsForCourse(courseName: String): Flow<List<StudyGroupEntity>>

    @Query("SELECT * FROM study_groups WHERE code = :groupCode LIMIT 1")
    suspend fun getGroupByCode(groupCode: String): StudyGroupEntity?

    @Query("SELECT * FROM study_groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<StudyGroupEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<StudyGroupEntity>)

    // Teachers
    @Query("SELECT * FROM teachers ORDER BY name ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherEntity>)

    // Rooms
    @Query("SELECT * FROM rooms ORDER BY building ASC, name ASC")
    fun getAllRooms(): Flow<List<RoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRooms(rooms: List<RoomEntity>)
}
