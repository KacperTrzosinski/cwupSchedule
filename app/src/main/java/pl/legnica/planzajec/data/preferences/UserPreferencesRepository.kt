package pl.legnica.planzajec.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

enum class AppTheme(val label: String) {
    LIQUID_OBSIDIAN("Liquid Obsidian"),
    AURORA_PURPLE("Aurora Purple"),
    EMERALD_MATRIX("Emerald Matrix"),
    DEEP_OCEAN("Deep Ocean"),
    PURE_AMOLED("Pure AMOLED")
}

enum class UiScale(val factor: Float, val label: String) {
    COMPACT(0.90f, "Mały (90%)"),
    NORMAL(1.0f, "Standard (100%)"),
    LARGE(1.10f, "Duży (110%)"),
    EXTRA_LARGE(1.20f, "B. duży (120%)")
}

data class UserPreferences(
    val selectedDepartmentId: Int?,
    val selectedCourseName: String?,
    val selectedGroupCode: String?,
    val selectedSubgroup: String?,
    val isAmoledTheme: Boolean,
    val appTheme: AppTheme,
    val uiScale: UiScale,
    val filterMode: FilterMode,
    val mergeConsecutiveBlocks: Boolean,
    val notificationsEnabled: Boolean,
    val onboardingCompleted: Boolean,
    val viewMode: ViewMode
)

enum class FilterMode {
    ALL,
    CAMPUS_ONLY,
    ONLINE_ONLY
}

enum class ViewMode {
    UPCOMING,
    DAY_BY_DAY
}

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val SELECTED_DEPARTMENT_ID = intPreferencesKey("selected_department_id")
        val SELECTED_COURSE_NAME = stringPreferencesKey("selected_course_name")
        val SELECTED_GROUP_CODE = stringPreferencesKey("selected_group_code")
        val SELECTED_SUBGROUP = stringPreferencesKey("selected_subgroup")
        val IS_AMOLED_THEME = booleanPreferencesKey("is_amoled_theme")
        val APP_THEME = stringPreferencesKey("app_theme")
        val UI_SCALE = stringPreferencesKey("ui_scale")
        val FILTER_MODE = stringPreferencesKey("filter_mode")
        val MERGE_CONSECUTIVE_BLOCKS = booleanPreferencesKey("merge_consecutive_blocks")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val VIEW_MODE = stringPreferencesKey("view_mode")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        val deptId = prefs[Keys.SELECTED_DEPARTMENT_ID]?.takeIf { it > 0 }
        val course = prefs[Keys.SELECTED_COURSE_NAME]?.takeIf { it.isNotBlank() }
        val group = prefs[Keys.SELECTED_GROUP_CODE]?.takeIf { it.isNotBlank() }
        val subgroup = prefs[Keys.SELECTED_SUBGROUP]?.takeIf { it.isNotBlank() }
        val isAmoled = prefs[Keys.IS_AMOLED_THEME] ?: false
        val themeStr = prefs[Keys.APP_THEME] ?: (if (isAmoled) AppTheme.PURE_AMOLED.name else AppTheme.LIQUID_OBSIDIAN.name)
        val appTheme = try { AppTheme.valueOf(themeStr) } catch (_: Exception) { AppTheme.LIQUID_OBSIDIAN }
        val scaleStr = prefs[Keys.UI_SCALE] ?: UiScale.NORMAL.name
        val uiScale = try { UiScale.valueOf(scaleStr) } catch (_: Exception) { UiScale.NORMAL }
        val filterModeStr = prefs[Keys.FILTER_MODE] ?: FilterMode.ALL.name
        val filterMode = try { FilterMode.valueOf(filterModeStr) } catch (_: Exception) { FilterMode.ALL }
        val mergeBlocks = prefs[Keys.MERGE_CONSECUTIVE_BLOCKS] ?: false
        val notifEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
        val onboardingDone = prefs[Keys.ONBOARDING_COMPLETED] ?: false
        val viewModeStr = prefs[Keys.VIEW_MODE] ?: ViewMode.UPCOMING.name
        val viewMode = try { ViewMode.valueOf(viewModeStr) } catch (_: Exception) { ViewMode.UPCOMING }

        UserPreferences(
            selectedDepartmentId = deptId,
            selectedCourseName = course,
            selectedGroupCode = group,
            selectedSubgroup = subgroup,
            isAmoledTheme = isAmoled || appTheme == AppTheme.PURE_AMOLED,
            appTheme = appTheme,
            uiScale = uiScale,
            filterMode = filterMode,
            mergeConsecutiveBlocks = mergeBlocks,
            notificationsEnabled = notifEnabled,
            onboardingCompleted = onboardingDone,
            viewMode = viewMode
        )
    }

    suspend fun saveGroupSelection(
        departmentId: Int,
        courseName: String,
        groupCode: String,
        subgroup: String? = null
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_DEPARTMENT_ID] = departmentId
            prefs[Keys.SELECTED_COURSE_NAME] = courseName
            prefs[Keys.SELECTED_GROUP_CODE] = groupCode
            if (subgroup != null) {
                prefs[Keys.SELECTED_SUBGROUP] = subgroup
            } else {
                prefs.remove(Keys.SELECTED_SUBGROUP)
            }
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }

    suspend fun updateSubgroup(subgroup: String?) {
        context.dataStore.edit { prefs ->
            if (subgroup != null) {
                prefs[Keys.SELECTED_SUBGROUP] = subgroup
            } else {
                prefs.remove(Keys.SELECTED_SUBGROUP)
            }
        }
    }

    suspend fun setAmoledTheme(enabled: Boolean) {
        context.dataStore.edit {
            it[Keys.IS_AMOLED_THEME] = enabled
            if (enabled) {
                it[Keys.APP_THEME] = AppTheme.PURE_AMOLED.name
            } else if (it[Keys.APP_THEME] == AppTheme.PURE_AMOLED.name) {
                it[Keys.APP_THEME] = AppTheme.LIQUID_OBSIDIAN.name
            }
        }
    }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit {
            it[Keys.APP_THEME] = theme.name
            it[Keys.IS_AMOLED_THEME] = (theme == AppTheme.PURE_AMOLED)
        }
    }

    suspend fun setUiScale(scale: UiScale) {
        context.dataStore.edit {
            it[Keys.UI_SCALE] = scale.name
        }
    }

    suspend fun setFilterMode(mode: FilterMode) {
        context.dataStore.edit { it[Keys.FILTER_MODE] = mode.name }
    }

    suspend fun setMergeConsecutiveBlocks(enabled: Boolean) {
        context.dataStore.edit { it[Keys.MERGE_CONSECUTIVE_BLOCKS] = enabled }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setViewMode(mode: ViewMode) {
        context.dataStore.edit { it[Keys.VIEW_MODE] = mode.name }
    }

    suspend fun clearGroupSelection() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SELECTED_DEPARTMENT_ID)
            prefs.remove(Keys.SELECTED_COURSE_NAME)
            prefs.remove(Keys.SELECTED_GROUP_CODE)
            prefs.remove(Keys.SELECTED_SUBGROUP)
            prefs[Keys.ONBOARDING_COMPLETED] = false
        }
    }
}
