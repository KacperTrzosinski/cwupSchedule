package pl.legnica.planzajec

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import pl.legnica.planzajec.data.preferences.UserPreferencesRepository
import pl.legnica.planzajec.ui.navigation.MainScreen
import pl.legnica.planzajec.ui.onboarding.OnboardingScreen
import pl.legnica.planzajec.ui.theme.CwupScheduleTheme
import pl.legnica.planzajec.ui.theme.DarkBackground
import javax.inject.Inject

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import pl.legnica.planzajec.data.preferences.AppTheme
import pl.legnica.planzajec.data.preferences.UiScale
import pl.legnica.planzajec.notification.ScheduleAlarmReceiver

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userPrefs by preferencesRepository.userPreferencesFlow.collectAsState(initial = null)
            val scope = rememberCoroutineScope()

            val appTheme = userPrefs?.appTheme ?: AppTheme.LIQUID_OBSIDIAN
            val isAmoled = userPrefs?.isAmoledTheme ?: false
            val uiScale = userPrefs?.uiScale ?: UiScale.NORMAL

            LaunchedEffect(userPrefs?.selectedGroupCode, userPrefs?.notificationsEnabled) {
                if (userPrefs?.selectedGroupCode != null && userPrefs?.notificationsEnabled == true) {
                    ScheduleAlarmReceiver.triggerImmediateUpdate(this@MainActivity)
                }
            }

            val currentDensity = LocalDensity.current
            val scaledDensity = Density(
                density = currentDensity.density,
                fontScale = currentDensity.fontScale * uiScale.factor
            )

            CompositionLocalProvider(LocalDensity provides scaledDensity) {
                CwupScheduleTheme(appTheme = appTheme, isAmoled = isAmoled) {
                    pl.legnica.planzajec.ui.theme.LiquidGlassBackground(appTheme = appTheme, isAmoled = isAmoled) {
                        val prefs = userPrefs
                        if (prefs == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF00D2FF))
                            }
                        } else if (!prefs.onboardingCompleted) {
                            OnboardingScreen(
                                viewModel = hiltViewModel(),
                                onFinish = {
                                    ScheduleAlarmReceiver.triggerImmediateUpdate(this@MainActivity)
                                }
                            )
                        } else {
                            MainScreen(
                                preferencesRepository = preferencesRepository,
                                onChangeGroupClick = {
                                    scope.launch {
                                        preferencesRepository.clearGroupSelection()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
