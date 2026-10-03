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

            val isAmoled = userPrefs?.isAmoledTheme ?: false

            CwupScheduleTheme(isAmoled = isAmoled) {
                pl.legnica.planzajec.ui.theme.LiquidGlassBackground(isAmoled = isAmoled) {
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
                                // Onboarding completed, state will update via DataStore flow
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
